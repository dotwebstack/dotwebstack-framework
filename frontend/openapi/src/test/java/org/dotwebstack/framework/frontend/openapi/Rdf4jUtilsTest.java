package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class Rdf4jUtilsTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void asRepository_ReturnsRepository_ForModel() {
    // Arrange
    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME).subject(
            DBEERPEDIA.MAXIMUS).add(RDF.TYPE, DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME,
                DBEERPEDIA.MAXIMUS_NAME).build();

    // Act
    Repository repository = Rdf4jUtils.asRepository(model);

    // Assert
    try (RepositoryConnection con = repository.getConnection()) {
      assertThat(con.size(), is(4L));

      assertTrue(con.hasStatement(DBEERPEDIA.BROUWTOREN, RDF.TYPE, DBEERPEDIA.BREWERY_TYPE, false));
      assertTrue(con.hasStatement(DBEERPEDIA.BROUWTOREN, DBEERPEDIA.NAME,
          DBEERPEDIA.BROUWTOREN_NAME, false));
      assertTrue(con.hasStatement(DBEERPEDIA.MAXIMUS, RDF.TYPE, DBEERPEDIA.BREWERY_TYPE, false));
      assertTrue(
          con.hasStatement(DBEERPEDIA.MAXIMUS, DBEERPEDIA.NAME, DBEERPEDIA.MAXIMUS_NAME, false));
    }
  }

  @Test
  public void evaluateSingleBindingSelectQuery_EvaluatesToSetOfResources() {
    // Arrange
    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME).subject(
            DBEERPEDIA.MAXIMUS).add(RDF.TYPE, DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME,
                DBEERPEDIA.MAXIMUS_NAME).build();
    Repository repository = Rdf4jUtils.asRepository(model);

    // Act
    Set<Resource> result = Rdf4jUtils.evaluateSingleBindingSelectQuery(repository,
        String.format("SELECT ?s WHERE {?s <%s> <%s> }", RDF.TYPE, DBEERPEDIA.BREWERY_TYPE));

    // Assert
    assertThat(result, containsInAnyOrder(DBEERPEDIA.BROUWTOREN, DBEERPEDIA.MAXIMUS));
  }

  @Test
  public void evaluateSingleBindingSelectQuery_ThrowsEx_WhenMultipleBindingsHaveBeenDefined() {
    // Assert
    thrown.expect(QueryEvaluationException.class);
    thrown.expectMessage("Query must define exactly 1 binding:");

    // Arrange
    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME).subject(
            DBEERPEDIA.MAXIMUS).add(RDF.TYPE, DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME,
                DBEERPEDIA.MAXIMUS_NAME).build();
    Repository repository = Rdf4jUtils.asRepository(model);

    // Act
    Rdf4jUtils.evaluateSingleBindingSelectQuery(repository,
        String.format("SELECT ?s ?p WHERE {?s ?p <%s> }", DBEERPEDIA.BREWERY_TYPE));
  }

  @Test
  public void evaluateSingleBindingSelectQuery_ThrowsEx_WhenNonRdfResourceIsReturned() {
    // Assert
    thrown.expect(QueryEvaluationException.class);
    thrown.expectMessage("Query must return RDF resources (IRIs and blank nodes) only");

    // Arrange
    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME).subject(
            DBEERPEDIA.MAXIMUS).add(RDF.TYPE, DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME,
                DBEERPEDIA.MAXIMUS_NAME).build();
    Repository repository = Rdf4jUtils.asRepository(model);

    // Act
    Rdf4jUtils.evaluateSingleBindingSelectQuery(repository,
        String.format("SELECT ?o WHERE {<%s> <%s> ?o }", DBEERPEDIA.BROUWTOREN, DBEERPEDIA.NAME));
  }

}
