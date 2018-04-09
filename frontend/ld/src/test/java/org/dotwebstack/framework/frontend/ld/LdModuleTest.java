package org.dotwebstack.framework.frontend.ld;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScannerTest.StubGraphEntityWriter;
import org.dotwebstack.framework.frontend.ld.mappers.DirectEndPointRequestMapper;
import org.dotwebstack.framework.frontend.ld.mappers.DynamicEndPointRequestMapper;
import org.dotwebstack.framework.frontend.ld.mappers.LdRedirectionRequestMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LdModuleTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private HttpConfiguration httpConfiguration = new HttpConfiguration(ImmutableList.of());

  @Mock
  private DirectEndPointRequestMapper directEndPointRequestMapper;

  @Mock
  private DynamicEndPointRequestMapper dynamicEndPointRequestMapper;

  @Mock
  private LdRedirectionRequestMapper ldRedirectionRequestMapper;

  @Mock
  private SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  @Mock
  private SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  private LdModule ldModule;

  @Before
  public void setUp() {
    ldModule = new LdModule(dynamicEndPointRequestMapper, directEndPointRequestMapper,
        ldRedirectionRequestMapper, supportedWriterMediaTypesScanner,
        supportedReaderMediaTypesScanner);
    ldModule.initialize(httpConfiguration);
  }

  @Test
  public void constructor_RegistersSparqlProviders_WhenProvidedByScanner() {
    // Arrange
    when(supportedWriterMediaTypesScanner.getGraphEntityWriters()).thenReturn(
        Collections.singletonList(new StubGraphEntityWriter()));

    // Act
    ldModule.initialize(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getInstances(), hasSize(1));
  }

}
