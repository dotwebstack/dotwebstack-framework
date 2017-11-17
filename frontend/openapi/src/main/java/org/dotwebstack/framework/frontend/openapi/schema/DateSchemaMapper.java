package org.dotwebstack.framework.frontend.openapi.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.Property;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class DateSchemaMapper extends AbstractSchemaMapper
    implements SchemaMapper<DateProperty, LocalDate> {
  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.DATE);

  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

  @Override
  public LocalDate mapTupleValue(@NonNull DateProperty schema, @NonNull Value value) {
    return convertToDate(SchemaMapperUtils.castLiteralValue(value).calendarValue());
  }

  @Override
  public LocalDate mapGraphValue(DateProperty schema, GraphEntityContext graphEntityContext,
                                 SchemaMapperAdapter schemaMapperAdapter, Value value) {
    return handle(schema, graphEntityContext, value);
  }

  LocalDate handle(DateProperty property, GraphEntityContext entityBuilderContext,
      Value context) {

    String ldPathQuery =
        (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH);

    if (ldPathQuery == null && isLiteral(context)) {
      return convertToDate(((Literal) context).calendarValue());
    }

    if (ldPathQuery == null) {
      throw new SchemaMapperRuntimeException(
          String.format("Property '%s' must have a '%s' attribute.", property.getName(),
              OpenApiSpecificationExtensions.LDPATH));
    }

    LdPathExecutor ldPathExecutor = entityBuilderContext.getLdPathExecutor();
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(context, ldPathQuery);

    if (!property.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    Value dateValue = getSingleStatement(queryResult, ldPathQuery);

    if (!isLiteral(dateValue)) {
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

}
