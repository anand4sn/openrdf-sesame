/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.result.util.QueryResultUtil;
import org.openrdf.store.StoreException;

public class TupleQueryResultSerializationTest extends TestCase {

	public void testBinaryResultFormat()
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException, StoreException
	{
		testQueryResultFormat(TupleQueryResultFormat.BINARY);
	}

	private void testQueryResultFormat(TupleQueryResultFormat format)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException,
		UnsupportedQueryResultFormatException, StoreException
	{
		TupleResult input = createQueryResult();
		TupleResult expected = createQueryResult();

		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		QueryResultIO.write(input, format, out);
		input.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		TupleResult output = QueryResultIO.parse(in, format);

		assertTrue(QueryResultUtil.equals(expected, output));
	}

	private TupleResult createQueryResult() {
		List<String> bindingNames = Arrays.asList("a", "b", "c");

		MapBindingSet solution1 = new MapBindingSet(bindingNames.size());
		solution1.addBinding("a", new URIImpl("foo:bar"));
		solution1.addBinding("b", new BNodeImpl("bnode"));
		solution1.addBinding("c", new LiteralImpl("baz"));

		MapBindingSet solution2 = new MapBindingSet(bindingNames.size());
		solution2.addBinding("a", new LiteralImpl("1", XMLSchema.INTEGER));
		solution2.addBinding("c", new LiteralImpl("Hello World!", "en"));

		List<? extends BindingSet> bindingSetList = Arrays.asList(solution1, solution2);

		TupleResultImpl result = new TupleResultImpl(bindingNames, bindingSetList);

		return result;
	}
}
