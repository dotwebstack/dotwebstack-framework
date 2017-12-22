package org.dotwebstack.framework.param;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.shapes.IntegerPropertyShape;
import org.dotwebstack.framework.param.shapes.StringPropertyShape;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParameterResourceProvider extends AbstractResourceProvider<ParameterDefinition> {

  private Set<PropertyShape> supportedShapes = new HashSet<>();

  @Autowired
  public ParameterResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
    supportedShapes.add(new StringPropertyShape());
    supportedShapes.add(new IntegerPropertyShape());

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
    String name = getObjectString(model, identifier, ELMO.NAME_PROP).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> property found for <%s> of type <%s>", ELMO.NAME_PROP,
                identifier, ELMO.TERM_FILTER)));

    Set<Value> iriShapeTypes =
        model.filter((org.eclipse.rdf4j.model.Resource) model.filter(identifier, ELMO.SHAPE_PROP,
            null).objects().iterator().next(), null, null).objects();

    Optional<PropertyShape> propertyShapeOptional = supportedShapes.stream().filter(
        propertyShape -> iriShapeTypes.iterator().next().stringValue().equals(
            propertyShape.getDataType())).findFirst();

    return new ParameterDefinition(identifier, name, propertyShapeOptional);
  }

}
