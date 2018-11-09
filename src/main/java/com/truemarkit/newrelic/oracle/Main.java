package com.truemarkit.newrelic.oracle;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.binding.Context;
import com.newrelic.metrics.publish.binding.Request;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.newrelic.metrics.publish.configuration.SDKConfiguration;
import com.newrelic.metrics.publish.internal.DataCollector;
import com.newrelic.metrics.publish.util.Logger;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Main entry point for the plugin.
 *
 * @author Dilip S Sisodia
 * @author Erik R. Jensen
 */
public class Main {

  private static final Logger log = Logger.getLogger(Main.class);

  public static void main(String[] args) {
    try {
      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(Runner.class);
      enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> {
        if (method.getDeclaringClass() != Object.class && method.getName().equals("setupAndRun")) {
          Runner runner = (Runner) o;
          SDKConfiguration config = ((Runner) o).getConfiguration();
          Method privateMethod = Runner.class.getDeclaredMethod("setupAgents");
          privateMethod.setAccessible(true);
          privateMethod.invoke(o, objects);

          // TODO: when removing SDKConfiguration, move config validation here
          int pollInterval = 60;

          ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
          ScheduledFuture<?> future = executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
              try {
                log.debug("Harvest and report data");

                Field runnerContextField = Runner.class.getDeclaredField("context");
                runnerContextField.setAccessible(true);
                Context runnerContext = (Context) runnerContextField.get(o);
                Request request = runnerContext.createRequest();

                ExecutorService executorService = Executors.newWorkStealingPool(10);

                Field componentAgentsField = Runner.class.getDeclaredField("componentAgents");
                componentAgentsField.setAccessible(true);
                List<Agent> runnerComponentAgents = (List<Agent>) componentAgentsField.get(o);
                for (Iterator<Agent> iterator = runnerComponentAgents.iterator(); iterator.hasNext(); ) {

                  Agent agent = iterator.next();
                  Field agentCollectorField = Agent.class.getDeclaredField("collector");
                  agentCollectorField.setAccessible(true);
                  DataCollector agentCollector = (DataCollector) agentCollectorField.get(agent);
                  agentCollector.setRequest(request);
                  Runnable agentPollTask = () -> {
                    log.debug("Beginning poll cycle for agent: '", agent.getAgentName(), "'");
                    agent.pollCycle();
                    log.debug("Ending poll cycle for agent: '", agent.getAgentName(), "'");
                  };
                  executorService.execute(agentPollTask);
                }
                 try {
                   executorService.shutdown();
                   executorService.awaitTermination(40, TimeUnit.SECONDS);
                 }
                 catch (InterruptedException e) {
                 	log.error("tasks interrupted");
                 }
                 finally {
                   executorService.shutdownNow();
                 }
                request.deliver();
              } catch (Exception e) {
                // log exception and continue polling -- could be a transient issue
                // java.lang.Error(s) are thrown and handled by the main thread
                log.error("SEVERE: An error has occurred");
                e.printStackTrace();
              }

            }
          }, 0, pollInterval, TimeUnit.SECONDS);  //schedule pollAgentsRunnable as the runnable command

          System.out.println("INFO: New Relic monitor started");

          try {
            // getting the future's response will block forever unless an exception is thrown
            future.get();
          } catch (InterruptedException e) {
            System.err.println("SEVERE: An error has occurred");
            e.printStackTrace();
          } catch (CancellationException e) {
            System.err.println("SEVERE: An error has occurred");
            e.printStackTrace();
          } catch (ExecutionException e) {
            // ExecutionException will wrap any java.lang.Error from the polling thread that we should not catch there (e.g. OutOfMemoryError)
            System.err.println("SEVERE: An error has occurred");
            e.printStackTrace();
          } finally {
            // clean up
            future.cancel(true);
            executor.shutdown();
          }
          return null;
        } else {
          return methodProxy.invokeSuper(o, objects);
        }
      });

      Runner runner = (Runner) enhancer.create();
      runner.add(new OracleAgentFactory());
      runner.setupAndRun();
    } catch (ConfigurationException e) {
      log.error("Error starting plugin: " + e.getMessage(), e);
      System.exit(1);
    }
  }
}