/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 * Unit tests for SPARQL 1.1 Update functionality.
 * 
 * @author Jeen Broekstra
 */
public abstract class SPARQLUpdateTest extends TestCase {

	private Repository rep;

	private RepositoryConnection con;

	private ValueFactory f;

	private URI bob;

	private URI alice;

	private URI graph1;

	private URI graph2;
	
	protected static final String EX_NS = "http://example.org/";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		rep = createRepository();
		con = rep.getConnection();
		f = rep.getValueFactory();
		loadDataset();
		
		bob = f.createURI(EX_NS, "bob");
		alice = f.createURI(EX_NS, "alice");

		graph1 = f.createURI(EX_NS, "graph1");
		graph2 = f.createURI(EX_NS, "graph2");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
		con.close();
		con = null;

		rep.shutDown();
		rep = null;

		super.tearDown();
	}
	
	/* test methods */ 
	
	@Test
	public void testInsertWhere() throws Exception 
	{
		StringBuilder update = new StringBuilder();
		update.append(getNamespaceDeclarations());
		update.append("INSERT {?x rdfs:label ?y . } WHERE {?x foaf:name ?y }");
		
		Update operation = con.prepareUpdate(QueryLanguage.SPARQL, update.toString());

		assertFalse(con.hasStatement(bob, RDFS.LABEL, f.createLiteral("Bob"), true));
		assertFalse(con.hasStatement(alice, RDFS.LABEL, f.createLiteral("Alice"), true));

		operation.execute();
		
		assertTrue(con.hasStatement(bob, RDFS.LABEL, f.createLiteral("Bob"), true));
		assertTrue(con.hasStatement(alice, RDFS.LABEL, f.createLiteral("Alice"), true));
	}
	
	@Test
	public void testInsertWhereGraph() throws Exception 
	{
		StringBuilder update = new StringBuilder();
		update.append(getNamespaceDeclarations());
		update.append("INSERT {GRAPH ?g {?x rdfs:label ?y . }} WHERE {GRAPH ?g {?x foaf:name ?y }}");
		
		Update operation = con.prepareUpdate(QueryLanguage.SPARQL, update.toString());

		operation.execute();
			
		String message = "labels should have been inserted in corresponding named graphs only.";
		assertTrue(message, con.hasStatement(bob, RDFS.LABEL, f.createLiteral("Bob"), true, graph1));
		assertFalse(message, con.hasStatement(bob, RDFS.LABEL, f.createLiteral("Bob"), true, graph2));
		assertTrue(message, con.hasStatement(alice, RDFS.LABEL, f.createLiteral("Alice"), true, graph2));
		assertFalse(message, con.hasStatement(alice, RDFS.LABEL, f.createLiteral("Alice"), true, graph1));
	}

	@Test
	public void testInsertWhereUsing() throws Exception 
	{
		StringBuilder update = new StringBuilder();
		update.append(getNamespaceDeclarations());
		update.append("INSERT {?x rdfs:label ?y . } USING ex:graph1 WHERE {?x foaf:name ?y }");
		
		Update operation = con.prepareUpdate(QueryLanguage.SPARQL, update.toString());

		operation.execute();
				
		String message = "label should have been inserted in default graph, for ex:bob only";
		assertTrue(message, con.hasStatement(bob, RDFS.LABEL, f.createLiteral("Bob"), true));
		assertFalse(message, con.hasStatement(bob, RDFS.LABEL, f.createLiteral("Bob"), true, graph1));
		assertFalse(message, con.hasStatement(bob, RDFS.LABEL, f.createLiteral("Bob"), true, graph2));
		assertFalse(message, con.hasStatement(alice, RDFS.LABEL, f.createLiteral("Alice"), true, graph2));
		assertFalse(message, con.hasStatement(alice, RDFS.LABEL, f.createLiteral("Alice"), true, graph1));
	}
	
	@Test
	public void testInsertWhereWith() throws Exception 
	{
		StringBuilder update = new StringBuilder();
		update.append(getNamespaceDeclarations());
		update.append("WITH ex:graph1 INSERT {?x rdfs:label ?y . } WHERE {?x foaf:name ?y }");
		
		Update operation = con.prepareUpdate(QueryLanguage.SPARQL, update.toString());

		operation.execute();
				
		String message = "label should have been inserted in graph1 only, for ex:bob only";
		assertTrue(message, con.hasStatement(bob, RDFS.LABEL, f.createLiteral("Bob"), true, graph1));
		assertFalse(message, con.hasStatement(bob, RDFS.LABEL, f.createLiteral("Bob"), true, graph2));
		assertFalse(message, con.hasStatement(alice, RDFS.LABEL, f.createLiteral("Alice"), true, graph2));
		assertFalse(message, con.hasStatement(alice, RDFS.LABEL, f.createLiteral("Alice"), true, graph1));
	}
	
	@Test
	public void testDeleteWhere() throws Exception 
	{
		StringBuilder update = new StringBuilder();
		update.append(getNamespaceDeclarations());
		update.append("DELETE {?x foaf:name ?y } WHERE {?x foaf:name ?y }");
		
		Update operation = con.prepareUpdate(QueryLanguage.SPARQL, update.toString());

		assertTrue(con.hasStatement(bob, FOAF.NAME, f.createLiteral("Bob"), true));
		assertTrue(con.hasStatement(alice, FOAF.NAME, f.createLiteral("Alice"), true));

		operation.execute();

		String msg = "foaf:name properties should have been deleted";
		assertFalse(msg, con.hasStatement(bob, FOAF.NAME, f.createLiteral("Bob"), true));
		assertFalse(msg, con.hasStatement(alice, FOAF.NAME, f.createLiteral("Alice"), true));

	}
	
	@Test
	public void testDeleteTransformedWhere() throws Exception 
	{
		StringBuilder update = new StringBuilder();
		update.append(getNamespaceDeclarations());
		update.append("DELETE {?y foaf:name [] } WHERE {?x ex:containsPerson ?y }");
		
		Update operation = con.prepareUpdate(QueryLanguage.SPARQL, update.toString());

		assertTrue(con.hasStatement(bob, FOAF.NAME, f.createLiteral("Bob"), true));
		assertTrue(con.hasStatement(alice, FOAF.NAME, f.createLiteral("Alice"), true));

		operation.execute();

		String msg = "foaf:name properties should have been deleted";
		assertFalse(msg, con.hasStatement(bob, FOAF.NAME, f.createLiteral("Bob"), true));
		assertFalse(msg, con.hasStatement(alice, FOAF.NAME, f.createLiteral("Alice"), true));
		
		msg = "ex:containsPerson properties should not have been deleted";
		assertTrue(msg, con.hasStatement(graph1, f.createURI(EX_NS, "containsPerson"), bob, true));
		assertTrue(msg, con.hasStatement(graph2, f.createURI(EX_NS, "containsPerson"), alice, true));

	}
	
	/* protected methods */
		
	protected void loadDataset() throws RDFParseException, RepositoryException, IOException {
		InputStream dataset = SPARQLUpdateTest.class.getResourceAsStream("/testdata-update/dataset-update.trig");
		try {
			con.add(dataset, "", RDFFormat.TRIG);
		}
		finally {
			dataset.close();
		}
	}
	
	/**
	 * Get a set of useful namespace prefix declarations.
	 * 
	 * @return namespace prefix declarations for rdf, rdfs, dc, foaf and ex.
	 */
	protected String getNamespaceDeclarations() {
		StringBuilder declarations = new StringBuilder();
		declarations.append("PREFIX rdf: <" + RDF.NAMESPACE + "> \n");
		declarations.append("PREFIX rdfs: <" + RDFS.NAMESPACE + "> \n");
		declarations.append("PREFIX dc: <" + DC.NAMESPACE + "> \n");
		declarations.append("PREFIX foaf: <" + FOAF.NAMESPACE + "> \n");
		declarations.append("PREFIX ex: <" + EX_NS + "> \n");
		declarations.append("\n");
		
		return declarations.toString();
	}
	
	/**
	 * Creates, initializes and clears a repository.
	 * 
	 * @return an initialized empty repository.
	 * @throws Exception
	 */
	protected Repository createRepository()
		throws Exception
	{
		Repository repository = newRepository();
		repository.initialize();
		RepositoryConnection con = repository.getConnection();
		con.clear();
		con.clearNamespaces();
		con.close();
		return repository;
	}

	/**
	 * Create a new Repository object. Subclasses are expected to implement this
	 * method to supply the test case with a specific Repository type and
	 * configuration.
	 * 
	 * @return a new (uninitialized) Repository
	 * @throws Exception
	 */
	protected abstract Repository newRepository()
		throws Exception;
}
