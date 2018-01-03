package org.dotwebstack.framework.informationproduct;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.AbstractParameter;
import org.dotwebstack.framework.param.GeometryDefinition;
import org.dotwebstack.framework.param.GeometryResourceProvider;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.ParameterDefinition;
import org.dotwebstack.framework.param.ParameterResourceProvider;
import org.dotwebstack.framework.param.PropertyShape;
import org.dotwebstack.framework.param.shapes.StringPropertyShape;
import org.dotwebstack.framework.param.types.GeometryParameter;
import org.dotwebstack.framework.param.types.TermParameter;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
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

  private final ParameterResourceProvider parameterResourceProvider;

  private final GeometryResourceProvider geometryResourceProvider;

  private final PropertyShape defaultPropertyShape = new StringPropertyShape();

  @Autowired
  public InformationProductResourceProvider(ConfigurationBackend configurationBackend,
      @NonNull BackendResourceProvider backendResourceProvider,
      @NonNull ParameterResourceProvider parameterResourceProvider,
      @NonNull GeometryResourceProvider geometryResourceProvider,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
    this.backendResourceProvider = backendResourceProvider;
    this.parameterResourceProvider = parameterResourceProvider;
    this.geometryResourceProvider = geometryResourceProvider;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.INFORMATION_PRODUCT);

    return graphQuery;
  }

  @Override
  protected InformationProduct createResource(Model model, IRI identifier) {
    IRI backendIRI =
        Models.objectIRI(model.filter(identifier, ELMO.BACKEND_PROP, null)).orElseThrow(
            () -> new ConfigurationException(
                String.format("No <%s> statement has been found for information product <%s>.",
                    ELMO.BACKEND_PROP, identifier)));
    Set<IRI> requiredParameterIds =
        Models.objectIRIs(model.filter(identifier, ELMO.REQUIRED_PARAMETER_PROP, null));
    Set<IRI> optionalParameterIds =
        Models.objectIRIs(model.filter(identifier, ELMO.OPTIONAL_PARAMETER_PROP, null));

    Set<IRI> geoFilters = Models.objectIRIs(model.filter(identifier, ELMO.GEOMETRY_FILTER, null));


    String label = getObjectString(model, identifier, RDFS.LABEL).orElse(null);

    return create(backendIRI, requiredParameterIds, optionalParameterIds, identifier, label, model,
        geoFilters);
  }

  private InformationProduct create(IRI backendIdentifier, Set<IRI> requiredParameterIds,
      Set<IRI> optionalParameterIds, IRI identifier, String label, Model statements,
      Set<IRI> geoFilters) {
    Backend backend = backendResourceProvider.get(backendIdentifier);

    ImmutableList.Builder<Parameter> builder = ImmutableList.builder();

    requiredParameterIds.stream().map(parameterResourceProvider::get).map(
        d -> createTermParameter(d, true)).forEach(builder::add);
    optionalParameterIds.stream().map(parameterResourceProvider::get).map(
        d -> createTermParameter(d, false)).forEach(builder::add);

    geoFilters.stream().map(geometryResourceProvider::get).map(d -> createGeoFilter(d)).forEach(
        builder::add);
    return backend.createInformationProduct(identifier, label, builder.build(), statements);
  }

  private AbstractParameter<?> createGeoFilter(GeometryDefinition d) {
    return new GeometryParameter(d.getIdentifier(), d.getName());
  }

  private AbstractParameter<?> createTermParameter(ParameterDefinition d, boolean required) {
    PropertyShape propertyShape = d.getShapeTypes().orElse(defaultPropertyShape);

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
