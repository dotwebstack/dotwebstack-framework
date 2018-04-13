package org.dotwebstack.framework;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.frontend.http.layout.LayoutResourceProvider;
import org.dotwebstack.framework.frontend.http.site.SiteResourceProvider;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndpointResourceProvider;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndpointResourceProvider;
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapperResourceProvider;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.frontend.ld.service.ServiceResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.TransactionResourceProvider;
import org.dotwebstack.framework.transaction.flow.step.StepResourceProvider;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConfigurationIntegrationTest {

  @Autowired
  private SiteResourceProvider siteResourceProvider;

  @Autowired
  private StageResourceProvider stageResourceProvider;

  @Autowired
  private BackendResourceProvider backendResourceProvider;

  @Autowired
  private InformationProductResourceProvider informationProductResourceProvider;

  @Autowired
  private TransactionResourceProvider transactionResourceProvider;

  @Autowired
  private RepresentationResourceProvider representationResourceProvider;

  @Autowired
  private ParameterMapperResourceProvider parameterMapperResourceProvider;

  @Autowired
  private LayoutResourceProvider layoutResourceProvider;

  @Autowired
  private DirectEndpointResourceProvider directEndpointResourceProvider;

  @Autowired
  private DynamicEndpointResourceProvider dynamicEndpointResourceProvider;

  @Autowired
  private StepResourceProvider stepResourceProvider;

  @Autowired
  private ServiceResourceProvider serviceResourceProvider;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Test
  public void resources_ConfigurationLoaded_WhenApplicationStarted() {
    assertThat(siteResourceProvider.getAll().entrySet(), hasSize(1));
    assertThat(siteResourceProvider.get(DBEERPEDIA.SITE), notNullValue());
    assertThat(stageResourceProvider.getAll().entrySet(), hasSize(1));
    assertThat(stageResourceProvider.get(DBEERPEDIA.STAGE), notNullValue());
    assertThat(backendResourceProvider.getAll().entrySet(), hasSize(1));
    assertThat(backendResourceProvider.get(DBEERPEDIA.BACKEND), instanceOf(SparqlBackend.class));
    assertThat(informationProductResourceProvider.getAll().entrySet(), hasSize(4));
    assertThat(informationProductResourceProvider.get(DBEERPEDIA.TUPLE_BREWERIES), notNullValue());
    assertThat(informationProductResourceProvider.get(DBEERPEDIA.GRAPH_BREWERIES), notNullValue());
    assertThat(transactionResourceProvider.getAll().entrySet(), hasSize(2));
    assertThat(transactionResourceProvider.get(DBEERPEDIA.TRANSACTION), notNullValue());
    assertThat(representationResourceProvider.getAll().entrySet(), hasSize(6));
    assertThat(representationResourceProvider.get(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION),
        notNullValue());
    assertThat(representationResourceProvider.get(DBEERPEDIA.TUPLE_BREWERY_LIST_REPRESENTATION),
        notNullValue());
    assertThat(representationResourceProvider.get(DBEERPEDIA.BREWERY_REPRESENTATION),
        notNullValue());
    assertThat(parameterMapperResourceProvider.getAll().entrySet(), hasSize(1));
    assertThat(parameterMapperResourceProvider.get(DBEERPEDIA.SUBJECT_FROM_URL), notNullValue());
    assertThat(layoutResourceProvider.getAll().entrySet(), hasSize(2));
    assertThat(layoutResourceProvider.get(DBEERPEDIA.LAYOUT), notNullValue());
    assertThat(layoutResourceProvider.get(DBEERPEDIA.LAYOUT_NL), notNullValue());
    String cssResource = layoutResourceProvider.get(DBEERPEDIA.LAYOUT).getOptions().get(
        valueFactory.createIRI("http://www.w3.org/1999/xhtml/vocab#stylesheet")).stringValue();
    assertThat(cssResource, equalTo("stage-layout.css"));
    assertThat(directEndpointResourceProvider.getAll().entrySet(), hasSize(8));
    assertThat(dynamicEndpointResourceProvider.getAll().entrySet(), hasSize(1));
    assertThat(dynamicEndpointResourceProvider.get(DBEERPEDIA.DOC_ENDPOINT), notNullValue());
    assertThat(dynamicEndpointResourceProvider.get(DBEERPEDIA.DOC_ENDPOINT).getLabel(),
        notNullValue());
    assertThat(directEndpointResourceProvider.get(DBEERPEDIA.DEFAULT_ENDPOINT), notNullValue());
    assertThat(stepResourceProvider.getAll().entrySet(), hasSize(8));
    assertThat(stepResourceProvider.get(DBEERPEDIA.PERSISTENCE_STEP), notNullValue());
    assertThat(serviceResourceProvider.getAll().entrySet(), hasSize(3));
    assertThat(stepResourceProvider.get(DBEERPEDIA.ASSERTION_IF_EXIST_STEP), notNullValue());
    assertThat(stepResourceProvider.get(DBEERPEDIA.ASSERTION_IF_NOT_EXIST_STEP), notNullValue());
    assertThat(stepResourceProvider.get(DBEERPEDIA.PRE_UPDATE_STEP), notNullValue());
    assertThat(stepResourceProvider.get(DBEERPEDIA.POST_UPDATE_STEP), notNullValue());
    assertThat(stepResourceProvider.get(DBEERPEDIA.VALIDATION_STEP), notNullValue());
  }

}
