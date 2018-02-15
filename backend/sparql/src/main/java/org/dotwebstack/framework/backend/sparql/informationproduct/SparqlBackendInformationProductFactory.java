package org.dotwebstack.framework.backend.sparql.informationproduct;

import static org.dotwebstack.framework.vocabulary.ELMO.RESULT_TYPE_DEFAULT;

import java.util.Collection;
import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SparqlBackendInformationProductFactory {

  private final QueryEvaluator queryEvaluator;

  private final TemplateProcessor templateProcessor;

  @Autowired
  public SparqlBackendInformationProductFactory(@NonNull QueryEvaluator queryEvaluator,
      @NonNull TemplateProcessor templateProcessor) {
    this.queryEvaluator = queryEvaluator;
    this.templateProcessor = templateProcessor;
  }

  public InformationProduct create(Resource identifier, String label, Backend backend,
      Collection<Parameter> parameters, Model statements) {
    String query = getQuery(identifier, statements);

    ResultType resultType = getResultType(identifier, statements);

    return new SparqlBackendInformationProduct.Builder(identifier, (SparqlBackend) backend, query,
        resultType, queryEvaluator, templateProcessor, parameters).label(label).build();
  }

  private String getQuery(Resource identifier, Model statements) {
    return Models.objectString(statements.filter(identifier, ELMO.QUERY, null)).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> statement has been found for a SPARQL information product <%s>.",
                ELMO.QUERY, identifier)));
  }

  private ResultType getResultType(Resource identifier, Model statements) {
    IRI resultTypeIri =
        Models.objectIRI(statements.filter(identifier, ELMO.RESULT_TYPE, null)).orElse(
            RESULT_TYPE_DEFAULT);
    try {
      return ResultType.valueOf(resultTypeIri.getLocalName().toUpperCase());
    } catch (IllegalArgumentException illegalArgumentException) {
      throw new ConfigurationException(
          String.format("No resulttype found for <%s>.", resultTypeIri), illegalArgumentException);
    }
  }

}
