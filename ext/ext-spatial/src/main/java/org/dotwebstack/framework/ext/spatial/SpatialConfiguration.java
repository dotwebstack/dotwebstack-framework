package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import java.util.Base64;
import java.util.List;
import lombok.AllArgsConstructor;
import org.dotwebstack.framework.ext.spatial.model.Spatial;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
public class SpatialConfiguration {

  private final Spatial spatial;

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
        return new SpatialDataFetcher(spatial, typeEnforcer);
      }
    };
  }

  public static void main(String[] args) {
    byte[] wkb = Base64.getDecoder()
        .decode("AKAAAAEAABz3QBEs3XyujYNASbjy39E2aUAIAAAAAAAA");
    var wkbReader = new WKBReader();

    try {
      Geometry geom = wkbReader.read(wkb);
      System.out.println(geom.getSRID());
    } catch (ParseException e) {
      throw new IllegalArgumentException("Unable to parse wkb to geometry.", e);
    }
  }
}
