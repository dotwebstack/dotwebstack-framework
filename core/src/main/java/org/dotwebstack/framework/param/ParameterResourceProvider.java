package org.dotwebstack.framework.param;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.shapes.StringPropertyShape;
import org.dotwebstack.framework.param.types.TermParameter;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParameterResourceProvider extends AbstractResourceProvider<ParameterDefinition> {

  private final PropertyShape defaultPropertyShape = new StringPropertyShape();

  private List<ParameterResourceFactory> parameterResourceFactories = new ArrayList<>();

  @Autowired
  public ParameterResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties,
      @NonNull List<ParameterResourceFactory> parameterResourceFactories) {
    super(configurationBackend, applicationProperties);
    this.parameterResourceFactories = parameterResourceFactories;
  }

  @Override
  protected GraphQuery getQueryForResources(@NonNull RepositoryConnection conn) {
    String query =
        "PREFIX elmo: <http://dotwebstack.org/def/elmo#> CONSTRUCT { ?s ?p ?o . ?os ?op ?oo . } WHERE { ?s a ?type . ?s ?p ?o . OPTIONAL { ?s elmo:shape ?os . ?os ?op ?oo} } ";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.TERM_FILTER);
    return graphQuery;
  }

  @Override
  protected ParameterDefinition createResource(@NonNull Model model, @NonNull IRI identifier) {

    IRI backendType = getObjectIRI(model, identifier, RDF.TYPE).orElseThrow(
        () -> new ConfigurationException(String.format(
            "No <%s> statement has been found for backend <%s>.", RDF.TYPE, identifier)));

    return parameterResourceFactories.stream().filter(p -> p.supports(backendType)).map(
        parameterResourceFactory -> parameterResourceFactory.create(model,
            identifier)).findFirst().orElseThrow(
                () -> new ConfigurationException("No factory supplied resource"));

  }

  public Collection<Parameter> createParameters(Model model, IRI identifier) {

    ImmutableList.Builder<Parameter> builder = ImmutableList.builder();
    Set<IRI> requiredParameterIds =
        Models.objectIRIs(model.filter(identifier, ELMO.REQUIRED_PARAMETER_PROP, null));
    Set<IRI> optionalParameterIds =
        Models.objectIRIs(model.filter(identifier, ELMO.OPTIONAL_PARAMETER_PROP, null));

    requiredParameterIds.stream().map(this::get).map(d -> createTermParameter(d, true)).forEach(
        builder::add);
    optionalParameterIds.stream().map(this::get).map(d -> createTermParameter(d, false)).forEach(
        builder::add);

    return builder.build();
  }

  private AbstractParameter<?> createTermParameter(ParameterDefinition d, boolean required) {
    PropertyShape propertyShape =
        ((TermParameterDefinition) d).getShapeType().orElse(defaultPropertyShape);

    if (propertyShape.getTermClass().getConstructors().length == 1) {
      Constructor constructor = propertyShape.getTermClass().getConstructors()[0];
      try {
        return (AbstractParameter<?>) constructor.newInstance(d.getIdentifier(), d.getName(),
            required);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new ConfigurationException("Cannot create TermParameter");
      }
    }

    return new TermParameter(d.getIdentifier(), d.getName(), required);
  }
}
