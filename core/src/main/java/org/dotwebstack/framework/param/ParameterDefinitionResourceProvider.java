package org.dotwebstack.framework.param;

import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParameterDefinitionResourceProvider
    extends AbstractResourceProvider<ParameterDefinition> {

  private final List<ParameterDefinitionFactory> factories;

  @Autowired
  public ParameterDefinitionResourceProvider(@NonNull ConfigurationBackend configurationBackend,
      @NonNull ApplicationProperties applicationProperties,
      @NonNull List<ParameterDefinitionFactory> factories) {
    super(configurationBackend, applicationProperties);

    this.factories = factories;
  }

  @Override
  protected GraphQuery getQueryForResources(@NonNull RepositoryConnection connection) {
    String query = "PREFIX elmo: <http://dotwebstack.org/def/elmo#> "
        + "CONSTRUCT { ?s ?p ?o . ?o ?op ?oo . } "
        + "WHERE { ?s a ?type . ?type rdfs:subClassOf ?parameter . ?s ?p ?o . "
        + "OPTIONAL { ?o ?op ?oo } } ";

    GraphQuery graphQuery = connection.prepareGraphQuery(query);

    graphQuery.setBinding("parameter", ELMO.PARAMETER);

    return graphQuery;
  }

  @Override
  protected ParameterDefinition createResource(@NonNull Model model, @NonNull Resource id) {
    if (id instanceof BNode) {
      return null;
    }
    IRI type = getObjectIRI(model, id, RDF.TYPE).orElseThrow(() -> new ConfigurationException(
        String.format("No <%s> statement has been found for parameter <%s>.", RDF.TYPE, id)));
    for (ParameterDefinitionFactory factory : factories) {
      if (factory.supports(type)) {
        return factory.create(model, id);
      }
    }
    return null;
  }

}
