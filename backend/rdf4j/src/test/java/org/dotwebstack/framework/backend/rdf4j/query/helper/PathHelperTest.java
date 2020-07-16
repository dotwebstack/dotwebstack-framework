package org.dotwebstack.framework.backend.rdf4j.query.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;
import org.dotwebstack.framework.backend.rdf4j.query.model.Constraint;
import org.dotwebstack.framework.backend.rdf4j.query.model.Edge;
import org.dotwebstack.framework.backend.rdf4j.query.model.PathType;
import org.dotwebstack.framework.backend.rdf4j.query.model.Vertice;
import org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PathHelperTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private Vertice vertice;

  private NodeShape nodeShape;

  @Mock
  private GraphQLFieldDefinition fieldDefinition1;

  @Mock
  private GraphQLFieldDefinition fieldDefinition2;

  private FieldPath fieldPath;

  private OuterQuery<?> query;

  @BeforeEach
  public void setup() {
    this.vertice = buildVertice();
    this.nodeShape = buildNodeShape();
    this.query = Queries.CONSTRUCT();
    this.fieldPath = FieldPath.builder()
        .fieldDefinitions(List.of(this.fieldDefinition1, this.fieldDefinition2))
        .build();
  }

  @Test
  public void resolvePath_returnsEmpty_for_resourceDirective() {
    // Arrange
    when(fieldDefinition2.getDirective(Rdf4jDirectives.RESOURCE_NAME)).thenReturn(mock(GraphQLDirective.class));

    // Act
    Optional<Edge> result =
        PathHelper.resolvePath(this.vertice, this.nodeShape, this.fieldPath, this.query, PathType.CONSTRAINT);

    // Assert
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void resolvePath_returnsEmpty_for_nullNodeShape() {
    // Arrange & Act
    Optional<Edge> result = PathHelper.resolvePath(this.vertice, null, this.fieldPath, this.query, PathType.CONSTRAINT);

    // Assert
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  public void resolvePath_returnsEdge_for_singleFieldDefinitions() {
    // Arrange
    when(this.fieldDefinition1.getName()).thenReturn("field1");
    FieldPath fieldPath = FieldPath.builder()
        .fieldDefinitions(List.of(this.fieldDefinition1))
        .build();

    // Act
    Optional<Edge> result =
        PathHelper.resolvePath(this.vertice, this.nodeShape, fieldPath, this.query, PathType.CONSTRAINT);

    // Assert
    assertThat(result.isPresent(), is(true));
    assertThat(result.get()
        .getPropertyShape()
        .getName(), is("field1"));
    assertThat(result.get()
        .getPathTypes(), is(List.of(PathType.CONSTRAINT)));
  }

  @Test
  public void resolvePath_returnsEdge_for_multipleFieldDefinitions() {
    // Arrange
    when(this.fieldDefinition1.getName()).thenReturn("field1");
    when(this.fieldDefinition2.getName()).thenReturn("field2");
    FieldPath fieldPath = FieldPath.builder()
        .fieldDefinitions(List.of(this.fieldDefinition1, this.fieldDefinition2))
        .build();

    // Act
    Optional<Edge> result =
        PathHelper.resolvePath(this.vertice, this.nodeShape, fieldPath, this.query, PathType.FILTER);

    // Assert
    assertThat(result.isPresent(), is(true));
    assertThat(result.get()
        .getPropertyShape()
        .getName(), is("field2"));
    assertThat(result.get()
        .getPathTypes(), is(List.of(PathType.FILTER)));
  }

  private NodeShape buildNodeShape() {
    PropertyShape propertyShape2 = PropertyShape.builder()
        .name("field2")
        .path(PredicatePath.builder()
            .iri(VF.createIRI("http://www.example.com/iri2"))
            .build())
        .build();

    NodeShape nodeShape2 = NodeShape.builder()
        .propertyShapes(Map.of("field2", propertyShape2))
        .classes(Set.of(VF.createIRI("http://www.example.com#testType2")))
        .build();

    PropertyShape propertyShape1 = PropertyShape.builder()
        .name("field1")
        .node(nodeShape2)
        .path(PredicatePath.builder()
            .iri(VF.createIRI("http://www.example.com/iri1"))
            .build())
        .build();

    return NodeShape.builder()
        .propertyShapes(Map.of("field1", propertyShape1))
        .classes(Set.of(VF.createIRI("http://www.example.com#testType")))
        .build();
  }

  private Vertice buildVertice() {
    IRI iri = VF.createIRI("http://www.example.com#testType");

    return Vertice.builder()
        .constraints(Set.of(Constraint.builder()
            .constraintType(ConstraintType.RDF_TYPE)
            .predicate(() -> "<" + RDF.TYPE.stringValue() + ">")
            .values(Set.of(iri))
            .build()))
        .build();
  }
}
