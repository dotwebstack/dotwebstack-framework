package org.dotwebstack.framework.backend.rdf4j.serializers;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SerializerRouter {

  private final List<Serializer> serializers;

  public SerializerRouter(List<Serializer> serializers) {
    this.serializers = serializers;
  }

  public String serialize(Object object) {
    Optional<Serializer> compatibleSerializer = serializers.stream()
        .filter(serializer -> serializer.supports(object))
        .findFirst();

    return compatibleSerializer.isPresent() ? compatibleSerializer.get()
        .serialize(object) : object.toString();
  }
}
