package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.Property;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class DoubleSchemaMapper extends AbstractSchemaMapper<DoubleProperty, Double> {

  private static final Set<IRI> SUPPORTED_TYPES =
      ImmutableSet.of(XMLSchema.DOUBLE, XMLSchema.DOUBLE);
  private static final Set<String> SUPPORTED_VENDOR_EXTENSIONS = ImmutableSet.of(
          OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.CONSTANT_VALUE);

  @Override
  protected Set<String> getSupportedVendorExtensions() {
    return SUPPORTED_VENDOR_EXTENSIONS;
  }

  @Override
  String expectedException(String ldPathQuery) {
    return String.format(
            "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>",
            ldPathQuery, XMLSchema.DOUBLE.stringValue());
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof DoubleProperty;
  }

  @Override
  Double convertToType(Literal literal) {
    return literal.doubleValue();
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }
}
