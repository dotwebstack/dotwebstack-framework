package org.dotwebstack.framework.frontend.ld.parameter;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.parameter.source.ParameterSource;
import org.dotwebstack.framework.frontend.ld.parameter.target.Target;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.glassfish.jersey.uri.UriTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriParameterMapper extends AbstractParameterMapper {

  private static final Logger LOG = LoggerFactory.getLogger(UriParameterMapper.class);

  private static final IRI type = ELMO.URI_PARAMETER_MAPPER;

  private final String pattern;

  private final String template;

  protected UriParameterMapper(UriParameterMapperBuilder builder) {
    super(builder);
    template = builder.template;
    pattern = builder.pattern;
  }

  public static IRI getType() {
    return type;
  }

  @Override
  protected String parse(String input) {
    String output = input;

    if (pattern != null && template != null) {
      UriTemplate uriTemplatePattern = new UriTemplate(pattern);

      Map<String, String> map = new HashMap<>();
      if (uriTemplatePattern.match(input, map)) {
        UriTemplate replacement = new UriTemplate(template);
        output = replacement.createURI(map);
        LOG.debug("Pattern {} matched with value {}", pattern, input);
      } else {
        LOG.debug("Pattern {} not matched with value {}", pattern, input);
      }
    }

    return output;
  }

  public static class UriParameterMapperBuilder
      extends AbstractParameterMapper.Builder<UriParameterMapperBuilder> {
    private String pattern;

    private String template;

    public UriParameterMapperBuilder(@NonNull IRI identifier, @NonNull ParameterSource source,
        @NonNull Target target) {
      super(identifier, source, target);
    }

    public UriParameterMapperBuilder pattern(@NonNull String pattern) {
      this.pattern = pattern;
      return this;
    }

    public UriParameterMapperBuilder template(@NonNull String template) {
      this.template = template;
      return this;
    }

    public UriParameterMapper build() {
      return new UriParameterMapper(this);
    }
  }

}
