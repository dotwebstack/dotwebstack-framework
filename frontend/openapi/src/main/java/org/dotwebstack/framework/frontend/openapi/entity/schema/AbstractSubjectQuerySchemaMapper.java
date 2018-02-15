package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.Property;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

abstract class AbstractSubjectQuerySchemaMapper<S extends Property, T>
    extends AbstractSchemaMapper<S, T> {

  protected boolean hasSubjectQueryVendorExtension(@NonNull Property property) {
    return hasVendorExtension(property, OpenApiSpecificationExtensions.SUBJECT_QUERY);
  }

  /**
   * Apply the subject (SPARQL) query and returns the resulting subjects.
   * 
   * @throws IllegalStateException If the property does not have the
   *         {@link OpenApiSpecificationExtensions#SUBJECT_QUERY} vendor extension defined. Please
   *         call {@link #hasSubjectQueryVendorExtension(Property)} before calling this method.
   * @throws SchemaMapperRuntimeException If the subject query has &gt; 1 binding defined. Or if the
   *         result contains a non {@link Resource}.
   */
  protected final Set<Resource> getSubjects(@NonNull Property property,
      @NonNull GraphEntity entity) {
    if (!hasSubjectQueryVendorExtension(property)) {
      throw new IllegalStateException(String.format(
          "Vendor extension '%s' not defined, "
              + "please call hasSubjectQueryVendorExtension() before calling this method",
          OpenApiSpecificationExtensions.SUBJECT_QUERY));
    }

    Repository repository = createRepository();

    try (RepositoryConnection connection = repository.getConnection()) {
      connection.add(entity.getModel());

      String queryString =
          (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.SUBJECT_QUERY);
      TupleQuery tupleQuery = connection.prepareTupleQuery(queryString);

      return evaluateQuery(tupleQuery);
    } finally {
      repository.shutDown();
    }
  }

  private static Repository createRepository() {
    Repository repository = new SailRepository(new MemoryStore());

    repository.initialize();

    return repository;
  }

  private static Set<Resource> evaluateQuery(TupleQuery query) {
    try (TupleQueryResult result = query.evaluate()) {
      List<String> bindingNames = result.getBindingNames();

      checkNoBindingNamesEqualTo1(query, bindingNames);

      String bindingName = bindingNames.get(0);
      ImmutableSet.Builder<Resource> builder = ImmutableSet.builder();

      while (result.hasNext()) {
        BindingSet bindingSet = result.next();
        Value value = bindingSet.getValue(bindingName);

        checkValueInstanceOfResource(query, value);

        builder.add((Resource) value);
      }

      return builder.build();
    }
  }

  private static void checkNoBindingNamesEqualTo1(TupleQuery query, List<String> bindingNames) {
    if (bindingNames.size() != 1) {
      throw new SchemaMapperRuntimeException(
          String.format("'%s' must define exactly 1 binding: '%s'",
              OpenApiSpecificationExtensions.SUBJECT_QUERY, query));
    }
  }

  private static void checkValueInstanceOfResource(TupleQuery query, Value value) {
    if (!(value instanceof Resource)) {
      throw new SchemaMapperRuntimeException(String.format(
          "'%s' must return RDF resources (IRIs and blank nodes) only. "
              + "Query string: '%s'%nValue returned: '%s'",
          OpenApiSpecificationExtensions.SUBJECT_QUERY, query, value));
    }
  }

  /**
   * @return Applies the subject query and returns the single subject, or {@code null} if no subject
   *         can be found.
   * @throws SchemaMapperRuntimeException If the property is required, and no subject can be found.
   * @throws SchemaMapperRuntimeException If more than one subject has been found.
   * @see #getSubjects(Property, GraphEntity)
   */
  protected final Value getSubject(@NonNull Property property, @NonNull GraphEntity graphEntity) {
    Set<Resource> subjects = getSubjects(property, graphEntity);

    if (subjects.isEmpty()) {
      if (property.getRequired()) {
        throw new SchemaMapperRuntimeException(
            "Subject query for a required object property yielded no result.");
      }

      return null;
    }

    if (subjects.size() > 1) {
      throw new SchemaMapperRuntimeException(
          "More entrypoint subjects found. Only one is required.");
    }

    return subjects.iterator().next();
  }

}
