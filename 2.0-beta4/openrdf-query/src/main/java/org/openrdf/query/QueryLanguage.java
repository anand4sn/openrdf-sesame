/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A type-safe enumeration for RDF query languages.
 */
public class QueryLanguage {

	/*-----------*
	 * Constants *
	 *-----------*/

	public static final QueryLanguage SERQL = new QueryLanguage("SeRQL");

	public static final QueryLanguage SPARQL = new QueryLanguage("SPARQL");

	public static final QueryLanguage SERQO = new QueryLanguage("SeRQO");

	/*------------------*
	 * Static variables *
	 *------------------*/

	/**
	 * List of known query languages.
	 */
	private static List<QueryLanguage> QUERY_LANGUAGES = new ArrayList<QueryLanguage>(4);

	/*--------------------*
	 * Static initializer *
	 *--------------------*/

	static {
		register(SERQL);
		register(SPARQL);
		register(SERQO);
	}

	/*----------------*
	 * Static methods *
	 *----------------*/

	/**
	 * Returns all known/registered query languages.
	 */
	public static Collection<QueryLanguage> values() {
		return Collections.unmodifiableList(QUERY_LANGUAGES);
	}

	/**
	 * Registers the specified query language.
	 * 
	 * @param name
	 *        The name of the query language, e.g. "SPARQL".
	 */
	public static QueryLanguage register(String name) {
		QueryLanguage ql = new QueryLanguage(name);
		register(ql);
		return ql;
	}

	/**
	 * Registers the specified query language.
	 */
	public static void register(QueryLanguage ql) {
		QUERY_LANGUAGES.add(ql);
	}

	/**
	 * Returns the query language whose name matches the specified name.
	 * 
	 * @param qlName
	 *        A query language name.
	 * @return The query language whose name matches the specified name, or
	 *         <tt>null</tt> if there is no such query language.
	 */
	public static QueryLanguage valueOf(String qlName) {
		for (QueryLanguage ql : QUERY_LANGUAGES) {
			if (ql.getName().equalsIgnoreCase(qlName)) {
				return ql;
			}
		}

		return null;
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The query language's name.
	 */
	private String _name;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new QueryLanguage object.
	 * 
	 * @param name
	 *        The name of the query language, e.g. "SPARQL".
	 */
	public QueryLanguage(String name) {
		assert name != null : "name must not be null";

		_name = name;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the name of this query language.
	 * 
	 * @return A human-readable format name, e.g. "SPARQL".
	 */
	public String getName() {
		return _name;
	}

	public boolean equals(Object other) {
		if (other instanceof QueryLanguage) {
			QueryLanguage o = (QueryLanguage)other;
			return getName().equals(o.getName());
		}

		return false;
	}

	public int hashCode() {
		return getName().hashCode();
	}

	public String toString() {
		return getName();
	}
}
