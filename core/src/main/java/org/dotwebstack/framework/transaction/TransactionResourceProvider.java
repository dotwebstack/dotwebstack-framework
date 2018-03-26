package org.dotwebstack.framework.transaction;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.ParameterDefinition;
import org.dotwebstack.framework.param.ParameterDefinitionResourceProvider;
import org.dotwebstack.framework.transaction.flow.FlowFactory;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionResourceProvider extends AbstractResourceProvider<Transaction> {

  private List<FlowFactory> flowFactories;

  private ParameterDefinitionResourceProvider parameterDefinitionResourceProvider;

  @Autowired
  public TransactionResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties, @NonNull List<FlowFactory> flowFactories,
      @NonNull ParameterDefinitionResourceProvider parameterDefinitionResourceProvider) {
    super(configurationBackend, applicationProperties);
    this.flowFactories = flowFactories;
    this.parameterDefinitionResourceProvider = parameterDefinitionResourceProvider;
  }

  @Override
  protected GraphQuery getQueryForResources(@NonNull RepositoryConnection conn) {
    final String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";

    final GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.TRANSACTION);

    return graphQuery;
  }

  @Override
  protected Transaction createResource(@NonNull Model model, @NonNull Resource identifier) {

    for (IRI predicate : getPredicateIris(model, identifier)) {
      for (FlowFactory flowFactory : flowFactories) {
        if (flowFactory.supports(predicate)) {
          final Transaction.Builder transactionBuilder = new Transaction.Builder(identifier);
          getObjectResource(model, identifier, predicate).ifPresent(
              flowIndentifier -> transactionBuilder.flow(flowFactory.getResource(flowIndentifier)));

          Set<IRI> requiredParameterIds =
              Models.objectIRIs(model.filter(identifier, ELMO.REQUIRED_PARAMETER_PROP, null));
          Set<IRI> optionalParameterIds =
              Models.objectIRIs(model.filter(identifier, ELMO.OPTIONAL_PARAMETER_PROP, null));

          ImmutableList.Builder<Parameter> parameterBuilder = ImmutableList.builder();

          requiredParameterIds.stream().map(parameterDefinitionResourceProvider::get).map(
              ParameterDefinition::createRequiredParameter).forEach(parameterBuilder::add);
          optionalParameterIds.stream().map(parameterDefinitionResourceProvider::get).map(
              ParameterDefinition::createOptionalParameter).forEach(parameterBuilder::add);

          return transactionBuilder.parameters(parameterBuilder.build()).build();
        }
      }
    }

    throw new ConfigurationException(
        String.format("No flow statement has been found for transaction <%s>.", identifier));
  }

}
