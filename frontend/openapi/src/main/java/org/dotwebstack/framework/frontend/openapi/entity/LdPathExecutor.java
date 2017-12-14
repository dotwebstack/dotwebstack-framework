package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import lombok.NonNull;
import org.apache.marmotta.ldpath.LDPath;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.apache.marmotta.ldpath.model.Constants;
import org.apache.marmotta.ldpath.parser.Configuration;
import org.apache.marmotta.ldpath.parser.DefaultConfiguration;
import org.dotwebstack.framework.frontend.openapi.entity.backend.Rdf4jRepositoryBackend;
import org.dotwebstack.framework.frontend.openapi.ldpath.JoinFunction;
import org.dotwebstack.framework.frontend.openapi.ldpath.KeepAfterLastFunction;
import org.dotwebstack.framework.frontend.openapi.ldpath.SortByPropertyFunction;
import org.dotwebstack.framework.frontend.openapi.ldpath.TrimFunction;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class LdPathExecutor {

  private static final Configuration<Value> LD_PATH_CONFIG;

  static {
    LD_PATH_CONFIG = new DefaultConfiguration<>();
    LD_PATH_CONFIG.addFunction(Constants.NS_LMF_FUNCS + "keepAfterLast",
        new KeepAfterLastFunction<>());
    LD_PATH_CONFIG.addFunction(Constants.NS_LMF_FUNCS + "trim", new TrimFunction<>());
    LD_PATH_CONFIG.addFunction(Constants.NS_LMF_FUNCS + "join", new JoinFunction<>());
  }

  private final LDPath<Value> ldpath;

  private final ImmutableMap<String, String> ldpathNamespaces;

  public LdPathExecutor(@NonNull final GraphEntityContext context) {
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

    LDPath<Value> result = new LDPath<>(repositoryBackend, LD_PATH_CONFIG);

    result.registerFunction(new SortByPropertyFunction<>(ldpathNamespaces));

    return result;
  }

  public Collection<Value> ldPathQuery(Value context, @NonNull String query) {
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
