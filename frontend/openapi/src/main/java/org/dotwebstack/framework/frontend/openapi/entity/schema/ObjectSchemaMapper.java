package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
class ObjectSchemaMapper extends AbstractSchemaMapper<ObjectProperty, Object>
    implements LdPathSchemaMapper {

  @Override
  public Object mapTupleValue(ObjectProperty schema, Value value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object mapGraphValue(ObjectProperty property, GraphEntityContext graphEntityContext,
      SchemaMapperAdapter schemaMapperAdapter, Value context) {

    Value contextNew = context;
    Set<Resource> subjects = applySubjectFilterIfPossible(property, graphEntityContext);
    if (!subjects.isEmpty()) {
      if (subjects.size() > 1) {
        throw new SchemaMapperRuntimeException(
            "More entrypoint subjects found. Only one is needed.");
      }
      contextNew = subjects.iterator().next();
    }

    if (property.getVendorExtensions().containsKey(OpenApiSpecificationExtensions.LDPATH)) {
      String ldPath =
          property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH).toString();
      return handleLdPathVendorExtension(property, graphEntityContext, contextNew, ldPath,
          schemaMapperAdapter);
    }

    return handleProperties(property, graphEntityContext, schemaMapperAdapter, contextNew);
  }



  private Map<String, Object> handleLdPathVendorExtension(ObjectProperty property,
      GraphEntityContext entityBuilderContext, Value context, String ldPathQuery,
      SchemaMapperAdapter schemaMapperAdapter) {

    LdPathExecutor ldPathExecutor = entityBuilderContext.getLdPathExecutor();
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(context, ldPathQuery);

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

    return handleProperties(property, entityBuilderContext, schemaMapperAdapter,
        queryResult.iterator().next());
  }

  private Map<String, Object> handleProperties(ObjectProperty property,
      GraphEntityContext entityBuilderContext, SchemaMapperAdapter schemaMapperAdapter,
      Value context) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    property.getProperties().forEach((propKey, propValue) -> {
      Object propertyResult = schemaMapperAdapter.mapGraphValue(propValue, entityBuilderContext,
          schemaMapperAdapter, context);
      builder.put(propKey, com.google.common.base.Optional.fromNullable(propertyResult));
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
