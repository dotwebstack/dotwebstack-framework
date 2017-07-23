package org.dotwebstack.framework.product.config;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.PostConstruct;
import org.dotwebstack.framework.product.Product;
import org.dotwebstack.framework.product.ProductRegistry;
import org.dotwebstack.framework.product.Source;
import org.dotwebstack.framework.product.vocabulary.ELMO;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

@Component
public class ProductConfigurationLoader implements ResourceLoaderAware {

  private static final Logger logger = LoggerFactory.getLogger(ProductConfigurationLoader.class);

  private ProductProperties productProperties;

  private ProductRegistry productRegistry;

  private ResourceLoader resourceLoader;

  @Autowired
  public ProductConfigurationLoader(ProductProperties productProperties,
      ProductRegistry productRegistry) {
    this.productProperties = productProperties;
    this.productRegistry = productRegistry;
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void loadConfiguration() throws IOException {
    Resource[] resources =
        ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(
            String.format("classpath:%s/*.ttl", productProperties.getConfigPath()));

    if (resources.length == 0) {
      logger.info("No product configuration files found.");
      return;
    }

    Model productConfigurationModel = new LinkedHashModel();
    RDFParser turtleParser = Rio.createParser(RDFFormat.TURTLE);
    turtleParser.setRDFHandler(new StatementCollector(productConfigurationModel));

    for (Resource configResource : resources) {
      InputStream configResourceStream = configResource.getInputStream();

      try {
        turtleParser.parse(configResourceStream, "#");
      } catch (RDF4JException e) {
        throw new ProductConfigurationException(e.getMessage());
      } finally {
        configResourceStream.close();
      }

      logger.info("Loaded configuration file \"%s\".", configResource.getFilename());
    }

    for (Statement typeStatement : productConfigurationModel.filter(null, RDF.TYPE, ELMO.PRODUCT)) {
      Product product = createProductFromModel((IRI) typeStatement.getSubject());

      productRegistry.registerProduct(product);

      logger.debug("Loaded product \"%s\".", product.getIdentifier());
    }
  }

  private Product createProductFromModel(IRI identifier) {
    return new Product(identifier, new Source() {});
  }

}
