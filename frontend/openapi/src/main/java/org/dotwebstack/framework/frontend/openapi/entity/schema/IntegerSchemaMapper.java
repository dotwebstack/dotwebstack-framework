package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class IntegerSchemaMapper extends AbstractSchemaMapper<IntegerSchema, Object> {

  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.INTEGER, XMLSchema.INT,
      XMLSchema.POSITIVE_INTEGER, XMLSchema.NON_NEGATIVE_INTEGER, XMLSchema.NON_POSITIVE_INTEGER,
      XMLSchema.NEGATIVE_INTEGER, XMLSchema.UNSIGNED_INT);
  private static final Set<String> SUPPORTED_VENDOR_EXTENSIONS = ImmutableSet.of(
      OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.CONSTANT_VALUE);

  @Override
  protected Set<String> getSupportedVendorExtensions() {
    return SUPPORTED_VENDOR_EXTENSIONS;
  }

  @Override
  protected Integer convertLiteralToType(Literal literal) {
    return literal.intValue();
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

  @Override
  public boolean supports(@NonNull Schema schema) {
    return schema instanceof IntegerSchema && schema.getFormat().equals("int32");
  }

}
