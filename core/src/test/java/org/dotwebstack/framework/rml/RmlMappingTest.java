package org.dotwebstack.framework.rml;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RmlMappingTest {

  @Mock
  private Resource identifier;

  @Mock
  private Model model;

  private String streamName;

  @Test
  public void build_RmlMapping_WithValidData() {
    // Arrange
    streamName = "streamName";

    // Act
    RmlMapping rmlMapping = new RmlMapping.Builder(identifier).model(model).streamName(streamName)
        .build();

    // Assert
    assertThat(rmlMapping.getIdentifier(), equalTo(identifier));
    assertThat(rmlMapping.getModel(), equalTo(model));
    assertThat(rmlMapping.getStreamName(), equalTo(streamName));
  }

}
