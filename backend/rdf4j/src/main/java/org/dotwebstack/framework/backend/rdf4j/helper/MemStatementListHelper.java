package org.dotwebstack.framework.backend.rdf4j.helper;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.eclipse.rdf4j.sail.memory.model.MemStatement;
import org.eclipse.rdf4j.sail.memory.model.MemStatementList;

public class MemStatementListHelper {

  private MemStatementListHelper() {}

  public static List<MemStatement> listOf(@NonNull MemStatementList memStatementList) {
    List<MemStatement> result = new ArrayList<>();

    for (int i = 0; i < memStatementList.size(); i++) {
      result.add(memStatementList.get(i));
    }
    return result;
  }
}
