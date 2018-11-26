package org.dotwebstack.framework.frontend.ld.result;

import freemarker.template.Template;
import lombok.NonNull;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryResult;

public class HtmlResult<T> implements QueryResult<Object> {

  QueryResult queryResult;
  Template template;

  public HtmlResult(@NonNull QueryResult queryResult, @NonNull Template template) {
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
  public Object next() throws QueryEvaluationException {
    try {
      return this.queryResult.next();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
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
