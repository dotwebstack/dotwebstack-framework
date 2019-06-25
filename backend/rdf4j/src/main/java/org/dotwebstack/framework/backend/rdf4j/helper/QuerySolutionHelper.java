package org.dotwebstack.framework.backend.rdf4j.helper;

import static org.dotwebstack.framework.backend.rdf4j.helper.MemStatementListHelper.listOf;

import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.query.QuerySolution;
import org.eclipse.rdf4j.sail.memory.model.MemResource;
import org.eclipse.rdf4j.sail.memory.model.MemStatement;
import org.eclipse.rdf4j.sail.memory.model.MemStatementList;

public class QuerySolutionHelper {

  public static List<MemStatement> getSubjectStatements(@NonNull QuerySolution querySolution) {
    MemStatementList subjectStatements = ((MemResource) querySolution.getSubject()).getSubjectStatementList();
    return listOf(subjectStatements);
  }
}
