package org.dotwebstack.framework.informationproduct;

import com.google.common.collect.ImmutableList;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.ParameterDefinition;
import org.dotwebstack.framework.param.ParameterDefinitionResourceProvider;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InformationProductResourceProvider
    extends AbstractResourceProvider<InformationProduct> {

  private final BackendResourceProvider backendResourceProvider;

  private final ParameterDefinitionResourceProvider parameterDefinitionResourceProvider;

  @Autowired
  public InformationProductResourceProvider(ConfigurationBackend configurationBackend,
      @NonNull BackendResourceProvider backendResourceProvider,
      @NonNull ParameterDefinitionResourceProvider parameterDefinitionResourceProvider,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);

    this.backendResourceProvider = backendResourceProvider;
    this.parameterDefinitionResourceProvider = parameterDefinitionResourceProvider;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.INFORMATION_PRODUCT);

    return graphQuery;
  }

  @Override
  protected InformationProduct createResource(Model model, Resource identifier) {
    IRI backendIRI =
        Models.objectIRI(model.filter(identifier, ELMO.BACKEND_PROP, null)).orElseThrow(
            () -> new ConfigurationException(
                String.format("No <%s> statement has been found for information product <%s>.",
                    ELMO.BACKEND_PROP, identifier)));
    Set<IRI> requiredParameterIds =
        Models.objectIRIs(model.filter(identifier, ELMO.REQUIRED_PARAMETER_PROP, null));
    Set<IRI> optionalParameterIds =
        Models.objectIRIs(model.filter(identifier, ELMO.OPTIONAL_PARAMETER_PROP, null));

    String label = getObjectString(model, identifier, RDFS.LABEL).orElse(null);

    return create(backendIRI, requiredParameterIds, optionalParameterIds, identifier, label, model);
  }

  private InformationProduct create(IRI backendIdentifier, Set<IRI> requiredParameterIds,
      Set<IRI> optionalParameterIds, Resource identifier, String label, Model statements) {
    Backend backend = backendResourceProvider.get(backendIdentifier);

    ImmutableList.Builder<Parameter> builder = ImmutableList.builder();

    requiredParameterIds.stream().map(parameterDefinitionResourceProvider::get).map(
        ParameterDefinition::createRequiredParameter).forEach(builder::add);
    optionalParameterIds.stream().map(parameterDefinitionResourceProvider::get).map(
        ParameterDefinition::createOptionalParameter).forEach(builder::add);

    return backend.createInformationProduct(identifier, label, builder.build(), statements);
  }

}
