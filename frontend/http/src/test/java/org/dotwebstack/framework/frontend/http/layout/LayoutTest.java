package org.dotwebstack.framework.frontend.http.layout;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LayoutTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Test
  public void build_CreatesLayout_WithValidData() {
    // Act
    Layout layout = new Layout.Builder(DBEERPEDIA.LAYOUT).label("Hello World!").option(ELMO.LAYOUT,
        valueFactory.createLiteral("myStyle.css")).build();

    // Assert
    assertThat(layout.getOptions().get(ELMO.LAYOUT),
        equalTo(valueFactory.createLiteral("myStyle.css")));
    assertThat(layout.getIdentifier(), equalTo(DBEERPEDIA.LAYOUT));
    assertThat(layout.getLabel(), equalTo("Hello World!"));
  }

  @Test
  public void build_CreatesLayout_WithValidDataAndBNode() {
    // Act
    final BNode blankNode = valueFactory.createBNode();
    Layout layout = new Layout.Builder(blankNode).label("Hello World!").option(ELMO.LAYOUT,
        valueFactory.createLiteral("myStyle.css")).build();

    // Assert
    assertThat(layout.getOptions().get(ELMO.LAYOUT),
        equalTo(valueFactory.createLiteral("myStyle.css")));
    assertThat(layout.getIdentifier(), equalTo(blankNode));
    assertThat(layout.getLabel(), equalTo("Hello World!"));
  }

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Layout.Builder(null).option(ELMO.LAYOUT, valueFactory.createLiteral("myStyle.css")).build();
  }

  @Test
  public void build_ThrowsException_WithMissingCssRef() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Layout.Builder(DBEERPEDIA.LAYOUT).option(null, null).build();
  }

  @Test
  public void label_ThrowsException_WithMissingValue() {
    thrown.expect(NullPointerException.class);

    // Act
    new Layout.Builder(DBEERPEDIA.LAYOUT).label(null).build();
  }
}
