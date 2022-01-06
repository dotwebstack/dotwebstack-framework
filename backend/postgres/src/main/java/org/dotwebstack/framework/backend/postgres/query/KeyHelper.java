package org.dotwebstack.framework.backend.postgres.query;

import java.util.concurrent.atomic.AtomicReference;

import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.ObjectRequest;

import static org.dotwebstack.framework.backend.postgres.query.SortHelper.findOrAddObjectRequest;
import static org.dotwebstack.framework.backend.postgres.query.SortHelper.findOrAddScalarField;

final class KeyHelper {

  private KeyHelper() {}

  static void addKeyFields(ObjectRequest objectRequest) {
    var keyCriteria = objectRequest.getKeyCriteria();

    final AtomicReference<ObjectRequest> myObjectRequest = new AtomicReference<>(objectRequest);

    keyCriteria.getValues().forEach((fieldPath, value) -> {
      for (int index = 0; index < fieldPath.size(); index++) {
        ObjectField sortField = fieldPath.get(index);

        if (index == (fieldPath.size() - 1)) {
          findOrAddScalarField(myObjectRequest.get(), sortField);
        } else {
          ObjectField nextSortField = fieldPath.get(index + 1);
          myObjectRequest.set(findOrAddObjectRequest(myObjectRequest.get().getObjectFields(), sortField, nextSortField));
        }
      }
    });
  }
}

