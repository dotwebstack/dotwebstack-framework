package org.dotwebstack.framework.ext.spatial;

import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetcherFactories;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.PropertyDataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.WiringFactory;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.BiFunction;
import lombok.NonNull;
import org.dotwebstack.framework.core.GraphqlConfigurer;
import org.dotwebstack.framework.ext.spatial.formatter.GeometryFormatter;
import org.dotwebstack.framework.ext.spatial.formatter.WkbFormatter;
import org.dotwebstack.framework.ext.spatial.formatter.WktFormatter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.stereotype.Component;

@Component
public final class SpatialConfigurer implements GraphqlConfigurer {

  private final TypeEnforcer typeEnforcer;

  public SpatialConfigurer(TypeEnforcer typeEnforcer) {
    this.typeEnforcer = typeEnforcer;
  }

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(new ObjectTypeDefinition("Geometry"));
  }

  @Override
  public void configureRuntimeWiring(RuntimeWiring.@NonNull Builder builder) {
    builder.wiringFactory(new WiringFactory() {

      @Override
      public boolean providesDataFetcher(FieldWiringEnvironment environment) {
         if (GraphQLTypeUtil.isList(environment.getFieldType())) {
         return true;
         }

         GraphQLUnmodifiedType fieldType = GraphQLTypeUtil.unwrapAll(environment.getFieldType());

         return fieldType.getName().equals("Geometry");
      }

      @Override
      public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
        String fieldName = environment.getFieldDefinition()
            .getName();
        DataFetcher<?> defaultFetcher = new PropertyDataFetcher<>(fieldName);

        return DataFetcherFactories.wrapDataFetcher(defaultFetcher, geometryFetcher());
      }
    });

    builder.scalar(GraphQLScalarType.newScalar()
        .name("Geometry")
        .description("Geometry type")
        .coercing(new GeometryCoercing())
        .build());
  }

  private BiFunction<DataFetchingEnvironment, Object, Object> geometryFetcher() {
    return (environment, value) -> {
      // TODO: should this be handled elsewhere? Hoe does the default fetcher handles this?
      if (value instanceof Optional) {
        value = ((Optional<?>) value).orElse(null);
      }

      // TODO: is this necessary? Is fetcher called for null values?
      if (value == null) {
        return null;
      }

      Geometry geometry = null;

      if (value instanceof ByteBuffer) {
        ByteBuffer valueBuf = (ByteBuffer) value;
        byte[] wkbBytes = new byte[valueBuf.remaining()];
        valueBuf.get(wkbBytes);

        try {
          geometry = new WKBReader().read(wkbBytes);
        } catch (ParseException e) {
          throw new IllegalArgumentException("Cannot parse geometry from ByteBuffer.", e);
        }
      }

      if (geometry == null) {
        throw new IllegalArgumentException("Invalid type.");
      }

      String type = environment.getArgument("type");

      if (type != null) {
        geometry = typeEnforcer.enforce(GeometryType.valueOf(type), geometry);
      }

      GeometryFormat format = GeometryFormat.valueOf(environment.getArgument("format"));
      int dimensions = environment.getArgument("dimensions");

      GeometryFormatter<?> formatter;

      switch (format) {
        case WKT:
          formatter = WktFormatter.builder()
              .dimensions(dimensions)
              .build();
          break;
        case WKB:
          formatter = WkbFormatter.builder()
              .dimensions(dimensions)
              .build();
          break;
        default:
          throw new IllegalArgumentException("Invalid format.");
      }

      return new FormattedGeometry(geometry, formatter);
    };
  }
}
