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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.springframework.core.io.Resource;
import com.github.springtestdbunit.dataset.AbstractDataSetLoader;

/**
 * Factory for custom test fixture data loader instances. This custom loader
 * configures automatic column sensing, which makes NULL values easier to
 * deal with, and registers some common replacement values:
 * <ul>
 * <li>${symbol_dollar}{NULL} will be replaced with NUll value
 * <li>${symbol_dollar}{SYSTIMESTAMP} will be replaced with current local date/time
 * <li>${symbol_dollar}{SYSDATE} will be replaced with the current local date
 * <li>${symbol_dollar}{SYSTIME} will be replaced with the current local time
 * </ul>
 */
public class TestDataSetLoader extends AbstractDataSetLoader {

	private static final SimpleDateFormat SYSTIMESTAMP = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S" );
	private static final SimpleDateFormat SYSDATE = new SimpleDateFormat( "yyyy-MM-dd" );
	private static final SimpleDateFormat SYSTIME = new SimpleDateFormat( "HH:mm:ss.S" );

	@Override
	protected IDataSet createDataSet( Resource resource ) throws Exception {
		InputStream in = resource.getInputStream();
		ReplacementDataSet ds = new ReplacementDataSet(
			new FlatXmlDataSetBuilder()
				.setColumnSensing( true )
				.build( in )
		);
	    ds.addReplacementObject( "${symbol_dollar}{NULL}", null );
	    Date now = new Date();
	    ds.addReplacementObject( "${symbol_dollar}{SYSTIMESTAMP}", SYSTIMESTAMP.format( now ) );
	    ds.addReplacementObject( "${symbol_dollar}{SYSDATE}", SYSDATE.format( now ) );
	    ds.addReplacementObject( "${symbol_dollar}{SYSTIME}", SYSTIME.format( now ) );

	    return ds;
	}
}