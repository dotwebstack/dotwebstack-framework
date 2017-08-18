package org.dotwebstack.framework.stage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org. junit.Assert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.dotwebstack.framework.site.Site;
import org.dotwebstack.framework.stage.Stage;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StageTest {

    @Mock
    Site siteMock;

    @Test
    public void builder() {
        //Act
        Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, siteMock)
            .basePath(DBEERPEDIA.BASE_PATH.stringValue())
            .build();

        // Assert
        assertThat(stage.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
        assertThat(stage.getSite(), equalTo(siteMock));
        assertThat(stage.getBasePath(), equalTo(DBEERPEDIA.BASE_PATH.stringValue()));
    }

    @Test
    public void builderWithDefaultValues() {
        //Act
        Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, siteMock).build();

        // Assert
        assertThat(stage.getIdentifier(), equalTo(DBEERPEDIA.BREWERIES));
        assertThat(stage.getSite(), equalTo(siteMock));
        assertThat(stage.getBasePath(), equalTo("/"));
    }

    @Test(expected = NullPointerException.class)
    public void buildWithMandatoryNullValues1() {
        // Act
        Stage stage = new Stage.Builder(null, null).build();
    }

    @Test(expected = NullPointerException.class)
    public void buildWithMandatoryNullValues2() {
        // Act
        Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, null).build();
    }

    @Test(expected = NullPointerException.class)
    public void buildWithOptionalNullValues() {
        // Act
        Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, siteMock).basePath(null).build();
    }


}
