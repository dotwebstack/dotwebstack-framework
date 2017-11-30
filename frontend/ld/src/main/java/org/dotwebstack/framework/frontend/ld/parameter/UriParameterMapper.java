package org.dotwebstack.framework.frontend.ld.parameter;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.parameter.source.ParameterSource;
import org.dotwebstack.framework.frontend.ld.parameter.target.Target;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.model.IRI;
import org.glassfish.jersey.uri.UriTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriParameterMapper implements ParameterMapper {

  private static final Logger LOG = LoggerFactory.getLogger(UriParameterMapper.class);

  private IRI identifier;

  // bijv. http:requestIRI
  private ParameterSource source;

  // optioneel samen met template
  private String pattern;

  private String template;

  // bijv. parameter, als a TermFilter is, dan....
  private Target target;

  public Map<String, Object> map(@NonNull Representation representation,
      @NonNull ContainerRequestContext context) {
    Map<String, Object> result = new HashMap<>();

    String input = source.getValue();

    // optional processing
    if (pattern != null && template != null) {
      input = parse(input);
    }

    target.set(result, input);

    return result;
  }

  private String parse(String input) {

    String output = input;

    Map<String, String> map = new HashMap<String, String>();
    UriTemplate UriTemplatePattern = new UriTemplate(pattern);

    if (UriTemplatePattern.match(input, map)) {
      UriTemplate replacement = new UriTemplate(template);
      output = replacement.createURI(map);
    } else {
      LOG.debug("pattern not matched");
    }

    return output;
  }

  public static final class Builder {

    private IRI identifier;
    // bijv. http:requestIRI
    private ParameterSource source;
    // optioneel samen met template
    private String pattern;
    private String template;
    // bijv. parameter, als a TermFilter is, dan....
    private Target target;

    private Builder() {}

    public static Builder anUriParameterMapper() {
      return new Builder();
    }

    public Builder identifier(IRI identifier) {
      this.identifier = identifier;
      return this;
    }

    public Builder source(ParameterSource source) {
      this.source = source;
      return this;
    }

    public Builder pattern(String pattern) {
      this.pattern = pattern;
      return this;
    }

    public Builder template(String template) {
      this.template = template;
      return this;
    }

    public Builder target(Target target) {
      this.target = target;
      return this;
    }

    public UriParameterMapper build() {
      UriParameterMapper uriParameterMapper = new UriParameterMapper();
      uriParameterMapper.identifier = this.identifier;
      uriParameterMapper.source = this.source;
      uriParameterMapper.template = this.template;
      uriParameterMapper.pattern = this.pattern;
      uriParameterMapper.target = this.target;
      return uriParameterMapper;
    }
  }
}
