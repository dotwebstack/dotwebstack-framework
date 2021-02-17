package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpatialConfiguration {

  @Bean
  public WiringFactory wiringFactory() {
    return new WiringFactory() {

      @Override
      public boolean providesDataFetcher(FieldWiringEnvironment environment) {
        if (GraphQLTypeUtil.isList(environment.getFieldType())) {
          return false;
        }

        List<String> fieldNames = List.of(SpatialConstants.TYPE, SpatialConstants.AS_WKB, SpatialConstants.AS_WKT);
        return environment.getParentType()
            .getName()
            .equals(GEOMETRY)
            && fieldNames.contains(environment.getFieldDefinition()
                .getName());
      }

      @Override
      public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
        return new SpatialDataFetcher();
      }
    };
  }
}
