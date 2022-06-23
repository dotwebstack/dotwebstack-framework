package org.dotwebstack.framework.ext.rml.mapping;

import io.carml.engine.rdf.RdfRmlMapper;

public interface RmlMapperConfigurer {

  void configureMapper(RdfRmlMapper.Builder builder);

}
