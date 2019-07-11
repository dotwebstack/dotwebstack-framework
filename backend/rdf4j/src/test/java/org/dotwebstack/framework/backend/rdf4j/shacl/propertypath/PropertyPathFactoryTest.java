package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_BEERNAMES_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_BEERS_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_PATH;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_FOUNDED_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.BREWERY_POSTAL_CODE_SHAPE;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SCHEMA_ADDRESS;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SCHEMA_NAME;
import static org.dotwebstack.framework.backend.rdf4j.Constants.SCHEMA_POSTAL_CODE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PropertyPathFactoryTest {

  private static Model shapeModel;

  @BeforeAll
  public static void setup() throws IOException {
    shapeModel = loadShapeModel();
  }

  @Test
  void createPropertyPath_ReturnsSequencePath() {
    // Act
    final PropertyPath propertyPath = createPropertyPath(BREWERY_POSTAL_CODE_SHAPE);

    // Assert
    assertTrue(propertyPath instanceof SequencePath);
    assertThat(resolveIris(propertyPath), equalTo(Arrays.asList(SCHEMA_ADDRESS, SCHEMA_POSTAL_CODE, RDF.NIL)));
  }

  @Test
  void createPropertyPath_ReturnsPredicatePath() {
    // Act
    final PropertyPath propertyPath = createPropertyPath(BREWERY_FOUNDED_SHAPE);

    // Assert
    assertTrue(propertyPath instanceof PredicatePath);
    assertThat(resolveIris(propertyPath), equalTo(Arrays.asList(BREWERY_FOUNDED_PATH)));
  }

  @Test
  void createPropertyPath_ReturnsInversePathInsideSequencePath() {
    // Act
    final PropertyPath propertyPath = createPropertyPath(BREWERY_BEERNAMES_SHAPE);

    // Assert sequence path
    assertTrue(propertyPath instanceof SequencePath);
    assertThat(resolveIris(propertyPath), equalTo(Arrays.asList(BREWERY_BEERS_PATH, SCHEMA_NAME, RDF.NIL)));

    // Assert inverse path
    final PropertyPath first = ((SequencePath) propertyPath).getFirst();
    assertTrue(first instanceof InversePath);
    final PropertyPath object = ((InversePath) first).getObject();
    assertTrue(object instanceof PredicatePath);
  }

  public static PropertyPath createPropertyPath(Resource subject) {
    return PropertyPathFactory.create(shapeModel, subject, SHACL.PATH);
  }

  private static List<IRI> resolveIris(PropertyPath propertyPath) {
    List<IRI> result = new ArrayList<>();

    resolveIris(propertyPath, result);
    return result;
  }

  private static void resolveIris(PropertyPath propertyPath, List<IRI> iris) {
    if (propertyPath != null) {
      if (propertyPath instanceof SequencePath) {
        resolveIris(((SequencePath) propertyPath).getFirst(), iris);
        resolveIris(((SequencePath) propertyPath).getRest(), iris);
      } else if (propertyPath instanceof PredicatePath) {
        iris.add(((PredicatePath) propertyPath).getIri());
      } else if (propertyPath instanceof InversePath) {
        resolveIris(((InversePath) propertyPath).getObject(), iris);
      } else {
        fail("PropertyPath of type [" + propertyPath.getClass() + "] not supported");
      }
    }
  }

  public static Model loadShapeModel() throws IOException {
    Repository repo = new SailRepository(new MemoryStore());
    repo.init();
    String shapesPath = "config/model/shapes.trig";
    try (RepositoryConnection connection = repo.getConnection();
        InputStream is = PropertyPathFactoryTest.class.getClassLoader()
            .getResourceAsStream(shapesPath);
        Reader shaclRules = new InputStreamReader(is)) {

      connection.begin();
      connection.add(shaclRules, "", RDFFormat.TRIG, RDF4J.SHACL_SHAPE_GRAPH);
      connection.commit();
    }

    return QueryResults.asModel(repo.getConnection()
        .getStatements(null, null, null, RDF4J.SHACL_SHAPE_GRAPH));
  }
}
