package org.dotwebstack.framework.frontend.openapi.entity.builder;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.QueryResults;

public class QueryResult {

  private final Model model;
  private final ImmutableList<Resource> subjects;

  private final org.eclipse.rdf4j.query.QueryResult queryResultDb;
  private Set<Statement> statements = new HashSet<>();


  private QueryResult(Model model, ImmutableList<Resource> subjects,
      org.eclipse.rdf4j.query.QueryResult queryResultDb, Set<Statement> statements) {
    this.model = model;
    this.subjects = subjects;
    this.queryResultDb = queryResultDb;
    this.statements = statements;
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
    private final ImmutableList.Builder<Resource> contextsBuilder;
    private org.eclipse.rdf4j.query.QueryResult queryResultDb;

    public Builder() {
      model = new LinkedHashModel();
      contextsBuilder = new ImmutableList.Builder<>();
    }

    public Builder add(Model model) {
      this.model.addAll(model);
      return this;
    }

    public Builder add(Resource resource) {
      contextsBuilder.add(resource);
      return this;
    }

    public Builder withQueryResultDb(org.eclipse.rdf4j.query.QueryResult queryResultDb) {

      this.model = QueryResults.asModel(queryResultDb);
      this.queryResultDb = queryResultDb;
      return this;
    }

    public QueryResult build() {

      Set<Statement> statements = new HashSet<>();
      ImmutableList.Builder<Resource> listBuilder = ImmutableList.builder();
      try {
        while (this.queryResultDb.hasNext()) {
          Statement queryStatement = (Statement) this.queryResultDb.next();
          statements.add(queryStatement);
          listBuilder.add(queryStatement.getSubject());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }



      return new QueryResult(model, listBuilder.build(), this.queryResultDb, statements);
    }
  }

}
