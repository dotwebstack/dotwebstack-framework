package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.BindingSet;

public final class BindingSetFetcher implements DataFetcher<Object> {

  @Override
  public Object get(DataFetchingEnvironment environment) {
    BindingSet bindingSet = environment.getSource();
    String propertyName = environment.getFieldDefinition().getName();

    if (bindingSet == null || !bindingSet.hasBinding(propertyName)) {
      return null;
    }

    Value bindingValue = bindingSet.getValue(propertyName);

    if (!(bindingValue instanceof Literal)) {
      return bindingValue.stringValue();
    }

    IRI dataType = ((Literal) bindingValue).getDatatype();

    // Convert to Java types so built-in scalar types can handle them
    if (XMLSchema.STRING.equals(dataType)) {
      return bindingValue.stringValue();
    } else if (XMLSchema.BOOLEAN.equals(dataType)) {
      return ((Literal) bindingValue).booleanValue();
    } else if (XMLSchema.INT.equals(dataType)) {
      return ((Literal) bindingValue).intValue();
    } else if (XMLSchema.INTEGER.equals(dataType)) {
      return ((Literal) bindingValue).integerValue();
    } else if (XMLSchema.SHORT.equals(dataType)) {
      return ((Literal) bindingValue).shortValue();
    } else if (XMLSchema.LONG.equals(dataType)) {
      return ((Literal) bindingValue).longValue();
    } else if (XMLSchema.FLOAT.equals(dataType)) {
      return ((Literal) bindingValue).floatValue();
    } else if (XMLSchema.DOUBLE.equals(dataType)) {
      return ((Literal) bindingValue).doubleValue();
    } else if (XMLSchema.DECIMAL.equals(dataType)) {
      return ((Literal) bindingValue).decimalValue();
    } else if (XMLSchema.BYTE.equals(dataType)) {
      return ((Literal) bindingValue).byteValue();
    }

    return bindingValue;
  }

}
