package org.dotwebstack.framework.frontend.http.site;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SiteTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void builder() {
    // Act
    Site site =
        new Site.Builder(DBEERPEDIA.BREWERIES).domain(DBEERPEDIA.DOMAIN.stringValue()).build();

    // Assert
    assertThat(site.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(site.getDomain(), equalTo(DBEERPEDIA.DOMAIN.stringValue()));
    assertFalse(site.isMatchAllDomain());
  }

  @Test
  public void builderWithDefaultValues() {
    // Act
    Site site = new Site.Builder(DBEERPEDIA.BREWERIES).build();

    // Assert
    assertThat(site.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertTrue(site.isMatchAllDomain());
  }

  public void builderWithMandatoryNullValues() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Site.Builder(null).build();
  }

  public void builderWithOptionalNullValues() {
    thrown.expect(NullPointerException.class);

    // Act
    new Site.Builder(DBEERPEDIA.BREWERIES).domain(null).build();
  }

}
