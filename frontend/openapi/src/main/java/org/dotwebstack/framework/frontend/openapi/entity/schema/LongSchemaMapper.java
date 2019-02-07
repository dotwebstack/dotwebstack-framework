package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
public class LongSchemaMapper extends AbstractSchemaMapper<IntegerSchema, Long> {

  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.LONG);
  private static final Set<String> SUPPORTED_VENDOR_EXTENSIONS = ImmutableSet.of(
      OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.CONSTANT_VALUE);

  @Override
  protected Set<String> getSupportedVendorExtensions() {
    return SUPPORTED_VENDOR_EXTENSIONS;
  }

  @Override
  public boolean supports(Schema schema) {
    return schema instanceof IntegerSchema && schema.getFormat().equals("int64");
  }

  @Override
  protected Long convertLiteralToType(Literal literal) {
    return literal.longValue();
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

}
