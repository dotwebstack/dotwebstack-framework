package org.dotwebstack.framework.frontend.openapi.handlers;

import java.util.Collection;
import java.util.Map;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.filter.Filter;
import org.dotwebstack.framework.informationproduct.AbstractInformationProduct;
import org.eclipse.rdf4j.model.IRI;

public final class TestInformationProduct extends AbstractInformationProduct {

  public TestInformationProduct(IRI identifier, String label, ResultType resultType,
      Collection<Filter> requiredFilters, Collection<Filter> optionalFilters) {
    super(identifier, label, resultType, requiredFilters, optionalFilters);
  }

  @Override
  public Object getInnerResult(Map<String, String> values) {
    throw new UnsupportedOperationException(
        "getInnerResult() method unsupported, mock the InformationProduct interface instead");
  }

}
