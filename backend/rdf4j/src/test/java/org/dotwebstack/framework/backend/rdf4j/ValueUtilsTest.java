package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.jupiter.api.Test;

class ValueUtilsTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static final Resource SUBJECT = VF.createIRI("foo:1");

  private static final Literal LABEL = VF.createLiteral("foo");

  @Test
  void findRequiredPropertyIri_ReturnsIri_ForPresentValue() {
    Model model = new ModelBuilder().add(SUBJECT, RDF.TYPE, RDFS.CLASS)
        .build();

    IRI property = ValueUtils.findRequiredPropertyIri(model, SUBJECT, RDF.TYPE);

    assertThat(property, is(equalTo(RDFS.CLASS)));
  }

  @Test
  void findRequiredPropertyIri_ThrowsException_ForAbsentValue() {
    Model model = new ModelBuilder().build();

    assertThrows(InvalidConfigurationException.class,
        () -> ValueUtils.findRequiredPropertyIri(model, SUBJECT, RDF.TYPE));
  }

  @Test
  void findRequiredPropertyLiteral_ReturnsLiteral_ForPresentValue() {
    Model model = new ModelBuilder().add(SUBJECT, RDFS.LABEL, LABEL)
        .build();

    Literal property = ValueUtils.findRequiredPropertyLiteral(model, SUBJECT, RDFS.LABEL);

    assertThat(property, is(equalTo(LABEL)));
  }

  @Test
  void findRequiredPropertyLiteral_ThrowsException_ForAbsentValue() {
    Model model = new ModelBuilder().build();

    assertThrows(InvalidConfigurationException.class,
        () -> ValueUtils.findRequiredPropertyLiteral(model, SUBJECT, RDFS.LABEL));
  }

}
