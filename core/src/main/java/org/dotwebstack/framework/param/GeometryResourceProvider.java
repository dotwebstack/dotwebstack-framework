package org.dotwebstack.framework.param;

import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeometryResourceProvider extends AbstractResourceProvider<GeometryDefinition> {

  @Autowired
  public GeometryResourceProvider(ConfigurationBackend configurationBackend,
                                  ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);

  }

  @Override
  protected GraphQuery getQueryForResources(@NonNull RepositoryConnection conn) {
    String query =
        "CONSTRUCT { ?s ?p ?o . } WHERE { ?s a ?type . ?s ?p ?o . } ";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);

    graphQuery.setBinding("type", ELMO.GEOMETRY_FILTER);
    return graphQuery;
  }

  @Override
  protected GeometryDefinition createResource(@NonNull Model model, @NonNull IRI identifier) {
    return createGeometryFilter(model,identifier);
  }

  private GeometryDefinition createGeometryFilter(Model model, IRI identifier) {
    String name = getObjectString(model, identifier, ELMO.NAME_PROP).orElseThrow(
            () -> new ConfigurationException(
                    String.format("No <%s> property found for <%s> of type <%s>", ELMO.NAME_PROP,
                            identifier, ELMO.GEOMETRY_FILTER)));

    return new GeometryDefinition(identifier, name);
  }



}
