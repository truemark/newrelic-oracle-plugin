package com.truemarkit.newrelic.oracle.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Holds List of type T.
 *
 * @author Dilip S Sisodia
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class ListView<T> {

  @JsonAlias({"plugins", "components"})
  private List<T> items;

}
