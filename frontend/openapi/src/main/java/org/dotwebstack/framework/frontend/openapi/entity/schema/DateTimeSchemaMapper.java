package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.Property;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class DateTimeSchemaMapper extends AbstractSchemaMapper<DateTimeProperty, LocalDateTime> {
  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.DATETIME);

  // XXX: Mag de input null zijn? En is public de juiste accessibility?
  @Override
  public String expectedException(String ldPathQuery) {
    return String.format(
        "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>",
        ldPathQuery, XMLSchema.DATETIME.stringValue());
  }

  // XXX: Wat je doet in de DateSchemaMapper werkt niet voor DateTime?
  @Override
  LocalDateTime convertToType(Literal literal) {
    return literal.calendarValue().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
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
