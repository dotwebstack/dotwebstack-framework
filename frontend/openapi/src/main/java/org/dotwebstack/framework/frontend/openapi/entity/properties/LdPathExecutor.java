package org.dotwebstack.framework.frontend.openapi.entity.properties;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.Swagger;
import java.util.Collection;
import org.apache.marmotta.ldpath.LDPath;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.dotwebstack.framework.frontend.openapi.entity.backend.Rdf4jRepositoryBackend;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class LdPathExecutor {

  private LDPath<Value> ldpath;
  private final ImmutableMap<String, String> ldpathNamespaces;

  public LdPathExecutor(EntityBuilderContext context) {
    this.ldpathNamespaces = extractLdpathNamespaces(context.getSwagger());
    try {
      this.ldpath = createLdpath(context.getQueryResult());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  private ImmutableMap<String, String> extractLdpathNamespaces(Swagger swagger) {

    ImmutableMap<String, String> vendorExtensions =

        ImmutableMap.<String, String>builder()
                .put("nen3610","http://data.informatiehuisruimte.nl/def/nen3610#")
                .put("skos","http://www.w3.org/2004/02/skos/core#")
                .put("dcterms",
                    "http://purl.org/dc/terms/").put("ro",
                        "http://data.informatiehuisruimte.nl/def/ro#").put("pdok",
                            "http://data.pdok.nl/def/pdok#").put("geo",
                                "http://www.opengis.net/ont/geosparql#").put("ro_beg",
                                    "http://data.informatiehuisruimte.nl/ro/id/begrip").build();
    return vendorExtensions;


  }

  private LDPath<Value> createLdpath(QueryResult queryResult) throws Exception {
    Repository repository = new SailRepository(new MemoryStore());
    Rdf4jRepositoryBackend repositoryBackend;


    try {
      repository.initialize();
      repositoryBackend = new Rdf4jRepositoryBackend(repository);

      final RepositoryConnection connection = repository.getConnection();
      connection.add(queryResult.getStatements());
      connection.close();
    } catch (RepositoryException re) {
      throw new PropertyHandlerRuntimeException("Unable to initialize RDF4JRepository.", re);
    }

    LDPath<Value> ldPath = new LDPath<>(repositoryBackend);

    // ldPath.registerFunction(new KeepAfterLastFunction<>());
    // ldPath.registerFunction(new TrimFunction<>());
    // ldPath.registerFunction(new JoinFunction<>());
    // ldPath.registerFunction(new SortByPropertyFunction<>(ldpathNamespaces));

    return ldPath;
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
