package org.dotwebstack.framework.frontend.http.site;

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
  public void build_CreatesSite_WithValidData() {
    // Act
    Site site =
        new Site.Builder(DBEERPEDIA.BREWERIES).domain(DBEERPEDIA.DOMAIN.stringValue()).build();

    // Assert
    assertThat(site.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(site.getDomain(), equalTo(DBEERPEDIA.DOMAIN.stringValue()));
    assertThat(site.isMatchAllDomain(), equalTo(false));
  }

  @Test
  public void build_CreatesSiteDefaults_WhenNotProvided() {
    // Act
    Site site = new Site.Builder(DBEERPEDIA.BREWERIES).build();

    // Assert
    assertThat(site.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
    assertThat(site.isMatchAllDomain(), equalTo(true));
  }

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Site.Builder(null).build();
  }

  @Test
  public void domain_ThrowsException_WithMissingValue() {
    thrown.expect(NullPointerException.class);

    // Act
    new Site.Builder(DBEERPEDIA.BREWERIES).domain(null).build();
  }

}
