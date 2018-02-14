package org.dotwebstack.framework.frontend.ld.parameter;

import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParameterMapperResourceProvider extends AbstractResourceProvider<ParameterMapper> {

  private ParameterMapperFactory parameterMapperFactory;

  @Autowired
  public ParameterMapperResourceProvider(ConfigurationBackend configurationBackend,
      @NonNull ParameterMapperFactory parameterMapperFactory,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
    this.parameterMapperFactory = parameterMapperFactory;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o. ?rep ?parametermapper ?s. }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("parametermapper", ELMO.PARAMETER_MAPPER_PROP);

    return graphQuery;
  }

  @Override
  protected ParameterMapper createResource(Model model, Resource identifier) {
    IRI parameterMapperType = getObjectIRI(model, identifier, RDF.TYPE).orElseThrow(
        () -> new ConfigurationException(String.format(
            "No <%s> statement has been found for parametermapper <%s>.", RDF.TYPE, identifier)));

    return parameterMapperFactory.create(parameterMapperType, model, identifier);
  }

}
