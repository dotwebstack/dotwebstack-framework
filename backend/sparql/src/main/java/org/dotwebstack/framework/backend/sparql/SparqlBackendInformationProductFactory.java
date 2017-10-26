package org.dotwebstack.framework.backend.sparql;

import static org.dotwebstack.framework.vocabulary.ELMO.RESULT_TYPE_DEFAULT;

import java.util.Collection;
import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.filter.Filter;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SparqlBackendInformationProductFactory {

  private QueryEvaluator queryEvaluator;

  @Autowired
  public SparqlBackendInformationProductFactory(@NonNull QueryEvaluator queryEvaluator) {
    this.queryEvaluator = queryEvaluator;
  }

  public InformationProduct create(IRI identifier, String label, Backend backend,
      Collection<Filter> filters, Model statements) {
    String query = getQuery(identifier, statements);

    ResultType resultType = getResultType(identifier, statements);

    return new SparqlBackendInformationProduct.Builder(identifier, (SparqlBackend) backend, query,
        resultType, queryEvaluator, filters).label(label).build();
  }

  private String getQuery(IRI identifier, Model statements) {
    return Models.objectString(statements.filter(identifier, ELMO.QUERY, null)).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> statement has been found for a SPARQL information product <%s>.",
                ELMO.QUERY, identifier)));
  }

  private ResultType getResultType(IRI identifier, Model statements) {
    IRI resulttypeIri =
        Models.objectIRI(statements.filter(identifier, ELMO.RESULT_TYPE, null)).orElse(
            RESULT_TYPE_DEFAULT);
    return ResultType.valueOf(resulttypeIri.getLocalName().toUpperCase());
  }

}
