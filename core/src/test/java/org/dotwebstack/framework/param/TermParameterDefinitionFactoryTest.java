package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.dotwebstack.framework.param.shapes.BooleanPropertyShape;
import org.dotwebstack.framework.param.shapes.IntegerPropertyShape;
import org.dotwebstack.framework.param.shapes.IriPropertyShape;
import org.dotwebstack.framework.param.shapes.StringPropertyShape;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.dotwebstack.framework.vocabulary.SHACL;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Before;
import org.junit.Test;

public class TermParameterDefinitionFactoryTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private ParameterDefinitionFactory parameterDefinitionFactory;

  @Before
  public void setUp() {
    parameterDefinitionFactory =
        new TermParameterDefinitionFactory(ImmutableSet.of(new BooleanPropertyShape(),
            new IntegerPropertyShape(), new IriPropertyShape(), new StringPropertyShape()));
  }

  @Test
  public void supports_ReturnsTrue_ForSupportedIri() {
    // Assert
    boolean result = parameterDefinitionFactory.supports(ELMO.TERM_FILTER);

    // Act
    assertThat(result, is(true));
  }

  @Test
  public void supports_ReturnsFalse_ForUnsupportedIri() {
    // Assert
    boolean result = parameterDefinitionFactory.supports(
        VALUE_FACTORY.createIRI("http://unsupported#", "Filter"));

    // Act
    assertThat(result, is(false));
  }

  @Test
  public void create_createTermParameterDefinition_ForStringShape() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();
    BNode blankNode = VALUE_FACTORY.createBNode();

    builder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(RDF.TYPE, ELMO.TERM_FILTER).add(
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE).add(ELMO.SHAPE_PROP, blankNode).subject(
            blankNode).add(SHACL.DATATYPE, XMLSchema.STRING);

    Model model = builder.build();

    // Act
    ParameterDefinition result =
        parameterDefinitionFactory.create(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertThat(result, instanceOf(TermParameterDefinition.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is(DBEERPEDIA.NAME_PARAMETER_VALUE.stringValue()));
  }

  @Test
  public void create_createTermParameterDefinition_ForIntegerShape() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();
    BNode blankNode = VALUE_FACTORY.createBNode();

    builder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(RDF.TYPE, ELMO.TERM_FILTER).add(
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE).add(ELMO.SHAPE_PROP, blankNode).subject(
            blankNode).add(SHACL.DATATYPE, XMLSchema.INTEGER);

    Model model = builder.build();

    // Act
    ParameterDefinition result =
        parameterDefinitionFactory.create(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertThat(result, instanceOf(TermParameterDefinition.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is(DBEERPEDIA.NAME_PARAMETER_VALUE.stringValue()));
  }

  @Test
  public void create_createTermParameterDefinition_ForBooleanShape() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();
    BNode blankNode = VALUE_FACTORY.createBNode();

    builder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(RDF.TYPE, ELMO.TERM_FILTER).add(
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE).add(ELMO.SHAPE_PROP, blankNode).subject(
            blankNode).add(SHACL.DATATYPE, XMLSchema.BOOLEAN);

    Model model = builder.build();

    // Act
    ParameterDefinition result =
        parameterDefinitionFactory.create(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertThat(result, instanceOf(TermParameterDefinition.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is(DBEERPEDIA.NAME_PARAMETER_VALUE.stringValue()));
  }

  @Test
  public void create_createTermParameterDefinition_ForIriShape() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();
    BNode blankNode = VALUE_FACTORY.createBNode();

    builder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(RDF.TYPE, ELMO.TERM_FILTER).add(
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE).add(ELMO.SHAPE_PROP, blankNode).subject(
            blankNode).add(SHACL.DATATYPE, XMLSchema.ANYURI);

    Model model = builder.build();

    // Act
    ParameterDefinition result =
        parameterDefinitionFactory.create(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertThat(result, instanceOf(TermParameterDefinition.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is(DBEERPEDIA.NAME_PARAMETER_VALUE.stringValue()));
  }

  @Test
  public void create_createTermParameterDefinition_ForNoShape() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();

    builder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(RDF.TYPE, ELMO.TERM_FILTER).add(
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE);

    Model model = builder.build();

    // Act
    ParameterDefinition result =
        parameterDefinitionFactory.create(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertThat(result, instanceOf(TermParameterDefinition.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is(DBEERPEDIA.NAME_PARAMETER_VALUE.stringValue()));
  }

}
