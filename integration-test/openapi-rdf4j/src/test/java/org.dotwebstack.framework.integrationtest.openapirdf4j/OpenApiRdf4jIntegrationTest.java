package org.dotwebstack.framework.integrationtest.openapirdf4j;

import org.dotwebstack.framework.test.TestApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SuppressWarnings("unchecked")
@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenApiRdf4jIntegrationTest {

}
