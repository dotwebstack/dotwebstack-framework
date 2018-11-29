package org.dotwebstack.framework.frontend.ld.result;

import freemarker.template.Template;
import java.util.NoSuchElementException;
import lombok.NonNull;
import org.eclipse.rdf4j.common.iteration.AbstractCloseableIteration;
import org.eclipse.rdf4j.common.iteration.DistinctIteration;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryResult;

public class HtmlGraphResult
        extends AbstractCloseableIteration<Statement, QueryEvaluationException>
        implements QueryResult<Statement> {

  QueryResult queryResult;
  Template template;
  private final DistinctIteration<Statement, QueryEvaluationException> filter;


  public HtmlGraphResult(@NonNull QueryResult queryResult, @NonNull Template template) {
    this.queryResult = queryResult;
    this.template = template;
    this.filter = new DistinctIteration<Statement, QueryEvaluationException>(queryResult);
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
    if (isClosed()) {
      throw new NoSuchElementException("The iteration has been closed.");
    }

    try {
      return filter.next();
    } catch (NoSuchElementException e) {
      close();
      throw e;
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