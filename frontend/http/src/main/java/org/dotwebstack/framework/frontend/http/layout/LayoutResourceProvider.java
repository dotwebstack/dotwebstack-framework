package org.dotwebstack.framework.frontend.http.layout;

import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.dotwebstack.framework.vocabulary.Xhtml;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
    final String cssResourceLocation =
        getObjectString(model, identifier, Xhtml.STYLESHEET).orElseThrow(
            () -> new ConfigurationException(
                String.format("No file location has been found for " + "layout <%s>",
                    Xhtml.STYLESHEET, identifier)));
    try {
      final Resource cssResource = new ClassPathResource(cssResourceLocation);
      Layout.Builder builder = new Layout.Builder(identifier, cssResource);
      // getObjectString of label
      return builder.build();
    } catch (Exception ex) {
      LOG.error(ex.toString());
      throw new ConfigurationException(
          String.format("No css file has been found for layout <%s> at location <%s>",
              ELMO.LAYOUT_PROP, identifier, cssResourceLocation));
    }
  }
}
