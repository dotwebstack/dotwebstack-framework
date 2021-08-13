package org.dotwebstack.framework.service.openapi.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.dotwebstack.framework.service.openapi.helper.SchemaResolver.resolveSchema;

public class OasResponseBuilder {

  private final OpenAPI openApi;

  public OasResponseBuilder(OpenAPI openApi) {
    this.openApi = openApi;
  }

  public OasResponse build(Schema<?> responseSchema){
    OasResponse.Field root = toField(responseSchema, "response", true);
    OasResponse response =new OasResponse(root);

    return response;
  }

  private OasResponse.Field toField(Schema<?> schema, String name, boolean required) {
    Schema<?> resolved = resolveSchema(openApi, schema);
    if(isObject(resolved)){
      return toObjectField(resolved, name,required);
    }
    else if(isArray(resolved)){

    }
    if(isScalar(resolved)){
      return toScalarField(resolved, name, required);
    }
    return null;
  }

  private OasResponse.Field toScalarField(Schema<?> schema, String name, boolean required) {
    String expression = schema.getExtensions()!=null?(String) schema.getExtensions().get("x-dws-expression"):null;
    String type = schema.getType();
    boolean nillable = isNillable(schema);
    if(expression==null){
      return new OasResponse.ScalarField(name, nillable,required,  type);
    }
    else{
      return new OasResponse.ScalarExpressionField(name, nillable,required,  type, expression);
    }
  }

  private OasResponse.ObjectField toObjectField(Schema<?> schema, String name, boolean required) {
    // TODO oneOf, allOf
    boolean nillable = isNillable(schema);
    Map<String, OasResponse.Field> fields =  schema.getProperties().entrySet().stream().map(e->{
      String propertyName = e.getKey();
      boolean propertyRequired = schema.getRequired().contains(propertyName);
      return toField(e.getValue(), propertyName, propertyRequired);
    }).filter(Objects::nonNull).collect(Collectors.toMap(OasResponse.Field::getName, f->f));
    return new OasResponse.ObjectField(name,nillable, required, fields);
  }

  private boolean isNillable(Schema<?> schema) {
    boolean nillable = schema.getNullable()!=null? schema.getNullable():false;
    return nillable;
  }

  private boolean isArray(Schema<?> schema){
    return schema.getType().equals("array");
  }
  private boolean isObject(Schema<?> schema) {
    return schema.getType().equals("object");
  }

  private boolean isScalar(Schema<?> schema) {
    return Set.of("string", "number", "boolean","integer").contains(schema.getType());
  }
}
