package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.Property;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class DateSchemaMapper extends AbstractSchemaMapper<DateProperty, LocalDate> {
  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.DATE);

  @Override
  public LocalDate mapTupleValue(@NonNull DateProperty schema, @NonNull ValueContext valueContext) {
    return convertToType(
        SchemaMapperUtils.castLiteralValue(valueContext.getValue()));
  }

  @Override
  public LocalDate mapGraphValue(@NonNull DateProperty property, @NonNull GraphEntity graphEntity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    Map<String, Object> vendorExtensions = property.getVendorExtensions();

    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.CONSTANT_VALUE)) {
      return handleConstantValueVendorExtension(property);
    }

    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.LDPATH)) {
      return handleLdPathVendorExtension(property, valueContext, graphEntity);
    }
    return null;
  }

  private LocalDate handleConstantValueVendorExtension(DateProperty schema) {
    Object value = schema.getVendorExtensions().get(OpenApiSpecificationExtensions.CONSTANT_VALUE);

    if (value != null) {
      if (isSupportedLiteral(value)) {
        return convertToType(((Literal) value));
      }
      ValueFactory valueFactory = SimpleValueFactory.getInstance();
      Literal literal = valueFactory.createLiteral((String) value, XMLSchema.DATE);
      return convertToType(literal);
    }

    if (schema.getRequired()) {
      throw new SchemaMapperRuntimeException(String.format(
          "String Property has '%s' vendor extension that is null, but the property is required.",
          OpenApiSpecificationExtensions.CONSTANT_VALUE));
    }
    return null;
  }

  @Override
  public String expectedException(String ldPathQuery) {
    return String.format(
            "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>",
            ldPathQuery, XMLSchema.DATE.stringValue());
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof DateProperty;
  }

  @Override
  LocalDate convertToType(Literal literal) {
    return LocalDate.parse(literal.calendarValue().toString());
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

}
