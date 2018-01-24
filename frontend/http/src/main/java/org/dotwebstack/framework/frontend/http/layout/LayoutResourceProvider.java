package org.dotwebstack.framework.frontend.http.layout;

import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LayoutResourceProvider extends AbstractResourceProvider<Layout> {

  private static final Logger LOG = LoggerFactory.getLogger(LayoutResourceProvider.class);

  @Autowired
  public LayoutResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.LAYOUT);

    return graphQuery;
  }

  @Override
  protected Layout createResource(Model model, IRI identifier) {
    final String cssResource = getObjectString(model, identifier, XHTML.STYLESHEET).orElseThrow(
        () -> new ConfigurationException(String.format(
            "No file location has been found for " + "layout <%s>", XHTML.STYLESHEET, identifier)));
    Layout.Builder builder = new Layout.Builder(identifier);
    builder.addOption(XHTML.STYLESHEET, cssResource);
    return builder.build();
  }
}
