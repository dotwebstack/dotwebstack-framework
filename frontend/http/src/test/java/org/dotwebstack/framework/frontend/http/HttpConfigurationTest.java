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
  HttpExtension extensionA;

  @Mock
  HttpExtension extensionB;

  @Test
  public void noExtensions() {
    // Act
    new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void multipleExtensions() {
    // Act
    HttpConfiguration httpConfiguration =
        new HttpConfiguration(ImmutableList.of(extensionA, extensionB));

    // Assert
    verify(extensionA).initialize(httpConfiguration);
    verify(extensionB).initialize(httpConfiguration);
  }

}
