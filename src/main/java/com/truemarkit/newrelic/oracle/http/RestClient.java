package com.truemarkit.newrelic.oracle.http;

import java.io.IOException;
import java.io.Serializable;

/**
 * Contract for all HttpClient implementations.
 *
 * @author Erik R. Jensen
 */
public interface RestClient extends Serializable {

  /**
   * Sends an HTTP GET request and loads the response into a new instance of the provided type.
   *
   * @param uri   the URI to GET
   * @param clazz the type of instance to create
   * @param <T>   the type of the instance
   * @return the newly created instance
   * @throws IOException if an error occurs
   */
  <T> T get(String uri, Class<T> clazz) throws IOException;

  /**
   * Sends an HTTP GET request and loads the response into a new instance of the provided type. This
   * method is specifically used to load instances of object which are using generics. To get around
   * the type erasure problem, the parameterClass is used to specify the type parameter to use for
   * the generic class.
   *
   * @param uri            the URI to GET
   * @param clazz          the type of instance to create
   * @param parameterClass the type parameter class
   * @param <T>            the type of the instance
   * @param <S>            the type of the type parameter
   * @return the newly created instance
   * @throws IOException if an error occurs
   */
  <T, S> T get(String uri, Class<T> clazz, Class<S> parameterClass) throws IOException;

  /**
   * Sends an HTTP POST request to create a new instance. The response is
   * loaded back into the request object passed into the method.
   *
   * @param uri the URI to POST
   * @param o   the request object
   * @param <T> the type of the request object
   * @throws IOException if an error occurs
   */
  <T> void create(String uri, T o) throws IOException;

  /**
   * Sends an HTTP POST request to create a new instance. The response is loaded back into the last
   * parameter of the method.
   *
   * @param uri   the URI to POST
   * @param o     the request object
   * @param clazz the response type
   * @param <V>   the request object type
   * @param <T>   the response object type
   * @return the populated response object
   * @throws IOException if an error occurs
   */
  <V, T> V create(String uri, T o, Class<V> clazz) throws IOException;
}
