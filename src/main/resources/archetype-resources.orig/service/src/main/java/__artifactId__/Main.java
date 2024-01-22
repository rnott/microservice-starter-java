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

package ${package}.${artifactId};

import org.glassfish.jersey.server.ResourceConfig;
import ${package}.${artifactId}.foo.FooResource;
import ${package}.${artifactId}.model.ExpansionRepositoryImpl;
import ${package}.${artifactId}.util.AuthenticationExceptionMapper;
import ${package}.${artifactId}.util.AuthorizationExceptionMapper;
import ${package}.${artifactId}.util.ConstraintViolationExceptionMapper;
import ${package}.${artifactId}.util.DefaultExceptionMapper;
import ${package}.${artifactId}.util.FieldParseExceptionMapper;
import ${package}.${artifactId}.util.IllegalStateExceptionMapper;
import ${package}.${artifactId}.util.NoResultExceptionMapper;
import ${package}.${artifactId}.util.ClientExceptionMapper;
import ${package}.${artifactId}.util.TransactionSystemExceptionMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan
@SpringBootApplication
// use a custom JPA data repository
@EnableJpaRepositories(repositoryBaseClass = ExpansionRepositoryImpl.class)
public class Main  extends SpringBootServletInitializer {

	/**
	 * Register JAX-RS resources.
	 * <p>
	 * @return JAX-RS resource configuration.
	 */
	@Bean
	public ResourceConfig configureResources() {
		return new ResourceConfig()
			// TODO: register resource classes here
			.register( FooResource.class )

			// register exception handlers here
			// NOTE: you can safely remove handlers if you know the
			//       exception will never be thrown
			.register( AuthenticationExceptionMapper.class )
			.register( AuthorizationExceptionMapper.class )
			.register( ConstraintViolationExceptionMapper.class )
			.register( DefaultExceptionMapper.class )
			.register( FieldParseExceptionMapper.class )
			.register( IllegalStateExceptionMapper.class )
			.register( NoResultExceptionMapper.class )
			.register( ClientExceptionMapper.class )
			.register( TransactionSystemExceptionMapper.class );
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
