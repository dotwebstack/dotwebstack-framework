package org.dotwebstack.framework.service.openapi.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString
@Data
public class OasResponse {
  private Map<String, Field> root;

  public OasResponse(Field root) {
    this.root = Map.of(root.getName(), root);
  }

  @NoArgsConstructor
  @Data
  public static abstract class Field {
    @JsonIgnore
    private OasType type;
    @JsonIgnore
    private String name;
    @JsonIgnore
    private boolean nillable;
    @JsonIgnore
    private boolean required;

    public Field(String name, OasType type, boolean nillable, boolean required){
      this.name = name;
      this.type = type;
      this.nillable = nillable;
      this.required = required;
    }
  }

  @Data
  @NoArgsConstructor
  public static class EnvelopeObjectField extends Field {
    private Field field;

    public EnvelopeObjectField(String name, boolean nillable, boolean required, Field field){
      super(name, OasType.ENVELOPE_OBJECT, nillable, required);
      this.field = field;
    }
  }

  @Data
  @NoArgsConstructor
  public static class ObjectField extends Field {

    private Map<String, Field> fields;

    public ObjectField(String name, boolean nillable, boolean required, Map<String, Field> fields){
      super(name, OasType.OBJECT, nillable, required);
      this.fields = fields;
    }

    @JsonAnySetter
    public void add(String key, Field value) {
      this.fields.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Field> getFields(){
      return this.fields;
    }


  }
  @Data
  @NoArgsConstructor
  public static class ArrayField extends Field {
    private Field content;

    public ArrayField(String name, boolean nillable, boolean required, Field content) {
      super(name, OasType.ARRAY, nillable, required);
      this.content = content;
    }
  }

  @Data
  @NoArgsConstructor
  public static class ScalarField extends Field {

    private String scalarType;

    public ScalarField(String name, boolean nillable, boolean required, String scalarType){
      super(name, OasType.SCALAR, nillable, required);
      this.scalarType = scalarType;
    }
  }

  @Data
  @NoArgsConstructor
  public static class ScalarExpressionField extends Field {
    @JsonIgnore
    private String scalarType;
    @JsonIgnore
    private String expression;

    public ScalarExpressionField(String name, boolean nillable, boolean required, String scalarType, String expression){
      super(name, OasType.SCALAR_EXPRESSION, nillable, required);
      this.scalarType = scalarType;
      this.expression = expression;
    }
  }

  public enum OasType {
    OBJECT,
    ENVELOPE_OBJECT,
    ARRAY,
    SCALAR,
    SCALAR_EXPRESSION
  }
}

