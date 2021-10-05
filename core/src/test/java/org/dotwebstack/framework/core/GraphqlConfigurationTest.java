package org.dotwebstack.framework.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.FieldConfigurationImpl;
import org.dotwebstack.framework.core.config.TypeConfigurationImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphqlConfigurationTest {

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  private final GraphqlConfiguration graphqlConfiguration = new GraphqlConfiguration();

  @Test
  void createBlockPatterns_returnsList_forGivenConfiguration() {
    var objectType = new TypeConfigurationImpl();
    objectType.setName("testObject");

    var nameField = new FieldConfigurationImpl();
    nameField.setName("name");
    objectType.getFields()
        .put("name", nameField);

    var invisibleField = new FieldConfigurationImpl();
    invisibleField.setName("invisible");
    invisibleField.setVisible(false);
    objectType.getFields()
        .put("invisible", invisibleField);

    when(dotWebStackConfiguration.getObjectTypes()).thenReturn(Map.of("testObject", objectType));

    List<String> blockPatterns = graphqlConfiguration.createBlockPatterns(dotWebStackConfiguration);

    assertThat(blockPatterns, equalTo(List.of("testObject.invisible")));
  }
}
