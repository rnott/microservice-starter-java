/*
 * Copyright 2016 Randy Nott
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rnott.example.service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

/**
 * Base class for testing REST service resources. This implementation
 * provides
 * <ul>
 * <li>Bootstrapping the service client
 * <li>Configuring the dataset fixture loader
 * <li>Configuring JSON support in the client
 * <li>Response status code categorization
 * </ul>
 */
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
    	"spring.jpa.show-sql=true",
    	"spring.jpa.properties.hibernate.format_sql=true"
    }
)
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@DbUnitConfiguration(dataSetLoader=TestDataSetLoader.class)
public class AbstractTest extends AbstractTestNGSpringContextTests {

	@LocalServerPort
    private int port;

    private final String basePath;

    private WebTarget webTarget;

    /**
     * Construct an instance using the specified service base path. The
     * base path is the portion of a resource path shared by all resources
     * of the service.
     * <p> 
     * @param basePath the service base path. For example,
     * http://localhost:8080/foo/bar might have a base path of 'foo'.
     */
	protected AbstractTest( String basePath ) {
		this.basePath = basePath;
	}

	/**
	 * Initialize the REST client to be used by tests. This method is
	 * invoked once prior to invocation of any test methods.
	 */
    @BeforeClass
    public void setup() {
        JacksonJsonProvider provider = new JacksonJsonProvider()
            .configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
        Client client = ClientBuilder.newClient( new ClientConfig( provider ) );
        webTarget = client
        	.target( "http://localhost:" + port )
        	.path( basePath );
    }

    /**
     * Determine the base web target in use. The web target is based
     * on the base path specified in the constructor.
     * <p>
     * @return the base web target.
     */
    protected WebTarget getBaseTarget() {
        return webTarget;
    }

    /**
     * Determine if a request resulted in a successful response.
     * <p>
     * @param response the response to check.
     * @return <code>true</code> if the response indicates success,
     * <code>false</code> otherwise.
     */
    protected boolean success( Response response ) {
    	int status = response.getStatus();
    	return status >= 200 && status < 300;
    }

    /**
     * Determine if a request resulted in a client error response.
     * <p>
     * @param response the response to check.
     * @return <code>true</code> if the response indicates a client error,
     * <code>false</code> otherwise.
     */
    protected boolean clientError( Response response ) {
    	int status = response.getStatus();
    	return status >= 400 && status < 500;
    }

    /**
     * Determine if a request resulted in a server error response.
     * <p>
     * @param response the response to check.
     * @return <code>true</code> if the response indicates a server error,
     * <code>false</code> otherwise.
     */
    protected boolean serverError( Response response ) {
    	int status = response.getStatus();
    	return status >= 500 && status < 600;
    }
}
