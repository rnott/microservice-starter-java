#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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

package ${package}.service;

import java.util.Map;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.testng.annotations.Test;

/**
 * Test that actuators are installed and functional. The following
 * actuators are tested:
 * <ul>
 * <li>Health
 * <li>Info
 * <li>Metrics
 * </ul>
 * The following actuators are tested to ensure they are disabled:
 * <ul>
 * <li>Env
 * </ul>
 */
public class ActuatorTest extends AbstractTest {

    public ActuatorTest() {
    	super( "actuator" );
    }

    @Test
    public void health() {
        Response response = getBaseTarget()
        	.path( "health" )
        	.request()
        	.get();
        assert response.getStatus() == Status.OK.getStatusCode() : "Unexpected HTTP status: " + response.getStatus();
        Map<String, Object> value = response.readEntity( new GenericType<Map<String, Object>>(){} );
        assert value != null : "Value is NULL";
        String status = (String) value.get( "status" );
        assert "UP".equals( status ) : "Unexpected service status: " + status;
    }

    @Test
    public void info() {
        Response response = getBaseTarget()
        	.path( "info" )
        	.request()
        	.get();
        assert response.getStatus() == Status.OK.getStatusCode()
        	: "Unexpected HTTP status: " + response.getStatus();
    }

    @Test
    public void env() {
        Response response = getBaseTarget()
        	.path( "env" )
        	.request()
        	.get();
        assert response.getStatus() == Status.NOT_FOUND.getStatusCode()
        	: "Unexpected HTTP status: " + response.getStatus();
    }

    @Test(enabled = false)  // TODO: metrics actuator is disabled in test
    public void metrics() {
        Response response = getBaseTarget()
        	.path( "metrics" )
        	.request()
        	.get();
        assert response.getStatus() == Status.OK.getStatusCode()
        	: "Unexpected HTTP status: " + response.getStatus();
    }
}
