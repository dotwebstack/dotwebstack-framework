package org.dotwebstack.framework.site;

import static org.hamcrest.CoreMatchers.equalTo;
import static org. junit.Assert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.dotwebstack.framework.site.Site;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SiteTest {

    @Test
    public void builder() {
        // Act
        Site site = new Site.Builder(DBEERPEDIA.BREWERIES).domain(DBEERPEDIA.DOMAIN).build();

        // Assert
        assertThat(site.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
        assertThat(site.getDomain(), equalTo(DBEERPEDIA.DOMAIN));
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

    @Test(expected = NullPointerException.class)
    public void builderWithMandatoryNullValues() {
        // Act
        Site site = new Site.Builder(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void builderWithOptionalNullValues() {
        // Act
        Site site = new Site.Builder(DBEERPEDIA.BREWERIES).domain(null).build();
    }
}
