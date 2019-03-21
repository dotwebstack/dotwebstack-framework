package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jBackendException;
import org.dotwebstack.framework.backend.rdf4j.types.LiteralConverter;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class BindingSetFetcher implements DataFetcher<Object> {

  private final List<LiteralConverter> literalConverters;

  @Override
  public Object get(DataFetchingEnvironment environment) {
    BindingSet bindingSet = environment.getSource();
    String propertyName = environment.getFieldDefinition().getName();

    if (bindingSet == null || !bindingSet.hasBinding(propertyName)) {
      return null;
    }

    Value bindingValue = bindingSet.getValue(propertyName);

    if (bindingValue instanceof Literal) {
      return convertLiteral((Literal) bindingValue);
    }

    return bindingSet.getValue(propertyName).stringValue();
  }

  private Object convertLiteral(Literal value) {
    LiteralConverter literalConverter = literalConverters
        .stream()
        .filter(converter -> converter.supports(value.getDatatype()))
        .findFirst()
        .orElseThrow(() -> new Rdf4jBackendException(String
            .format("No literal converter found for datatype '%s'.",
                value.getDatatype().stringValue())));

    return literalConverter.convert(value);
  }

}
