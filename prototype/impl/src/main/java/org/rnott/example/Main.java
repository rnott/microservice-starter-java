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

package org.rnott.example;

import org.glassfish.jersey.server.ResourceConfig;
import org.rnott.example.feature.HypermediaFilter;
import org.rnott.example.persistence.NoResultExceptionMapper;
import org.rnott.example.problems.BadRequestExceptionMapper;
import org.rnott.example.problems.ConstraintViolationExceptionMapper;
import org.rnott.example.problems.DefaultExceptionMapper;
import org.rnott.example.problems.ForbiddenExceptionMapper;
import org.rnott.example.problems.InternalServerErrorExceptionMapper;
import org.rnott.example.problems.JsonMappingExceptionMapper;
import org.rnott.example.problems.JsonParseExceptionMapper;
import org.rnott.example.problems.NotAcceptableExceptionMapper;
import org.rnott.example.problems.NotAllowedExceptionMapper;
import org.rnott.example.problems.NotAuthorizedExceptionMapper;
import org.rnott.example.problems.NotFoundExceptionMapper;
import org.rnott.example.problems.JakartaNotFoundExceptionMapper;
import org.rnott.example.problems.NotSupportedExceptionMapper;
import org.rnott.example.problems.ObjectOptimisticLockingFailureExceptionMapper;
import org.rnott.example.problems.ServiceUnavailableExceptionMapper;
import org.rnott.example.problems.ValidationExceptionMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@SpringBootApplication
public class Main  extends SpringBootServletInitializer {

	/**
	 * Register JAX-RS resources.
	 * <p>
	 * @return JAX-RS resource configuration.
	 */
	@Bean
	public ResourceConfig configureResources() {
		return new ResourceConfig()
				// register resource classes here
				.register( ExampleApiImpl.class )
				// hypermedia support (HATEOAS)
				.register(HypermediaFilter.class)
				// register provided exception handlers here
				.register( BadRequestExceptionMapper.class )
				.register( ConstraintViolationExceptionMapper.class )
				.register( DefaultExceptionMapper.class )
				.register( ForbiddenExceptionMapper.class )
				.register(InternalServerErrorExceptionMapper.class)
				.register( NotAcceptableExceptionMapper.class )
				.register( NotAllowedExceptionMapper.class )
				.register( NotAuthorizedExceptionMapper.class )
				.register( NotFoundExceptionMapper.class )
				.register(JakartaNotFoundExceptionMapper.class)
				.register( NotSupportedExceptionMapper.class )
				.register(ServiceUnavailableExceptionMapper.class)
				.register(ObjectOptimisticLockingFailureExceptionMapper.class)
				// should only be registered if JPA is used
				.register( NoResultExceptionMapper.class )
				.register(JsonParseExceptionMapper.class)
				.register(JsonMappingExceptionMapper.class)
				.register(ValidationExceptionMapper.class);
				// TODO: register any application exception handlers here
	}

    /**
     * Application entry point.
     * <p>
     * @param args command line arguments to apply.
     */
    public static void main( String [] args ) {
		new Main()
			.configure( new SpringApplicationBuilder( Main.class ) )
			.run( args );
	}
}
