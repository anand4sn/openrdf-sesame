// SES-1071 disabling rdbms-based tests
///*
// * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
// *
// * Licensed under the Aduna BSD-style license.
// */
//package org.openrdf.repository.sail.rdbms;
//
//import org.openrdf.repository.CascadeValueExceptionTest;
//import org.openrdf.repository.Repository;
//import org.openrdf.repository.sail.SailRepository;
//import org.openrdf.sail.rdbms.postgresql.PgSqlStore;
//
//public class PgSqlCascadeValueExceptionTest extends CascadeValueExceptionTest {
//
//	protected Repository newRepository() {
//		PgSqlStore sail = new PgSqlStore("sesame_test");
//		sail.setUser("sesame");
//		sail.setPassword("opensesame");
//		return new SailRepository(sail);
//	}
//
//}
