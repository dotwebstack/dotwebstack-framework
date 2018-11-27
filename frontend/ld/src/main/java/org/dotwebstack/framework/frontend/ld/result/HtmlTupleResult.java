package org.dotwebstack.framework.frontend.ld.result;

import freemarker.template.Template;
import lombok.NonNull;
import org.apache.jena.rdf.model.Statement;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryResult;

public class HtmlTupleResult implements QueryResult<Statement> {

  QueryResult queryResult;
  Template template;

  public HtmlTupleResult(@NonNull QueryResult queryResult, @NonNull Template template) {
    this.queryResult = queryResult;
    this.template = template;
  }

  @Override
  public void close() throws QueryEvaluationException {
    try {
      this.queryResult.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean hasNext() throws QueryEvaluationException {
    try {
      return this.queryResult.hasNext();
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public Statement next() throws QueryEvaluationException {
    return null;
  }

  @Override
  public void remove() throws QueryEvaluationException {
    try {
      this.queryResult.remove();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}