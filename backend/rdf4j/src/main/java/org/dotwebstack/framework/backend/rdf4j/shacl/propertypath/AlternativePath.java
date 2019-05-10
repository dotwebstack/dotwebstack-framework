package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AlternativePath extends PropertyPath {

  private final PropertyPath object;

}