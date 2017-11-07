package org.dotwebstack.framework.frontend.openapi.entity.builder;

import java.util.HashSet;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

public class QueryResult {

  private final Model model;
  private final ImmutableList<Resource> subjects;
  private final int totalHits;

  private final org.eclipse.rdf4j.query.QueryResult queryResultDb;

  static final QueryResult EMPTY_RESULT =
      new QueryResult(new LinkedHashModel(), ImmutableList.of(), 0, null,new HashSet<>());
  private Set<Statement> statements = new HashSet<>();


  private QueryResult(Model model, ImmutableList<Resource> subjects, int totalHits,
                      org.eclipse.rdf4j.query.QueryResult queryResultDb, Set<Statement> statements) {
    this.model = model;
    this.subjects = subjects;
    this.totalHits = totalHits;
    this.queryResultDb = queryResultDb;
    this.statements= statements;
  }

  public Model getModel() {
    return model;
  }

  public ImmutableList<Resource> getSubjects() {
    return this.subjects;

  }

  public int getTotalHits() {
    return this.totalHits;
  }

  public static Builder builder() {
    return new Builder();
  }

  public org.eclipse.rdf4j.query.QueryResult getQueryResultDb() {
    return queryResultDb;
  }

  public Set<Statement> getStatements() {
    return statements;
  }


  public static class Builder {

    private final Model model;
    private final ImmutableList.Builder<Resource> contextsBuilder;
    private int totalHits;
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
      this.queryResultDb = queryResultDb;
      return this;
    }

    public Builder withTotalHits(int totalHits) {
      this.totalHits = totalHits;
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





      return new QueryResult(model, listBuilder.build(), this.totalHits, this.queryResultDb,statements);
    }
  }

}
