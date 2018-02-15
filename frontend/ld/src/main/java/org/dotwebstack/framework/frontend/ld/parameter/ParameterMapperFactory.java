package org.dotwebstack.framework.frontend.ld.parameter;

import java.util.Optional;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.parameter.source.ParameterSourceFactory;
import org.dotwebstack.framework.frontend.ld.parameter.target.TargetFactory;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParameterMapperFactory {

  private ParameterSourceFactory parameterSourceFactory;

  private TargetFactory parameterTargetFactory;

  @Autowired
  public ParameterMapperFactory(ParameterSourceFactory parameterSourceFactory,
      TargetFactory parameterTargetFactory) {
    this.parameterSourceFactory = parameterSourceFactory;
    this.parameterTargetFactory = parameterTargetFactory;
  }

  public ParameterMapper create(IRI parameterMapperType, Model model, Resource identifier) {

    IRI sourceIri = getObjectIRI(model, identifier, ELMO.SOURCE_PROP).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> statement has been found for parametermapper <%s>.",
                ELMO.SOURCE_PROP, identifier)));

    IRI targetIri = getObjectIRI(model, identifier, ELMO.TARGET_PROP).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> statement has been found for parametermapper <%s>.",
                ELMO.TARGET_PROP, identifier)));

    Optional<String> pattern = getObjectString(model, identifier, ELMO.PATTERN_PROP);
    Optional<String> template = getObjectString(model, identifier, ELMO.TEMPLATE_PROP);

    if (pattern.isPresent() != template.isPresent()) {
      throw new ConfigurationException(
          String.format("If present, both %s and %s are required, for parametermapper <%s>.",
              ELMO.TEMPLATE_PROP, ELMO.TARGET_PROP, identifier));
    }

    if (UriParameterMapper.getType().equals(parameterMapperType)) {
      UriParameterMapper.UriParameterMapperBuilder builder =
          new UriParameterMapper.UriParameterMapperBuilder((IRI) identifier,
              parameterSourceFactory.getParameterSource(sourceIri),
              parameterTargetFactory.getTarget(targetIri));

      pattern.ifPresent(builder::pattern);
      template.ifPresent(builder::template);

      return builder.build();
    }

    throw new ConfigurationException(
        String.format("No parametermapper available for type <%s>.", parameterMapperType));
  }

  private Optional<String> getObjectString(Model model, Resource subject, IRI predicate) {
    return Models.objectString(model.filter(subject, predicate, null));
  }

  private Optional<IRI> getObjectIRI(Model model, Resource subject, IRI predicate) {
    return Models.objectIRI(model.filter(subject, predicate, null));
  }

}
