package org.dotwebstack.framework.frontend.http;

import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpConfigurationTest {

  @Mock
  private HttpExtension extensionA;

  @Mock
  private HttpExtension extensionB;

  @Test
  public void noErrorsWithoutExtensions() {
    // Act & assert
    new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void extensionsInitialized() {
    // Act
    HttpConfiguration httpConfiguration =
        new HttpConfiguration(ImmutableList.of(extensionA, extensionB));

    // Assert
    verify(extensionA).initialize(httpConfiguration);
    verify(extensionB).initialize(httpConfiguration);
  }

}
