package org.dotwebstack.framework.service.openapi.jexl;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.datafetchers.paging.PagingConstants;
import org.dotwebstack.framework.core.jexl.JexlFunction;
import org.springframework.stereotype.Component;

@Component
public class PagingFunctions implements JexlFunction {

  private static final String NAMESPACE = "paging";

  private static final Pattern PAGE_PATTERN = Pattern.compile("page=([0-9]+)");

  @Override
  public String getNamespace() {
    return NAMESPACE;
  }

  @SuppressWarnings("unchecked")
  public String next(@NonNull Object data, @NonNull String pageSize, @NonNull String requestUri) {
    var nodes = ((Map<String, Object>) data).get(PagingConstants.NODES_FIELD_NAME);
    if (!(nodes instanceof Collection<?>)) {
      throw new InvalidConfigurationException("paging:next JEXL function used on un-pageable field");
    }

    if (((Collection<?>) nodes).size() == Integer.parseInt(pageSize)) {
      Matcher matcher = PAGE_PATTERN.matcher(requestUri);
      boolean found = matcher.find();
      if (found) {
        String group = matcher.group(1);
        int page = Integer.parseInt(group);
        return matcher.replaceFirst("page=" + (page + 1));
      } else {
        return requestUri.contains("?") ? requestUri + "&page=2" : requestUri + "?page=2";
      }
    }

    return null;
  }

  public String prev(@NonNull String requestUri) {
    Matcher matcher = PAGE_PATTERN.matcher(requestUri);
    boolean found = matcher.find();
    if (found) {
      String group = matcher.group(1);
      int page = Integer.parseInt(group);
      if (page >= 2) {
        return matcher.replaceFirst("page=" + (page - 1));
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
}
