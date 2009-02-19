/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.store;

import org.openrdf.query.QueryEvaluationException;

/**
 * An exception thrown by Sesame stores to indicate an error. Subclasses of this
 * exception are used to more precisely indicate the cause of the error.
 */
public class StoreException extends QueryEvaluationException {

	private static final long serialVersionUID = -3216054915937011603L;

	public StoreException() {
		super();
	}

	public StoreException(String msg) {
		super(msg);
	}

	public StoreException(Throwable t) {
		super(t);
	}

	public StoreException(String msg, Throwable t) {
		super(msg, t);
	}
}
