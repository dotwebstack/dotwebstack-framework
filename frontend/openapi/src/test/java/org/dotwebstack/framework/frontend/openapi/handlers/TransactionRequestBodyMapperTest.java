package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.container.ContainerRequestContext;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.rml.RmlMappingResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

@RunWith(MockitoJUnitRunner.class)
public class TransactionRequestBodyMapperTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private ContainerRequestContext contextMock;

  private RmlMappingResourceProvider rmlMappingResourceProvider;

  private TransactionRequestBodyMapper transactionRequestBodyMapper;

  private RequestParameters requestParameters;

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private ApplicationProperties applicationProperties;

  private SailRepository sailRepository;

  @Before
  public void setUp() throws IOException {
    rmlMappingResourceProvider = new RmlMappingResourceProvider(configurationBackend,
        applicationProperties);
    InputStream inputStream = new ClassPathResource("/rmlmapping/mapping.trig").getInputStream();
    sailRepository = new SailRepository(new MemoryStore());
    sailRepository.initialize();
    sailRepository.getConnection().add(inputStream, "", RDFFormat.TRIG);
    when(configurationBackend.getRepository()).thenReturn(sailRepository);

    rmlMappingResourceProvider.loadResources();
    transactionRequestBodyMapper = new TransactionRequestBodyMapper(rmlMappingResourceProvider);
  }

  @Test
  public void map_ReturnsCorrectParameterName_ForBodyParameter() {
    // Arrange
    Schema schema = new ObjectSchema();
    schema.setProperties(ImmutableMap.of("name", new StringSchema(), "beers",
        new ArraySchema()));

    RequestBody requestBody = new RequestBody();
    requestBody.setDescription("body");
    requestBody.addExtension(OpenApiSpecificationExtensions.RML_MAPPING,
        DBEERPEDIA.RML_MAPPING_NAME);
    requestBody.setContent(new Content().addMediaType("", new MediaType().schema(schema)));

    Operation operation = new Operation();
    operation.setRequestBody(requestBody);

    requestParameters = new RequestParameters();
    requestParameters.setRawBody("{\n"
        + "  \"name\":\"Davo Bieren Deventer\",\n"
        + "  \"beers\":[ \"Buzz Aldrin\", \"Nachtschade\", \"D`iesseltje\", \"Cafe Racer\" ]\n"
        + "}\n");

    // Act
    org.eclipse.rdf4j.model.Model resultModel = transactionRequestBodyMapper.map(operation,
        requestParameters);

    // Assert
    assertThat(resultModel.size(), equalTo(4));
  }

  @Test
  public void map_ThrowsException_WhenBodyParameterNotFound() {
    // Assert
    thrown.expect(RequestHandlerRuntimeException.class);

    // Act
    transactionRequestBodyMapper.map(new Operation(), new RequestParameters());
  }

  @Test
  public void map_ThrowsException_WhenRmlMappingExtensionNotfound() {
    // Arrange
    Schema schema = new ObjectSchema();
    schema.setProperties(ImmutableMap.of("name", new StringSchema(), "beers",
        new ArraySchema()));

    RequestBody requestBody = new RequestBody();
    requestBody.setDescription("body");
    requestBody.setContent(new Content().addMediaType("", new MediaType().schema(schema)));

    Operation operation = new Operation();
    operation.setRequestBody(requestBody);

    // Assert
    thrown.expect(RequestHandlerRuntimeException.class);

    // Act
    transactionRequestBodyMapper.map(operation, new RequestParameters());
  }

  @Test
  public void map_ThrowsBadRequestException_WhenBodyNotSet() {
    // Arrange
    Schema schema = new ObjectSchema();
    schema.setProperties(ImmutableMap.of("name", new StringSchema(), "beers",
        new ArraySchema()));

    RequestBody requestBody = new RequestBody();
    requestBody.setDescription("body");
    requestBody.addExtension(OpenApiSpecificationExtensions.RML_MAPPING,
        DBEERPEDIA.RML_MAPPING_NAME);
    requestBody.setContent(new Content().addMediaType("", new MediaType().schema(schema)));

    Operation operation = new Operation();
    operation.setRequestBody(requestBody);

    requestParameters = new RequestParameters();

    // Assert
    thrown.expect(BadRequestException.class);

    // Act
    transactionRequestBodyMapper.map(operation, requestParameters);
  }

}
