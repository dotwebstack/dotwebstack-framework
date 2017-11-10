package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.Swagger;
import java.util.Collection;
import java.util.Map;
import org.apache.marmotta.ldpath.LDPath;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.dotwebstack.framework.frontend.openapi.entity.backend.Rdf4jRepositoryBackend;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.OasVendorExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.properties.PropertyHandlerRuntimeException;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class LdPathExecutor {

  private LDPath<Value> ldpath;
  private final ImmutableMap<String, String> ldpathNamespaces;

  public LdPathExecutor(final EntityBuilderContext context) {
    this.ldpathNamespaces = extractLdpathNamespaces(context.getSwagger());
    this.ldpath = createLdpath(context.getQueryResult());

  }

  private ImmutableMap<String, String> getDefaultNamespaces() {

    return ImmutableMap.<String, String>builder().put("nen3610",
        "http://data.informatiehuisruimte.nl/def/nen3610#").put("skos",
            "http://www.w3.org/2004/02/skos/core#").put("dcterms", "http://purl.org/dc/terms/").put(
                "ro", "http://data.informatiehuisruimte.nl/def/ro#").put("pdok",
                    "http://data.pdok.nl/def/pdok#").put("geo",
                        "http://www.opengis.net/ont/geosparql#").put("ro_beg",
                            "http://data.informatiehuisruimte.nl/ro/id/begrip").build();

  }

  @SuppressWarnings("unchecked")
  private ImmutableMap<String, String> extractLdpathNamespaces(Swagger swagger) {
    if (swagger == null) {
      return getDefaultNamespaces();
    }
    Map<String, Object> extensions = swagger.getVendorExtensions();
    ImmutableMap<String, Object> vendorExtensions =
        extensions == null ? ImmutableMap.of() : ImmutableMap.copyOf(extensions);
    if (vendorExtensions.containsKey(OasVendorExtensions.LDPATH_NAMESPACES)) {
      Object ldPathNamespaces = vendorExtensions.get(OasVendorExtensions.LDPATH_NAMESPACES);
      try {
        return ImmutableMap.copyOf((Map<String, String>) ldPathNamespaces);
      } catch (ClassCastException cce) {
        throw new PropertyHandlerRuntimeException(String.format(
            "Vendor extension '%s' should contain a map of namespaces (eg. "
                + "{ \"rdfs\": \"http://www.w3.org/2000/01/rdf-schema#\", "
                + "\"rdf\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"})",
            OasVendorExtensions.LDPATH_NAMESPACES), cce);
      }
    }

    return null;
  }

  private LDPath<Value> createLdpath(QueryResult queryResult) {
    Repository repository = new SailRepository(new MemoryStore());
    Rdf4jRepositoryBackend repositoryBackend;


    try {
      repository.initialize();
      repositoryBackend = new Rdf4jRepositoryBackend(repository);

      final RepositoryConnection connection = repository.getConnection();
      connection.add(queryResult.getModel());
      connection.close();
    } catch (RepositoryException re) {
      throw new PropertyHandlerRuntimeException("Unable to initialize RDF4JRepository.", re);
    }

    return new LDPath<>(repositoryBackend);
  }

  public Collection<Value> ldPathQuery(Value context, String query) {

    if (context == null) {
      throw new PropertyHandlerRuntimeException(String.format(
          "Unable to execute LDPath expression '%s' because no context has been supplied.", query));
    }

    try {
      return ldpath.pathQuery(context, query, ldpathNamespaces);
    } catch (LDPathParseException ldppe) {
      throw new PropertyHandlerRuntimeException(
          String.format("Unable to parse LDPath expression '%s'", query), ldppe);
    }
  }

}
