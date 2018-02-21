package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Test;

public class Rdf4jUtilsTest {

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

}
