package org.dotwebstack.framework.informationproduct;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.filter.Filter;
import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractInformationProduct implements InformationProduct {

  protected final IRI identifier;

  protected final String label;

  protected final ResultType resultType;

  protected final Collection<Filter> filters;

  protected AbstractInformationProduct(@NonNull IRI identifier, String label,
      @NonNull ResultType resultType, @NonNull Collection<Filter> filters) {
    this.identifier = identifier;
    this.resultType = resultType;
    this.label = label;
    this.filters = ImmutableList.copyOf(filters);
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public ResultType getResultType() {
    return resultType;
  }

  @Override
  public Collection<Filter> getFilters() {
    return filters;
  }

}
