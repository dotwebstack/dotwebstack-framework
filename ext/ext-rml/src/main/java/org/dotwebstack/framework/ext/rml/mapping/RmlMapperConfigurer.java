package org.dotwebstack.framework.ext.rml.mapping;

import com.taxonic.carml.engine.rdf.RdfRmlMapper;

public interface RmlMapperConfigurer {

  void configureMapper(RdfRmlMapper.Builder builder);

}
