package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.Swagger;
import java.util.Collection;
import java.util.Map;
import org.apache.marmotta.ldpath.LDPath;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.backend.Rdf4jRepositoryBackend;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class LdPathExecutor {

  private LDPath<Value> ldpath;
  private final ImmutableMap<String, String> ldpathNamespaces;

  public LdPathExecutor(final GraphEntityContext context) {
    this.ldpathNamespaces = context.getLdPathNamespaces();
    this.ldpath = createLdpath(context.getModel());

  }

  private LDPath<Value> createLdpath(Model model) {
    Repository repository = new SailRepository(new MemoryStore());
    Rdf4jRepositoryBackend repositoryBackend;


    try {
      repository.initialize();
      repositoryBackend = new Rdf4jRepositoryBackend(repository);

      final RepositoryConnection connection = repository.getConnection();
      connection.add(model);
      connection.close();
    } catch (RepositoryException re) {
      throw new LdPathExecutorRuntimeException("Unable to initialize RDF4JRepository.", re);
    }

    return new LDPath<>(repositoryBackend);
  }

  public Collection<Value> ldPathQuery(Value context, String query) {

    if (context == null) {
      throw new LdPathExecutorRuntimeException(String.format(
          "Unable to execute LDPath expression '%s' because no context has been supplied.", query));
    }

    try {
      return ldpath.pathQuery(context, query, ldpathNamespaces);
    } catch (LDPathParseException ldppe) {
      throw new LdPathExecutorRuntimeException(
          String.format("Unable to parse LDPath expression '%s'", query), ldppe);
    }
  }

}
