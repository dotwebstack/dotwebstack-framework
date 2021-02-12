package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.TYPE;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
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

        List<String> fieldNames = List.of(TYPE, AS_WKB, AS_WKT);

        GraphQLUnmodifiedType fieldType = GraphQLTypeUtil.unwrapAll(environment.getFieldType());

        return environment.getParentType()
            .getName()
            .equals(GEOMETRY) && fieldNames.contains(fieldType.getName());
      }

      @Override
      public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
        return new SpatialDataFetcher();
      }
    };
  }
}
