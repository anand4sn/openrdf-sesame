/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.openrdf.query.EvaluationException;

public class BooleanQueryResultSerializationTest extends TestCase {

	public void testTextResultFormat()
		throws IOException, QueryResultParseException, UnsupportedQueryResultFormatException,
		EvaluationException
	{
		testQueryResultFormat(BooleanQueryResultFormat.TEXT, true);
		testQueryResultFormat(BooleanQueryResultFormat.TEXT, false);
	}

	private void testQueryResultFormat(BooleanQueryResultFormat format, boolean input)
		throws IOException, QueryResultParseException, UnsupportedQueryResultFormatException,
		EvaluationException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
		QueryResultIO.write(input, format, out);
		out.flush();
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		boolean output = QueryResultIO.parse(in, format);

		assertEquals(output, input);
	}
}
