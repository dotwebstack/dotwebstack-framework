package org.dotwebstack.framework.ext.spatial;

import static graphql.language.EnumTypeDefinition.newEnumTypeDefinition;
import static graphql.language.EnumValueDefinition.newEnumValueDefinition;
import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.TypeName.newTypeName;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY_TYPE;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.LINESTRING;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.MULTILINESTRING;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.MULTIPOINT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.MULTIPOLYGON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.POINT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.POLYGON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.TYPE;

import graphql.Scalars;
import graphql.language.EnumTypeDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.core.GraphqlConfigurer;
import org.springframework.stereotype.Component;

@Component
public class SpatialConfigurer implements GraphqlConfigurer {

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(createGeometryTypeDefinition());
    registry.add(createGeometryObjectDefinition());
  }

  private ObjectTypeDefinition createGeometryObjectDefinition() {
    TypeName stringType = newTypeName(Scalars.GraphQLString.getName()).build();

    return newObjectTypeDefinition().name(GEOMETRY)
        .fieldDefinition(newFieldDefinition().name(TYPE)
            .type(newTypeName(GEOMETRY_TYPE).build())
            .build())
        .fieldDefinition(newFieldDefinition().name(AS_WKB)
            .type(stringType)
            .build())
        .fieldDefinition(newFieldDefinition().name(AS_WKT)
            .type(stringType)
            .build())
        .build();
  }

  private EnumTypeDefinition createGeometryTypeDefinition() {
    return newEnumTypeDefinition().name(GEOMETRY_TYPE)
        .enumValueDefinition(newEnumValueDefinition().name(POINT)
            .build())
        .enumValueDefinition(newEnumValueDefinition().name(LINESTRING)
            .build())
        .enumValueDefinition(newEnumValueDefinition().name(POLYGON)
            .build())
        .enumValueDefinition(newEnumValueDefinition().name(MULTIPOINT)
            .build())
        .enumValueDefinition(newEnumValueDefinition().name(MULTILINESTRING)
            .build())
        .enumValueDefinition(newEnumValueDefinition().name(MULTIPOLYGON)
            .build())
        .build();
  }
}
