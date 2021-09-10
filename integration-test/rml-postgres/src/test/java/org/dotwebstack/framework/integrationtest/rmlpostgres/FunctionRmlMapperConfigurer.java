package org.dotwebstack.framework.integrationtest.rmlpostgres;

import com.taxonic.carml.engine.rdf.RdfRmlMapper;
import org.dotwebstack.framework.ext.rml.mapping.RmlMapperConfigurer;
import org.springframework.stereotype.Component;

@Component
public class FunctionRmlMapperConfigurer implements RmlMapperConfigurer {

  @Override
  public void configureMapper(RdfRmlMapper.Builder builder) {
    builder.addFunctions(new IndicatieFunction());
  }
}
