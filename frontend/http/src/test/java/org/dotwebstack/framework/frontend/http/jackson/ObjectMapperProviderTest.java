package org.dotwebstack.framework.frontend.http.jackson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.nullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ObjectMapperProviderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void constructor_DoesNotThrow_WhenConstructed() {
    // Act & Assert
    new ObjectMapperProvider();
  }

  @Test
  public void getContext_DoesNotThrow_WhenCalled() {
    // Arrange
    ObjectMapperProvider provider = new ObjectMapperProvider();

    // Act
    ObjectMapper mapper = provider.getContext(null);

    // Assert
    assertThat(mapper, is(not(nullValue())));
  }

}
