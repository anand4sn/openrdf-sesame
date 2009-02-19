/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import java.util.Collections;
import java.util.Map;

import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.TupleExpr;

/**
 * A query forumalated in the OpenRDF query algebra that produces an RDF graph
 * (a set of statements) as its result.
 * 
 * @author Arjohn Kampman
 */
public class GraphQueryModel extends QueryModel {

	private static final long serialVersionUID = 2142924005034334523L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private Map<String, String> queryNamespaces;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new graph query. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 */
	public GraphQueryModel() {
		super();
	}

	/**
	 * Creates a new graph query. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 * 
	 * @param namespaces
	 *        A mapping of namespace prefixes to namespace names representing the
	 *        namespaces that are used in the query.
	 */
	public GraphQueryModel(Map<String, String> namespaces) {
		super();
		queryNamespaces = namespaces;
	}

	/**
	 * Creates a new graph query for the supplied tuple expression.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in Sail Query
	 *        Model objects.
	 */
	public GraphQueryModel(TupleExpr tupleExpr) {
		super(tupleExpr);
	}

	/**
	 * Creates a new graph query.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in Sail Query
	 *        Model objects.
	 * @param namespaces
	 *        A mapping of namespace prefixes to namespace names representing the
	 *        namespaces that are used in the query.
	 */
	public GraphQueryModel(TupleExpr tupleExpr, Map<String, String> namespaces) {
		this(tupleExpr);
		queryNamespaces = namespaces;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Map<String, String> getQueryNamespaces() {
		if (queryNamespaces != null) {
			return queryNamespaces;
		}
		else {
			return Collections.emptyMap();
		}
	}
}
