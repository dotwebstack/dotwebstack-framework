package org.dotwebstack.framework.frontend.openapi.entity.backend;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.NonNull;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

public abstract class AbstractRdf4jBackend extends Rdf4jValueBackend implements RDFBackend<Value> {

  protected IRI createUriInternal(@NonNull final ValueFactory valueFactory, @NonNull String uri) {
    return valueFactory.createIRI(uri);
  }

  protected Literal createLiteralInternal(@NonNull final ValueFactory valueFactory,
      @NonNull String content) {
    return valueFactory.createLiteral(content);
  }

  protected Literal createLiteralInternal(@NonNull final ValueFactory valueFactory,
      @NonNull String content, Locale language, java.net.URI type) {
    if (language == null && type == null) {
      return valueFactory.createLiteral(content);
    } else if (type == null) {
      return valueFactory.createLiteral(content, language.getLanguage());
    } else {
      return valueFactory.createLiteral(content, valueFactory.createIRI(type.toString()));
    }
  }

  protected Collection<Value> listObjectsInternal(@NonNull RepositoryConnection connection,
      Resource subject, IRI property, boolean includeInferred, Resource... contexts) {
    final ValueFactory valueFactory = connection.getValueFactory();

    Set<Value> result = new HashSet<>();
    try (RepositoryResult<Statement> queryResult =
        connection.getStatements(merge(subject, valueFactory), merge(property, valueFactory), null,
            includeInferred, contexts)) {
      while (queryResult.hasNext()) {
        result.add(queryResult.next().getObject());
      }
    }
    return ImmutableSet.copyOf(result);
  }

  protected Collection<Value> listSubjectsInternal(@NonNull final RepositoryConnection connection,
      IRI property, Value object, boolean includeInferred, Resource... contexts) {
    final ValueFactory valueFactory = connection.getValueFactory();

    Set<Value> result = new HashSet<>();
    try (RepositoryResult<Statement> queryResult = connection.getStatements(null,
        merge(property, valueFactory), merge(object, valueFactory), includeInferred, contexts)) {
      while (queryResult.hasNext()) {
        result.add(queryResult.next().getSubject());
      }
    }
    return ImmutableSet.copyOf(result);
  }

  /**
   * Merge the value given as argument into the value factory given as argument.
   */
  @SuppressWarnings("unchecked")
  private <T extends Value> T merge(T value, @NonNull ValueFactory vf) {
    if (value instanceof IRI) {
      return (T) vf.createIRI(value.stringValue());
    } else if (value instanceof BNode) {
      return (T) vf.createBNode(((BNode) value).getID());
    } else {
      return value;
    }
  }

  @Override
  public abstract Literal createLiteral(String content);

  @Override
  public abstract Literal createLiteral(String content, Locale language, java.net.URI type);

  @Override
  public abstract IRI createURI(String uri);

  @Override
  public boolean supportsThreading() {
    return false;
  }

  @Override
  public ThreadPoolExecutor getThreadPool() {
    return null;
  }

}
