/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import java.sql.SQLException;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;

import org.openrdf.OpenRDFUtil;
import org.openrdf.cursor.Cursor;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.SailConnectionBase;
import org.openrdf.sail.rdbms.evaluation.RdbmsEvaluationFactory;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.iteration.RdbmsResourceCursor;
import org.openrdf.sail.rdbms.managers.NamespaceManager;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.rdbms.optimizers.RdbmsQueryOptimizer;
import org.openrdf.store.Isolation;
import org.openrdf.store.StoreException;

/**
 * Coordinates the triple store, namespace manager, optimizer, and evaluation
 * strategy into the {@link SailConnection} interface.
 * 
 * @author James Leigh
 */
public class RdbmsConnection extends SailConnectionBase {

	private RdbmsStore sail;

	private RdbmsValueFactory vf;

	private RdbmsTripleRepository triples;

	private NamespaceManager namespaces;

	private RdbmsQueryOptimizer optimizer;

	private RdbmsEvaluationFactory factory;

	private ExclusiveLockManager lockManager;

	private Lock lock;

	public RdbmsConnection(RdbmsStore sail, RdbmsTripleRepository triples) {
		this.sail = sail;
		this.vf = sail.getValueFactory();
		this.triples = triples;

		// Set default isolation level (serializable since we don't allow
		// concurrent transactions yet)
		try {
			setTransactionIsolation(Isolation.SERIALIZABLE);
		}
		catch (StoreException e) {
			throw new RuntimeException("unexpected exception", e);
		}
	}

	public RdbmsValueFactory getValueFactory() {
		return vf;
	}

	public void setNamespaces(NamespaceManager namespaces) {
		this.namespaces = namespaces;
	}

	public void setRdbmsQueryOptimizer(RdbmsQueryOptimizer optimizer) {
		this.optimizer = optimizer;
	}

	public void setRdbmsEvaluationFactory(RdbmsEvaluationFactory factory) {
		this.factory = factory;
	}

	public void setLockManager(ExclusiveLockManager lock) {
		this.lockManager = lock;
	}

	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		try {
			if (contexts != null && contexts.length == 0) {
				triples.add(vf.createStatement(subj, pred, obj));
			}
			else {
				for (Resource ctx : OpenRDFUtil.notNull(contexts)) {
					triples.add(vf.createStatement(subj, pred, obj, ctx));
				}
			}
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		catch (InterruptedException e) {
			throw new RdbmsException(e);
		}
	}

	@Override
	public void close()
		throws StoreException
	{
		try {
			triples.close();
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		finally {
			super.close();
			unlock();
		}
	}

	public void commit()
		throws StoreException
	{
		try {
			triples.commit();
			unlock();
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		catch (InterruptedException e) {
			throw new RdbmsException(e);
		}

		// sail.notifySailChanged(triples.getSailChangedEvent());

		// create a fresh event object.
		triples.setSailChangedEvent(new DefaultSailChangedEvent(sail));
		super.commit();
	}

	public RdbmsResourceCursor getContextIDs()
		throws StoreException
	{
		try {
			return triples.findContexts();
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public Cursor<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		RdbmsResource s = vf.asRdbmsResource(subj);
		RdbmsURI p = vf.asRdbmsURI(pred);
		RdbmsValue o = vf.asRdbmsValue(obj);
		RdbmsResource[] c = vf.asRdbmsResource(contexts);
		return triples.find(s, p, o, c);
	}

	public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		RdbmsResource s = vf.asRdbmsResource(subj);
		RdbmsURI p = vf.asRdbmsURI(pred);
		RdbmsValue o = vf.asRdbmsValue(obj);
		RdbmsResource[] c = vf.asRdbmsResource(contexts);
		triples.remove(s, p, o, c);
	}

	public void rollback()
		throws StoreException
	{
		try {
			triples.rollback();
			super.rollback();
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		finally {
			unlock();
		}
	}

	public Cursor<BindingSet> evaluate(QueryModel query, BindingSet bindings, boolean includeInferred)
		throws StoreException
	{
		triples.flush();
		TupleExpr tupleExpr;
		EvaluationStrategy strategy;
		strategy = factory.createRdbmsEvaluation(query);
		tupleExpr = optimizer.optimize(query, bindings, strategy);
		return strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());
	}

	public void clearNamespaces()
		throws StoreException
	{
		namespaces.clearPrefixes();
	}

	public String getNamespace(String prefix)
		throws StoreException
	{
		Namespace ns = namespaces.findByPrefix(prefix);
		if (ns == null)
			return null;
		return ns.getName();
	}

	public Cursor<? extends Namespace> getNamespaces()
		throws StoreException
	{
		return namespaces.getNamespacesWithPrefix();
	}

	public void removeNamespace(String prefix)
		throws StoreException
	{
		namespaces.removePrefix(prefix);
	}

	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		namespaces.setPrefix(prefix, name);
	}

	public long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		try {
			RdbmsResource s = vf.asRdbmsResource(subj);
			RdbmsURI p = vf.asRdbmsURI(pred);
			RdbmsValue o = vf.asRdbmsValue(obj);
			return triples.size(s, p, o, vf.asRdbmsResource(contexts));
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	@Override
	public void begin()
		throws StoreException
	{
		try {
			lock();
			triples.begin();
			super.begin();
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		catch (InterruptedException e) {
			throw new RdbmsException(e);
		}
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		unlock();
		super.finalize();
	}

	private void lock()
		throws InterruptedException
	{
		if (lockManager != null) {
			lock = lockManager.getExclusiveLock();
		}
	}

	private void unlock() {
		if (lockManager != null && lock != null) {
			lock.release();
			lock = null;
		}
	}

}
