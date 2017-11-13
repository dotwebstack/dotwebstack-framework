package org.dotwebstack.framework.frontend.openapi.entity.builder;

import com.google.common.collect.ImmutableList;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.QueryResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryResult {
  private static final Logger LOG = LoggerFactory.getLogger(QueryResult.class);

  private final Model model;
  private final ImmutableList<Resource> subjects;

  private QueryResult(Model model, ImmutableList<Resource> subjects) {
    this.model = model;
    this.subjects = subjects;
  }

  public Model getModel() {
    return model;
  }


  public ImmutableList<Resource> getSubjects() {
    return this.subjects;

  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Model model;
    private org.eclipse.rdf4j.query.QueryResult queryResultDb;

    public Builder() {
      model = new LinkedHashModel();
    }

    public Builder withQueryResultTriple(org.eclipse.rdf4j.query.QueryResult queryResultDb) {
      this.queryResultDb = queryResultDb;
      return this;
    }

    public Builder withQueryResultGraph(org.eclipse.rdf4j.query.QueryResult queryResultDb) {
      this.model = QueryResults.asModel(queryResultDb);
      this.queryResultDb = queryResultDb;
      return this;
    }

    public QueryResult build() {

      ImmutableList.Builder<Resource> listBuilder = ImmutableList.builder();
      try {
        while (this.queryResultDb.hasNext()) {

          if (this.queryResultDb.next() instanceof Statement) {
            Statement queryStatement = (Statement) this.queryResultDb.next();
            listBuilder.add(queryStatement.getSubject());
          }
        }
      } catch (Exception e) {
        LOG.error("Could not get subjects from queryresult.", e);
      }
      return new QueryResult(model, listBuilder.build());
    }

  }

}
