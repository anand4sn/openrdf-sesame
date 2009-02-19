/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer.helpers;

import info.aduna.concurrent.locks.Lock;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.helpers.SynchronizedSailConnection;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.store.StoreException;

/**
 * Wrapper class offering synchronization functionality for InferencerConnection
 * implementations. See {@link SynchronizedSailConnection} for more details.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * @author James Leigh
 */
public class SynchronizedInferencerConnection extends SynchronizedSailConnection implements
		InferencerConnection
{

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SynchronizedInferencerConnection(InferencerConnection wrappedCon) {
		super(wrappedCon);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected InferencerConnection getDelegate() {
		return (InferencerConnection)super.getDelegate();
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			Lock txnLock = getTransactionLock();
			try {
				return getDelegate().addInferredStatement(subj, pred, obj, contexts);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public boolean removeInferredStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			Lock txnLock = getTransactionLock();
			try {
				return getDelegate().removeInferredStatements(subj, pred, obj, contexts);
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public void flushUpdates()
		throws StoreException
	{
		Lock conLock = getSharedConnectionLock();
		try {
			Lock txnLock = getTransactionLock();
			try {
				getDelegate().flushUpdates();
			}
			finally {
				txnLock.release();
			}
		}
		finally {
			conLock.release();
		}
	}

	public void addConnectionListener(SailConnectionListener listener) {
		getDelegate().addConnectionListener(listener);
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		getDelegate().removeConnectionListener(listener);
	}
}
