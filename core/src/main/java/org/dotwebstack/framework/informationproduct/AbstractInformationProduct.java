package org.dotwebstack.framework.informationproduct;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.filter.Filter;
import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractInformationProduct implements InformationProduct {

  protected final IRI identifier;

  protected final String label;

  protected final ResultType resultType;

  protected final Collection<Filter> requiredFilters;

  protected final Collection<Filter> optionalFilters;

  protected AbstractInformationProduct(@NonNull IRI identifier, String label,
      @NonNull ResultType resultType, @NonNull Collection<Filter> requiredFilters,
      @NonNull Collection<Filter> optionalFilters) {
    this.identifier = identifier;
    this.resultType = resultType;
    this.label = label;
    this.requiredFilters = ImmutableList.copyOf(requiredFilters);
    this.optionalFilters = ImmutableList.copyOf(optionalFilters);
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
    List<Filter> result = new ArrayList<>(requiredFilters);

    result.addAll(optionalFilters);

    return result;
  }

  @Override
  public Object getResult(@NonNull Map<String, String> values) {
    for (Filter filter : requiredFilters) {
      if (values.get(filter.getName()) == null) {
        throw new BackendException(
            String.format("No value found for required filter '%s'. Supplied values: %s",
                filter.getName(), values));
      }
    }

    return getInnerResult(values);
  }

  protected abstract Object getInnerResult(@NonNull Map<String, String> values);

}
