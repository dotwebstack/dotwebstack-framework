package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.dotwebstack.framework.param.template.TemplateProcessor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Test;

public class StringFilterFactoryTest {

  @Test
  public void newStringFilter_createsStringFilter_WithValidData() {
    TemplateProcessor templateProcessorMock = mock(TemplateProcessor.class);
    StringFilterFactory factory = new StringFilterFactory(templateProcessorMock);

    IRI identifier = SimpleValueFactory.getInstance().createIRI("http://foo#", "bar");
    String name = "name";

    StringFilter result = factory.newStringFilter(identifier, name);

    assertThat(result.getIdentifier(), is(identifier));
    assertThat(result.getName(), is(name));
  }

}
