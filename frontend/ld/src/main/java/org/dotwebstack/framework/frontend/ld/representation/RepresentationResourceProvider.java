package org.dotwebstack.framework.frontend.ld.representation;

import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;

public class RepresentationResourceProvider extends AbstractResourceProvider<Representation> {

  private InformationProductResourceProvider informationProductResourceProvider;

  private StageResourceProvider stageResourceProvider;

  @Autowired
  public RepresentationResourceProvider(ConfigurationBackend configurationBackend,
      InformationProductResourceProvider informationProductResourceProvider) {
    // [todo] add stageResourceProvider
    super(configurationBackend);
    this.informationProductResourceProvider = informationProductResourceProvider;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.REPRESENTATION);
    return graphQuery;
  }

  @Override
  protected Representation createResource(Model model, IRI identifier) {
    IRI informationProductIri = getObjectIRI(model, identifier, ELMO.INFORMATION_PRODUCT_PROP)
        .orElseThrow(() -> new ConfigurationException(
            String.format("No <%s> information product has been found for representation <%s>.",
                ELMO.INFORMATION_PRODUCT_PROP,
                identifier)));

    final String urlPattern;
    if (getObjectString(model, identifier, ELMO.URL_PATTERN).isPresent()) {
      urlPattern = getObjectString(model, identifier, ELMO.URL_PATTERN).get();
    } else {
      throw new ConfigurationException(String
          .format("No <%s> url pattern has been found for representation <%s>.", ELMO.URL_PATTERN,
              identifier));
    }
    Representation.Builder builder = new Representation.Builder(identifier,
        informationProductResourceProvider.get((informationProductIri)),
        urlPattern);
    return builder.build();
  }
}
