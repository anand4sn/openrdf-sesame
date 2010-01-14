/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;

import org.openrdf.http.client.connections.HTTPRequest;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.Unauthorized;
import org.openrdf.http.protocol.exceptions.UnsupportedFileFormat;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.protocol.exceptions.UnsupportedQueryLanguage;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class SizeClient {

	private final HTTPConnectionPool pool;

	private String match;

	private String eTag;

	private int maxAge;

	public SizeClient(HTTPConnectionPool pool) {
		this.pool = pool;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public String getETag() {
		return eTag;
	}

	public void ifNoneMatch(String eTag) {
		match = eTag;
	}

	/*-------------------------*
	 * Repository/context size *
	 *-------------------------*/

	public Long get(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		HTTPRequest request = pool.get();

		try {
			if (match != null) {
				request.ifNoneMatch(match);
			}
			request.acceptLong();
			request.sendQueryString(getParams(subj, pred, obj, includeInferred, contexts));
			execute(request);
			if (request.isNotModified()) {
				return null;
			}
			return request.readLong();
		}
		catch (NumberFormatException e) {
			throw new StoreException("Server responded with invalid size value");
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			request.release();
		}
	}

	private List<NameValuePair> getParams(Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		if (subj != null) {
			params.add(new NameValuePair(Protocol.SUBJECT_PARAM_NAME, Protocol.encodeValue(subj)));
		}
		if (pred != null) {
			params.add(new NameValuePair(Protocol.PREDICATE_PARAM_NAME, Protocol.encodeValue(pred)));
		}
		if (obj != null) {
			params.add(new NameValuePair(Protocol.OBJECT_PARAM_NAME, Protocol.encodeValue(obj)));
		}
		for (String encodedContext : Protocol.encodeContexts(contexts)) {
			params.add(new NameValuePair(Protocol.CONTEXT_PARAM_NAME, encodedContext));
		}
		params.add(new NameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME, Boolean.toString(includeInferred)));
		return params;
	}

	private void execute(HTTPRequest request)
		throws IOException, StoreException
	{
		try {
			reset();
			request.execute();
			eTag = request.readETag();
			maxAge = request.readMaxAge();
		}
		catch (UnsupportedQueryLanguage e) {
			throw new UnsupportedQueryLanguageException(e);
		}
		catch (UnsupportedFileFormat e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (UnsupportedMediaType e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (Unauthorized e) {
			throw new StoreException(e);
		}
		catch (HTTPException e) {
			throw new StoreException(e);
		}
	}

	private void reset() {
		match = null;
	}

}
