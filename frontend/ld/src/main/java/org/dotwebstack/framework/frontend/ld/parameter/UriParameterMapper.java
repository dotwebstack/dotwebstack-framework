package org.dotwebstack.framework.frontend.ld.parameter;

import org.dotwebstack.framework.frontend.ld.parameter.source.ParameterSource;
import org.dotwebstack.framework.frontend.ld.parameter.target.Target;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;

public class UriParameterMapper extends ParameterMapper {

  private static final IRI type = ELMO.URI_PARAMETER_MAPPER;

  private String pattern;

  private String template;

  public static IRI getType() {
    return type;
  }

  public static class Builder extends ParameterMapper.Builder<Builder> {

    private String pattern;
    private String template;

    public Builder(IRI identifier, ParameterSource source, Target target) {
      super(identifier, source, target);
    }

    public Builder pattern(String pattern) {
      this.pattern = pattern;
      return this;
    }

    public Builder template(String template) {
      this.template = template;
      return this;
    }

    public UriParameterMapper build() {
      return new UriParameterMapper(this);
    }
  }

  protected UriParameterMapper(Builder builder) {
    super(builder);
    template = builder.template;
    pattern = builder.pattern;
  }

}
