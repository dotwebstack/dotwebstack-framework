package org.dotwebstack.framework.frontend.ld.redirection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RedirectionTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Stage stage;

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Redirection.Builder(null, stage, DBEERPEDIA.ID2DOC_PATH_PATTERN.stringValue(),
        DBEERPEDIA.ID2DOC_REDIRECT_TEMPLATE.stringValue()).build();
  }

  @Test
  public void build_ThrowsException_WithMissingStage() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Redirection.Builder(DBEERPEDIA.ID2DOC_REDIRECTION, null,
        DBEERPEDIA.ID2DOC_PATH_PATTERN.stringValue(),
        DBEERPEDIA.ID2DOC_REDIRECT_TEMPLATE.stringValue()).build();
  }

  @Test
  public void build_ThrowsException_WithMissingPathPattern() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Redirection.Builder(DBEERPEDIA.ID2DOC_REDIRECTION, stage, null,
        DBEERPEDIA.ID2DOC_REDIRECT_TEMPLATE.stringValue()).build();
  }

  @Test
  public void build_ThrowsException_WithMissingRedirectTemplate() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Redirection.Builder(DBEERPEDIA.ID2DOC_REDIRECTION, stage,
        DBEERPEDIA.ID2DOC_PATH_PATTERN.stringValue(), null).build();
  }

  @Test
  public void build_CreatesRedirection_WithValidData() {
    // Act
    Redirection redirection = new Redirection.Builder(DBEERPEDIA.ID2DOC_REDIRECTION, stage,
        DBEERPEDIA.ID2DOC_PATH_PATTERN.stringValue(),
        DBEERPEDIA.ID2DOC_REDIRECT_TEMPLATE.stringValue()).build();

    // Assert
    assertThat(redirection.getIdentifier(), equalTo(DBEERPEDIA.ID2DOC_REDIRECTION));
    assertThat(redirection.getStage(), equalTo(stage));
    assertThat(redirection.getPathPattern(), equalTo(DBEERPEDIA.ID2DOC_PATH_PATTERN.stringValue()));
    assertThat(redirection.getRedirectTemplate(),
        equalTo(DBEERPEDIA.ID2DOC_REDIRECT_TEMPLATE.stringValue()));
  }

}
