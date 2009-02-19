/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import org.openrdf.repository.Repository;
import org.openrdf.repository.TupleQueryResultTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.postgresql.PgSqlStore;

public class PgSqlTupleQueryResultTest extends TupleQueryResultTest {

	protected Repository newRepository() {
		PgSqlStore sail = new PgSqlStore("sesame_test");
		sail.setUser("sesame");
		sail.setPassword("opensesame");
		return new SailRepository(sail);
	}
}
