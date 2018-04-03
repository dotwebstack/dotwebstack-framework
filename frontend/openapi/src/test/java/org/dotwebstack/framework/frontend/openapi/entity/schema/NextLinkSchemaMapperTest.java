package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.atlassian.oai.validator.model.NormalisedPath;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.swagger.models.Operation;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ObjectProperty;
import java.net.URI;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.term.IntegerTermParameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NextLinkSchemaMapperTest {

  private final String baseUri = "http://localhost:8080/api/v2";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private TupleEntity tupleEntityMock;

  @Mock
  private GraphEntity graphEntityMock;

  @Mock
  private ValueContext valueContextMock;

  @Mock
  private RequestContext requestContextMock;

  @Mock
  private InformationProduct informationProductMock;

  @Mock
  private SchemaMapperAdapter schemaMapperAdapterMock;

  @Mock
  private ApiOperation apiOperationMock;

  @Mock
  private Operation operationMock;

  @Mock
  private NormalisedPath requestPathMock;

  private NextLinkSchemaMapper schemaMapper;

  private ObjectProperty schema;

  private QueryParameter pageParameter;

  private QueryParameter pageSizeParameter;

  @Before
  public void setUp() {
    schemaMapper = new NextLinkSchemaMapper();

    schema = new ObjectProperty();
    schema.setVendorExtension(OpenApiSpecificationExtensions.TYPE,
        OpenApiSpecificationExtensions.TYPE_SELF_LINK);

    pageParameter = new QueryParameter().name("p");
    pageParameter.setVendorExtension(OpenApiSpecificationExtensions.PARAMETER,
        ELMO.PAGE_PARAMETER.stringValue());

    pageSizeParameter = new QueryParameter().name("ps");
    pageSizeParameter.setVendorExtension(OpenApiSpecificationExtensions.PARAMETER,
        ELMO.PAGE_SIZE_PARAMETER.stringValue());

    when(operationMock.getParameters()).thenReturn(
        ImmutableList.of(pageParameter, pageSizeParameter));
    when(apiOperationMock.getRequestPath()).thenReturn(requestPathMock);
    when(apiOperationMock.getOperation()).thenReturn(operationMock);

    when(informationProductMock.getParameters()).thenReturn(
        ImmutableSet.of(new IntegerTermParameter(ELMO.PAGE_PARAMETER, "page", false, 1),
            new IntegerTermParameter(ELMO.PAGE_SIZE_PARAMETER, "pageSize", false, 2)));

    when(requestContextMock.getBaseUri()).thenReturn(baseUri);
    when(requestContextMock.getApiOperation()).thenReturn(apiOperationMock);
    when(requestContextMock.getInformationProduct()).thenReturn(informationProductMock);

    when(graphEntityMock.getRequestContext()).thenReturn(requestContextMock);
  }

  @Test
  public void mapTupleValue_ThrowsException_WhenInvoked() {
    // Assert
    thrown.expect(UnsupportedOperationException.class);

    // Act
    schemaMapper.mapTupleValue(schema, tupleEntityMock, valueContextMock);
  }

  @Test
  public void mapGraphValue_ReturnsNextLink_WhenSubjectsSizeEqualsPageSize() {
    // Arrange
    when(requestPathMock.normalised()).thenReturn("/breweries");
    when(graphEntityMock.getSubjects()).thenReturn(
        ImmutableSet.of(DBEERPEDIA.BROUWTOREN, DBEERPEDIA.MAXIMUS));

    // Act
    Object result = schemaMapper.mapGraphValue(schema, graphEntityMock, valueContextMock,
        schemaMapperAdapterMock);

    // Assert
    assertThat(result,
        equalTo(SchemaMapperUtils.createLink(URI.create(baseUri + "/breweries?p=2"))));
  }

  @Test
  public void mapGraphValue_ReturnsNull_WhenSubjectsSizeIsLessThanPageSize() {
    // Arrange
    when(graphEntityMock.getSubjects()).thenReturn(ImmutableSet.of(DBEERPEDIA.BROUWTOREN));

    // Act
    Object result = schemaMapper.mapGraphValue(schema, graphEntityMock, valueContextMock,
        schemaMapperAdapterMock);

    // Assert
    assertThat(result, is(nullValue()));
  }

}
