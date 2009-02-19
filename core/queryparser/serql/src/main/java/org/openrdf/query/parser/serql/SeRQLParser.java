/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.GraphQueryModel;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.query.parser.serql.ast.ASTGraphQuery;
import org.openrdf.query.parser.serql.ast.ASTQuery;
import org.openrdf.query.parser.serql.ast.ASTQueryContainer;
import org.openrdf.query.parser.serql.ast.ASTTupleQuery;
import org.openrdf.query.parser.serql.ast.ParseException;
import org.openrdf.query.parser.serql.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.serql.ast.TokenMgrError;
import org.openrdf.query.parser.serql.ast.VisitorException;

public class SeRQLParser implements QueryParser {

	public QueryModel parseQuery(String queryStr, String baseURI)
		throws MalformedQueryException
	{
		try {
			ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(queryStr);

			// Replace deprecated NULL nodes with semantically equivalent
			// alternatives
			NullProcessor.process(qc);

			StringEscapesProcessor.process(qc);
			Map<String, String> namespaces = NamespaceDeclProcessor.process(qc);
			ProjectionProcessor.process(qc);
			qc.jjtAccept(new ProjectionAliasProcessor(), null);
			qc.jjtAccept(new AnonymousVarGenerator(), null);

			// TODO: check use of unbound variables?

			TupleExpr tupleExpr = QueryModelBuilder.buildQueryModel(qc, new ValueFactoryImpl());

			ASTQuery queryNode = qc.getQuery();
			QueryModel query;
			if (queryNode instanceof ASTTupleQuery) {
				query = new TupleQueryModel(tupleExpr);
			}
			else if (queryNode instanceof ASTGraphQuery) {
				query = new GraphQueryModel(tupleExpr, namespaces);
			}
			else {
				throw new RuntimeException("Unexpected query type: " + queryNode.getClass());
			}

			return query;
		}
		catch (ParseException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
		catch (TokenMgrError e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
		catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
	}

	public static void main(String[] args)
		throws java.io.IOException
	{
		System.out.println("Your SeRQL query:");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		StringBuilder buf = new StringBuilder();
		String line = null;
		while ((line = in.readLine()) != null) {
			if (line.length() > 0) {
				buf.append(' ').append(line).append('\n');
			}
			else {
				String queryStr = buf.toString().trim();
				if (queryStr.length() > 0) {
					try {
						SeRQLParser parser = new SeRQLParser();
						parser.parseQuery(queryStr, null);
					}
					catch (Exception e) {
						System.err.println(e.getMessage());
						e.printStackTrace();
					}
				}
				buf.setLength(0);
			}
		}
	}
}
