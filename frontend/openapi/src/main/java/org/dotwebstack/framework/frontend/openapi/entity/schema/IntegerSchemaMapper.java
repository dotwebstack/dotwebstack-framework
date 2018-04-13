package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.BaseIntegerProperty;
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
class IntegerSchemaMapper extends AbstractSchemaMapper<BaseIntegerProperty, Object> {

  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.INTEGER, XMLSchema.INT);
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
            ldPathQuery, XMLSchema.INTEGER.stringValue());
  }

  @Override
  Integer convertToType(Literal literal) {
    return literal.intValue();
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof BaseIntegerProperty;
  }

}
