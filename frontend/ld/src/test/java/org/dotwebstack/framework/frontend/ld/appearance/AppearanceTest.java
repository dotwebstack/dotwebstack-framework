package org.dotwebstack.framework.frontend.ld.appearance;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AppearanceTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Model model;

  @Test
  public void build_CreateAppearance_WithValidData() {
    // Assert
    Appearance appearance = new Appearance.Builder(DBEERPEDIA.BREWERY_APPEARANCE,
        ELMO.RESOURCE_APPEARANCE, model).build();

    // Act
    assertThat(appearance.getIdentifier(), equalTo(DBEERPEDIA.BREWERY_APPEARANCE));
    assertThat(appearance.getType(), equalTo(ELMO.RESOURCE_APPEARANCE));
    assertThat(appearance.getModel(), equalTo(model));
  }

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Appearance.Builder(null, ELMO.RESOURCE_APPEARANCE, model).build();
  }

  @Test
  public void build_ThrowsException_WithMissingType() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Appearance.Builder(DBEERPEDIA.BREWERY_APPEARANCE, null, model).build();
  }

  @Test
  public void build_ThrowsException_WithMissingModel() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Appearance.Builder(DBEERPEDIA.BREWERY_APPEARANCE, ELMO.RESOURCE_APPEARANCE, null).build();
  }

}
