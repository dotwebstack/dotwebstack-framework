package org.dotwebstack.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FilenameUtils;
import org.dotwebstack.framework.Product;
import org.dotwebstack.framework.ProductRegistry;
import org.dotwebstack.framework.Source;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
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
            String.format("classpath:%s/**", productProperties.getConfigPath()));

    if (resources.length == 0) {
      logger.info("No product configuration files found.");
      return;
    }

    Model productConfigurationModel = loadResources(resources);
    registerProducts(productConfigurationModel);
  }

  private void registerProducts(Model productConfigurationModel) {
    for (Statement typeStatement : productConfigurationModel.filter(null, RDF.TYPE, ELMO.PRODUCT)) {
      Product product = createProductFromModel((IRI) typeStatement.getSubject());

      productRegistry.registerProduct(product);

      logger.debug("Loaded product \"%s\".", product.getIdentifier());
    }
  }

  private Model loadResources(Resource[] resources) throws IOException {
    Map<String, RDFFormat> fileFormats = getFileFormats();

    Model productConfigurationModel = new LinkedHashModel();

    for (Resource configResource : resources) {
      InputStream configResourceStream = configResource.getInputStream();
      String extension = FilenameUtils.getExtension(configResource.getFilename());

      if (!fileFormats.containsKey(extension)) {
        logger.debug("File extension not supported, ignoring file: \"%s\"",
            configResource.getFilename());
        continue;
      }

      try {
        Model model = Rio.parse(configResourceStream, ELMO.NAMESPACE, fileFormats.get(extension));
        productConfigurationModel.addAll(model);
      } catch (RDFParseException ex) {
        throw new ProductConfigurationException(ex.getMessage(), ex);
      }
    }
    return productConfigurationModel;
  }

  private Product createProductFromModel(IRI identifier) {
    return new Product(identifier, new Source() {
    });
  }

  private Map<String, RDFFormat> getFileFormats() {
    Map<String, RDFFormat> formatMap = new HashMap<>();
    formatMap.put("ttl", RDFFormat.TURTLE);
    formatMap.put("xml", RDFFormat.RDFXML);
    formatMap.put("json", RDFFormat.RDFJSON);
    return formatMap;
  }
}
