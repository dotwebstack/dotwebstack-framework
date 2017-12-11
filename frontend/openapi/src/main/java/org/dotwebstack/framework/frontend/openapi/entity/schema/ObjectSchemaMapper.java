package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class ObjectSchemaMapper extends AbstractSubjectFilterSchemaMapper<ObjectProperty, Object> {

  @Override
  public Object mapTupleValue(ObjectProperty schema, SchemaMapperContext schemaMapperContext) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object mapGraphValue(ObjectProperty property, GraphEntityContext graphEntityContext,
      SchemaMapperContext schemaMapperContext, SchemaMapperAdapter schemaMapperAdapter) {

    processPropagationsInitial(property, schemaMapperContext);

    Object result =
        handleProperty(property, graphEntityContext, schemaMapperContext, schemaMapperAdapter);
    if (!isExcludedWhenNull(schemaMapperContext, property, result)) {
      return result;
    }
    return null;
  }

  private Object handleProperty(ObjectProperty property, GraphEntityContext graphEntityContext,
      SchemaMapperContext schemaMapperContext, SchemaMapperAdapter schemaMapperAdapter) {
    if (hasSubjectFilterVendorExtension(property)) {
      Set<Resource> subjects = filterSubjects(property, graphEntityContext);

      if (subjects.isEmpty()) {
        if (property.getRequired()) {
          throw new SchemaMapperRuntimeException(
              "Subject filter for a required object property yielded no result.");
        }

        return null;
      }

      if (subjects.size() > 1) {
        throw new SchemaMapperRuntimeException(
            "More entrypoint subjects found. Only one is required.");
      }

      schemaMapperContext.setValue(subjects.iterator().next());
    }

    if (hasVendorExtension(property, OpenApiSpecificationExtensions.LDPATH)) {
      String ldPath =
          property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH).toString();
      return handleLdPathVendorExtension(property, graphEntityContext, schemaMapperContext, ldPath,
          schemaMapperAdapter);
    }

    return handleProperties(property, graphEntityContext, schemaMapperContext, schemaMapperAdapter);
  }


  private Map<String, Object> handleLdPathVendorExtension(ObjectProperty property,
      GraphEntityContext entityBuilderContext, SchemaMapperContext schemaMapperContext,
      String ldPathQuery, SchemaMapperAdapter schemaMapperAdapter) {

    LdPathExecutor ldPathExecutor = entityBuilderContext.getLdPathExecutor();
    Collection<Value> queryResult =
        ldPathExecutor.ldPathQuery(schemaMapperContext.getValue(), ldPathQuery);

    if (queryResult.isEmpty()) {
      if (!property.getRequired()) {
        return null;
      }
      throw new SchemaMapperRuntimeException(String.format(
          "LDPath expression for a required object property ('%s') yielded no result.",
          ldPathQuery));
    }

    if (queryResult.size() > 1) {
      throw new SchemaMapperRuntimeException(String.format(
          "LDPath expression for object property ('%s') yielded multiple elements.", ldPathQuery));
    }
    schemaMapperContext.setValue(queryResult.iterator().next());
    return handleProperties(property, entityBuilderContext, schemaMapperContext,
        schemaMapperAdapter);
  }



  private Map<String, Object> handleProperties(ObjectProperty property,
      GraphEntityContext entityBuilderContext, SchemaMapperContext schemaMapperContext,
      SchemaMapperAdapter schemaMapperAdapter) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    property.getProperties().forEach((propKey, propValue) -> {
      Object propertyResult = schemaMapperAdapter.mapGraphValue(propValue, entityBuilderContext,
          schemaMapperContext, schemaMapperAdapter);

      if (!(propValue instanceof ArrayProperty)
          && (!isExcludedWhenNull(schemaMapperContext, propValue, propertyResult))) {

        builder.put(propKey, com.google.common.base.Optional.fromNullable(propertyResult));
      }
      if (((propValue instanceof ArrayProperty)
          && !isExcludedWhenEmpty(schemaMapperContext, propValue, propertyResult))) {

        builder.put(propKey, com.google.common.base.Optional.fromNullable(propertyResult));
      }
    });
    return builder.build();
  }



  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof ObjectProperty;
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return ImmutableSet.of();
  }

}
