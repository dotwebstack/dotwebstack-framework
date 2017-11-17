package org.dotwebstack.framework.frontend.openapi.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.Property;
import java.time.LocalDateTime;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
class DateSchemaMapper extends AbstractSchemaMapper
    implements SchemaMapper<DateProperty, LocalDateTime> {

  @Override
  public LocalDateTime mapTupleValue(@NonNull DateProperty schema, @NonNull Value value) {
    return convertToDateTime(SchemaMapperUtils.castLiteralValue(value).calendarValue());
  }

  @Override
  public LocalDateTime mapGraphValue(DateProperty schema, GraphEntityContext graphEntityContext,
      SchemaMapperAdapter schemaMapperAdapter, Value value) {
    return handle(schema, graphEntityContext, value);
  }


  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.DATE);

  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }


  public LocalDateTime handle(DateProperty property, GraphEntityContext entityBuilderContext,
      Value context) {

    String ldPathQuery =
        (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH);

    if (ldPathQuery == null && isLiteral(context)) {
      return ((Literal) context).calendarValue().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
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

    return convertToDateTime(((Literal) dateValue).calendarValue());
  }

  private LocalDateTime convertToDateTime(XMLGregorianCalendar dateValue) {

    return dateValue.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
  }


  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof BooleanProperty;
  }

}
