package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SelectBuilderTest {

  @Mock
  private RequestContext requestContext;

  @Mock
  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  @Mock
  private AliasManager aliasManager;

  @InjectMocks
  private SelectBuilder selectBuilder;
}
