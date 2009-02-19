/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.store.StoreException;

/**
 * A representation of a query result as a sequence of {@link Statement}
 * objects. Each query result consists of zero or more Statements and
 * additionaly carries information about relevant namespace declarations. Note:
 * take care to always close a GraphQueryResult after use to free any resources
 * it keeps hold of.
 * 
 * @author jeen
 */
@Deprecated
public interface GraphQueryResult extends QueryResult<Statement> {

	/**
	 * Retrieves relevant namespaces from the query result.
	 * 
	 * @return a Map<String, String> object containing (prefix, namespace) pairs.
	 * @throws StoreException
	 */
	public Map<String, String> getNamespaces()
		throws StoreException;
}
