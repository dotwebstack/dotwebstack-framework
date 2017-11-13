package org.dotwebstack.framework.informationproduct.template;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TemplateProcessorTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private TemplateProcessor processor;

  @Before
  public void setUp() throws Exception {
    processor = new TemplateProcessor();
    processor.afterPropertiesSet();
  }

  @Test
  public void processString_ThrowsException_ForMissingParameter() {
    exception.expect(TemplateException.class);

    String templateString = "someEndpoint/${iDoNotExist}";

    processor.processString(templateString, null);
  }

  @Test
  public void processString_fillsTemplate_WithSingleParameter() {
    // Arrange
    String templateString = "someEndpoint/${id}";
    ImmutableMap<String, Object> parameters = ImmutableMap.of("id", "bestemmingsplan.1");

    // Act
    String result = processor.processString(templateString, parameters);

    // Assert
    assertThat(result, is("someEndpoint/bestemmingsplan.1"));
  }

  @Test
  public void processString_fillsTemplate_WithMultipleParameters() {
    // Arrange
    String templateString = "someEndpoint/${id}?randomParameter=${somePathParameter}";
    ImmutableMap<String, Object> parameters =
        ImmutableMap.of("id", "bestemmingsplan.1", "somePathParameter", "test");

    // Act
    String result = processor.processString(templateString, parameters);

    // Assert
    assertThat(result, is("someEndpoint/bestemmingsplan.1?randomParameter=test"));
  }

  @Test
  public void processString_fillsTemplate_NestedTemplates() {
    // Arrange (1)
    String templateString = "<#noparse>'${param1}</#noparse> + ${param2}'";
    ImmutableMap<String, Object> parameters1 = ImmutableMap.of("param2", "test1");

    // Act (1)
    String result1 = processor.processString(templateString, parameters1);

    // Assert (1)
    assertThat("'${param1} + test1'", is(result1));

    // Arrange (2)
    ImmutableMap<String, Object> parameters2 = ImmutableMap.of("param1", "test2");

    // Act (2)
    String result2 = processor.processString(result1, parameters2);

    // Assert (2)
    assertThat("'test2 + test1'", is(result2));
  }

}
