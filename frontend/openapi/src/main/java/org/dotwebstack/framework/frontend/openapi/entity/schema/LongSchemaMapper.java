package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.Property;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
public class LongSchemaMapper extends AbstractSchemaMapper<LongProperty, Long> {

  private static final Set<IRI> SUPPORTED_TYPES =
      ImmutableSet.of(XMLSchema.DOUBLE, XMLSchema.DOUBLE);
  private static final Set<String> SUPPORTED_VENDOR_EXTENSIONS = ImmutableSet.of(
      OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.CONSTANT_VALUE);

  @Override
  protected Set<String> getSupportedVendorExtensions() {
    return SUPPORTED_VENDOR_EXTENSIONS;
  }

  @Override

  public Long mapTupleValue(@NonNull LongProperty schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).longValue();
  }

  @Override
  public Long mapGraphValue(@NonNull LongProperty schema, @NonNull GraphEntity graphEntity,
      @NotNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).longValue();
  }

  @Override
  protected Long convertLiteralToType(Literal literal) {
    return literal.longValue();
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof LongProperty;
  }

}
