package org.dotwebstack.framework.frontend.ld.appearance;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AppearanceTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void build_CreateAppearance_WithValidData() {
    // Assert
    Model model = new LinkedHashModel();
    Appearance appearance = new Appearance.Builder(DBEERPEDIA.BREWERY_APPEARANCE,
        ELMO.RESOURCE_APPEARANCE, model).build();

    // Act
    assertThat(appearance.getIdentifier(), equalTo(DBEERPEDIA.BREWERY_APPEARANCE));
    assertThat(appearance.getType(), equalTo(ELMO.RESOURCE_APPEARANCE));
    assertThat(appearance.getModel(), equalTo(model));
  }

}
