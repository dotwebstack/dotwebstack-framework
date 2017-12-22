package org.dotwebstack.framework.frontend.openapi.entity.backend;

import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

public class Rdf4jRepositoryBackend extends AbstractRdf4jBackend {

  private final Repository repository;
  private final boolean includeInferred;
  private final Resource[] contexts;

  /**
   * Initialise a new RDF4J backend using the repository passed as argument. Queries will include
   * inferred statements and all contexts.
   * 
   * @param repository The back-end repository.
   */
  public Rdf4jRepositoryBackend(@NonNull Repository repository) {
    this(repository, true);
  }

  /**
   * Initialise a new RDF4J backend using the repository.
   */
  private Rdf4jRepositoryBackend(@NonNull Repository repository, boolean includeInferred,
      Resource... contexts) {
    this.repository = repository;
    this.includeInferred = includeInferred;
    this.contexts = contexts;
  }

  /**
   * Create a literal node with the content passed as argument.
   *
   * @param content string content to represent inside the literal
   * @return a literal node in using the model used by this backend
   */
  @Override
  public Literal createLiteral(@NonNull String content) {
    return createLiteralInternal(repository.getValueFactory(), content);
  }

  /**
   * Create a literal node with the content passed as argument.
   *
   * @param content string content to represent inside the literal
   * @return a literal node in using the model used by this backend
   */
  @Override
  public Literal createLiteral(@NonNull String content, Locale language, URI type) {
    return createLiteralInternal(repository.getValueFactory(), content, language, type);
  }

  /**
   * Create a URI mode with the URI passed as argument.
   *
   * @param uri URI of the resource to create
   * @return a URI node using the model used by this backend
   */
  @Override
  public IRI createURI(@NonNull String uri) {
    return createUriInternal(repository.getValueFactory(), uri);
  }

  /**
   * List the objects of triples in the triple store underlying this backend that have the subject
   * and property given as argument.
   *
   * @param subject the subject of the triples to look for
   * @param property the property of the triples to look for
   * @return all objects of triples with matching subject and property
   */
  @Override
  public Collection<Value> listObjects(Value subject, Value property) {
    try {
      RepositoryConnection connection = repository.getConnection();

      try {
        connection.begin();
        return listObjectsInternal(connection, (Resource) subject, (IRI) property, includeInferred,
            contexts);
      } finally {
        connection.commit();
        connection.close();
      }
    } catch (RepositoryException exception) {
      throw new Rdf4jBackendRuntimeException("Error while querying RDF4J repository.", exception);
    } catch (ClassCastException exception) {
      throw new IllegalArgumentException(
          String.format(
              "Subject needs to be a URI or blank node, property a URI node "
                  + "(types: [subject: %s, property: %s]).",
              debugType(subject), debugType(property)),
          exception);
    }

  }

  /**
   * List the subjects of triples in the triple store underlying this backend that have the object
   * and property given as argument.
   *
   * @param object the object of the triples to look for
   * @param property the property of the triples to look for
   * @return all subjects of triples with matching object and property
   * @throws UnsupportedOperationException in case reverse selection is not supported (e.g. when
   *         querying Linked Data)
   */
  @Override
  public Collection<Value> listSubjects(Value property, Value object) {
    try {
      final RepositoryConnection connection = repository.getConnection();

      try {
        connection.begin();
        return listSubjectsInternal(connection, (IRI) property, object, includeInferred, contexts);
      } finally {
        connection.commit();
        connection.close();
      }
    } catch (RepositoryException ex) {
      throw new Rdf4jBackendRuntimeException("Error while querying RDF4J repository.", ex);
    } catch (ClassCastException ex) {
      String namelessNodeType = isBlank(property) ? "bNode" : "literal";
      throw new IllegalArgumentException(
          String.format("Property needs to be a URI node (property type: %s).",
              isURI(property) ? "URI" : namelessNodeType),
          ex);
    }
  }
}
