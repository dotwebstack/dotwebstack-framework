package org.dotwebstack.framework.frontend.http.layout;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LayoutTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void build_CreatesLayout_WithValidData() {
    // Act
    Layout layout =
        new Layout.Builder(DBEERPEDIA.LAYOUT, "myStyle.css").label("Hello World!").build();

    // Assert
    assertThat(layout.getCssResource(), equalTo("myStyle.css"));
    assertThat(layout.getIdentifier(), equalTo(DBEERPEDIA.LAYOUT));
    assertThat(layout.getLabel(), equalTo("Hello World!"));
  }

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Layout.Builder(null, "myStyle.css").build();
  }

  @Test
  public void build_ThrowsException_WithMissingCssRef() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Layout.Builder(DBEERPEDIA.LAYOUT, null).build();
  }

  @Test
  public void label_ThrowsException_WithMissingValue() {
    thrown.expect(NullPointerException.class);

    // Act
    new Layout.Builder(DBEERPEDIA.LAYOUT, "myStyle.css").label(null).build();
  }
}
