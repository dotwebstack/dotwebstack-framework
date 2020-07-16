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
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.OuterQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PathHelperTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private Vertice vertice;

  private NodeShape nodeShape;

  @Mock
  private Variable variable;

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

    when(this.variable.getQueryString()).thenReturn("?x1");
    when(this.fieldDefinition1.getName()).thenReturn("field1");
    when(this.fieldDefinition2.getName()).thenReturn("field2");

  }

  @DisplayName("Resolve path returns empty when last path element contains a @resource directive")
  @Test
  public void resolvePathResourceDirective() {
    // Arrange
    when(fieldDefinition2.getDirective(Rdf4jDirectives.RESOURCE_NAME)).thenReturn(mock(GraphQLDirective.class));

    // Act
    Optional<Edge> result =
        PathHelper.resolvePath(this.vertice, this.nodeShape, this.fieldPath, this.query, PathType.CONSTRAINT);

    // Assert
    assertThat(result.isEmpty(), is(true));
  }

  @DisplayName("Resolve path returns empty optional for null nodeshape")
  @Test
  public void resolvePathNullNodeShape() {
    // Arrange & Act
    Optional<Edge> result = PathHelper.resolvePath(this.vertice, null, this.fieldPath, this.query, PathType.CONSTRAINT);

    // Assert
    assertThat(result.isEmpty(), is(true));
  }

  @DisplayName("Resolve path returns edge with constraint path type")
  @Test
  public void resolvePath() {
    // Arrange
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

  private NodeShape buildNodeShape() {
    PropertyShape propertyShape1 = PropertyShape.builder()
        .name("field1")
        .path(PredicatePath.builder()
            .iri(VF.createIRI("http://www.example.com/iri1"))
            .build())
        .build();

    Map<String, PropertyShape> propertyShapes = Map.of("field1", propertyShape1);
    return NodeShape.builder()
        .propertyShapes(propertyShapes)
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
