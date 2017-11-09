package org.dotwebstack.framework.frontend.openapi.handlers;

import java.util.Collection;
import java.util.Map;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.informationproduct.AbstractInformationProduct;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.IRI;

public final class TestInformationProduct extends AbstractInformationProduct {

  public TestInformationProduct(IRI identifier, String label, ResultType resultType,
      Collection<Parameter> requiredParameters, Collection<Parameter> optionalParameters,
      TemplateProcessor templateProcessor) {
    super(identifier, label, resultType, requiredParameters, optionalParameters, templateProcessor);
  }

  @Override
  public Object getInnerResult(Map<String, Object> parameterValues) {
    throw new UnsupportedOperationException(
        "getInnerResult() method unsupported, mock the InformationProduct interface instead");
  }

}
