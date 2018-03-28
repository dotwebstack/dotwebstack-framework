package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.Property;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class DateSchemaMapper extends AbstractSchemaMapper<DateProperty, LocalDate> {
  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.DATE);

  @Override
  public LocalDate mapTupleValue(@NonNull DateProperty schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return convertToDate(
        SchemaMapperUtils.castLiteralValue(valueContext.getValue()).calendarValue());
  }

  @Override
  public LocalDate mapGraphValue(@NonNull DateProperty schema, @NonNull GraphEntity entity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    String ldPathQuery =
        (String) schema.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH);

    if (ldPathQuery == null && isSupportedLiteral(valueContext.getValue())) {
      return convertToDate(((Literal) valueContext.getValue()).calendarValue());
    }

    if (ldPathQuery == null) {
      throw new SchemaMapperRuntimeException(
          String.format("Property '%s' must have a '%s' attribute.", schema.getName(),
              OpenApiSpecificationExtensions.LDPATH));
    }

    LdPathExecutor ldPathExecutor = entity.getLdPathExecutor();
    Collection<Value> queryResult =
        ldPathExecutor.ldPathQuery(valueContext.getValue(), ldPathQuery);

    if (!schema.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    Value dateValue = getSingleStatement(queryResult, ldPathQuery);

    if (!isSupportedLiteral(dateValue)) {
      throw new SchemaMapperRuntimeException(String.format(
          "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>.",
          ldPathQuery, dataTypesAsString()));
    }

    return convertToDate(((Literal) dateValue).calendarValue());
  }

  private LocalDate convertToDate(XMLGregorianCalendar dateValue) {
    return dateValue.toGregorianCalendar().toZonedDateTime().toLocalDate();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof DateProperty;
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

}
