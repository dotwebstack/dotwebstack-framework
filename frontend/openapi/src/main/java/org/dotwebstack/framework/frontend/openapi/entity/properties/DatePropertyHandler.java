package org.dotwebstack.framework.frontend.openapi.entity.properties;

import com.google.common.collect.ImmutableSet;

import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.OasVendorExtensions;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
public class DatePropertyHandler extends AbstractPropertyHandler<DateProperty> {

  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.DATE);

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

  @Override
  public Object handle(DateProperty property, EntityBuilderContext entityBuilderContext,
      PropertyHandlerRegistry registry, Value context) {

    String ldPathQuery = (String) property.getVendorExtensions().get(OasVendorExtensions.LDPATH);

    if (ldPathQuery == null && isLiteral(context)) {
      return ((Literal) context).calendarValue();
    }

    if (ldPathQuery == null) {
      throw new PropertyHandlerRuntimeException(
          String.format("Property '%s' must have a '%s' attribute.", property.getName(),
              OasVendorExtensions.LDPATH));
    }

    LdPathExecutor ldPathExecutor = entityBuilderContext.getLdPathExecutor();
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(context, ldPathQuery);

    if (!property.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    Value dateValue = getSingleStatement(queryResult, ldPathQuery);

    if (!isLiteral(dateValue)) {
      throw new PropertyHandlerRuntimeException(String.format(
          "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>.",
          ldPathQuery, dataTypesAsString()));
    }

    return ((Literal) dateValue).calendarValue();
  }

  @Override
  public boolean supports(Property property) {
    return DateProperty.class.isInstance(property);
  }

}
