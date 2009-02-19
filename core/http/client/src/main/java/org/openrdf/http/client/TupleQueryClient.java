/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.FutureTupleQueryResult;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.result.TupleResult;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class TupleQueryClient extends QueryClient {

	public TupleQueryClient(HTTPConnectionPool query) {
		super(query);
	}

	public TupleResult get()
		throws StoreException
	{
		Callable<TupleResult> task = new Callable<TupleResult>() {

			public TupleResult call()
				throws Exception
			{
				try {
					HTTPConnection method = createConnection();
					method.acceptTupleQueryResult();
					execute(method);
					return method.getTupleQueryResult();
				}
				catch (NoCompatibleMediaType e) {
					throw new UnsupportedRDFormatException(e);
				}
			}
		};
		return new FutureTupleQueryResult(submitTask(task));
	}

	public void get(TupleQueryResultHandler handler)
		throws TupleQueryResultHandlerException, StoreException
	{
		HTTPConnection method = createConnection();

		try {
			method.acceptRDF(false);
			execute(method);
			method.readTupleQueryResult(handler);
		}
		catch (NoCompatibleMediaType e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		catch (QueryResultParseException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

}
