package org.dotwebstack.framework.backend.rdf4j.serializers;

import lombok.NonNull;

public interface Serializer {

  boolean supports(@NonNull Object object);

  String serialize(@NonNull Object object);

}
