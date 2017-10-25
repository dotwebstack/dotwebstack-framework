package org.dotwebstack.framework.frontend.openapi.handlers;

import java.util.Collection;
import java.util.Map;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.filter.Filter;
import org.dotwebstack.framework.informationproduct.AbstractInformationProduct;
import org.eclipse.rdf4j.model.IRI;

public final class TestInformationProduct extends AbstractInformationProduct {

  public TestInformationProduct(IRI identifier, String label, ResultType resultType,
      Collection<Filter> filters) {
    super(identifier, label, resultType, filters);
  }

  @Override
  public Object getResult(Map<String, String> values) {
    throw new UnsupportedOperationException(
        "getResult() method unsupported, mock the InformationProduct interface instead");
  }

}
