package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.Property;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class DateTimeSchemaMapper extends AbstractSchemaMapper<DateTimeProperty, LocalDateTime> {
  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.DATETIME);

  @Override
  public LocalDateTime mapTupleValue(@NonNull DateTimeProperty schema,
      @NonNull ValueContext valueContext) {
    return convertToDateTime(
        SchemaMapperUtils.castLiteralValue(valueContext.getValue()).calendarValue());
  }

  @Override
  public LocalDateTime mapGraphValue(@NonNull DateTimeProperty property,
      @NonNull GraphEntity graphEntity, @NonNull ValueContext valueContext,
      @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    Map<String, Object> vendorExtensions = property.getVendorExtensions();

    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.CONSTANT_VALUE)) {
      return handleConstantValueVendorExtension(property);
    }

    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.LDPATH)) {
      return handleLdPathVendorExtension(property, valueContext, graphEntity);
    }
    return null;
  }

  private LocalDateTime handleConstantValueVendorExtension(DateTimeProperty schema) {
    Object value = schema.getVendorExtensions().get(OpenApiSpecificationExtensions.CONSTANT_VALUE);

    if (value != null) {
      if (isSupportedLiteral(value)) {
        return convertToDateTime(((Literal) value).calendarValue());
      }
      ValueFactory valueFactory = SimpleValueFactory.getInstance();
      Literal literal = valueFactory.createLiteral((String) value, XMLSchema.DATETIME);
      return convertToDateTime(literal.calendarValue());
    }

    if (schema.getRequired()) {
      throw new SchemaMapperRuntimeException(String.format(
          "String Property has '%s' vendor extension that is null, but the property is required.",
          OpenApiSpecificationExtensions.CONSTANT_VALUE));
    }
    return null;
  }

  private LocalDateTime handleLdPathVendorExtension(@NonNull DateTimeProperty property,
      @NonNull ValueContext valueContext, @NonNull GraphEntity graphEntity) {
    String ldPathQuery =
        (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH);

    if (ldPathQuery == null && isSupportedLiteral(valueContext.getValue())) {
      return convertToDateTime(((Literal) valueContext.getValue()).calendarValue());
    }

    if (ldPathQuery == null) {
      throw new SchemaMapperRuntimeException(
          String.format("Property '%s' must have a '%s' attribute.", property.getName(),
              OpenApiSpecificationExtensions.LDPATH));
    }

    LdPathExecutor ldPathExecutor = graphEntity.getLdPathExecutor();
    Collection<Value> queryResult =
        ldPathExecutor.ldPathQuery(valueContext.getValue(), ldPathQuery);

    if (!property.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    Value dateTimeValue = getSingleStatement(queryResult, ldPathQuery);
    try {
      return convertToDateTime(((Literal) dateTimeValue).calendarValue());
    } catch (IllegalArgumentException iae) {
      throw new SchemaMapperRuntimeException(String.format(
          "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>",
          ldPathQuery, XMLSchema.DATETIME.stringValue()));
    }
  }

  private LocalDateTime convertToDateTime(XMLGregorianCalendar dateTimeValue) {
    return dateTimeValue.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof DateTimeProperty;
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

}
