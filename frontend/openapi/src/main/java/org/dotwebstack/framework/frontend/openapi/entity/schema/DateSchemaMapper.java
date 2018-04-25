package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.Property;
import java.time.LocalDate;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class DateSchemaMapper extends AbstractSchemaMapper<DateProperty, LocalDate> {
  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.DATE);

  private static final Set<String> SUPPORTED_VENDOR_EXTENSIONS = ImmutableSet.of(
      OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.CONSTANT_VALUE);

  @Override
  protected Set<String> getSupportedVendorExtensions() {
    return SUPPORTED_VENDOR_EXTENSIONS;
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof DateProperty;
  }

  @Override
  protected LocalDate convertLiteralToType(Literal literal) {
    return LocalDate.parse(literal.stringValue());
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }
}
