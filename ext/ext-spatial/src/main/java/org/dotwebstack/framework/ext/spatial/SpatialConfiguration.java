package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
public class SpatialConfiguration {

  private final TypeEnforcer typeEnforcer;

  @Bean
  public WiringFactory wiringFactory() {
    return new WiringFactory() {

      @Override
      public boolean providesDataFetcher(FieldWiringEnvironment environment) {
        if (GraphQLTypeUtil.isList(environment.getFieldType())) {
          return false;
        }

        List<String> fieldNames = List.of(SpatialConstants.SRID, SpatialConstants.TYPE, SpatialConstants.AS_WKB,
            SpatialConstants.AS_WKT, SpatialConstants.AS_GEOJSON);
        return environment.getParentType()
            .getName()
            .equals(GEOMETRY)
            && fieldNames.contains(environment.getFieldDefinition()
                .getName());
      }

      @Override
      public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
        return new SpatialDataFetcher(typeEnforcer);
      }
    };
  }
}
