package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.Property;
import java.time.LocalDate;
import java.util.Set;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class DateSchemaMapper extends AbstractSchemaMapper<DateProperty, LocalDate> {
  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.DATE);

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
