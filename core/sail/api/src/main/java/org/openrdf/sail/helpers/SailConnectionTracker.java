/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.sail.SailConnection;
import org.openrdf.store.StoreException;

/**
 * Takes care of closing of active connections and a grace period for active
 * connections during shutdown of the store.
 * 
 * @author Herko ter Horst
 * @author jeen
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class SailConnectionTracker {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Default connection timeout on shutdown: 20,000 milliseconds.
	 */
	private final static long DEFAULT_CONNECTION_TIMEOUT = 20000L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Connection timeout on shutdown (in ms). Defaults to
	 * {@link #DEFAULT_CONNECTION_TIMEOUT}.
	 */
	private long connectionTimeOut = DEFAULT_CONNECTION_TIMEOUT;

	/**
	 * Map used to track active connections and where these were acquired. The
	 * Throwable value may be null in case debugging was disable at the time the
	 * connection was acquired.
	 */
	private Map<SailConnection, Throwable> activeConnections = new IdentityHashMap<SailConnection, Throwable>();

	/*---------*
	 * Methods *
	 *---------*/

	public long getTimeOut() {
		return connectionTimeOut;
	}

	public void setTimeOut(long connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
	}

	/**
	 * Tracks a connection to force close it later if necessary.
	 */
	public SailConnection track(SailConnection connection)
		throws StoreException
	{
		connection = wrapConnection(connection);

		Throwable stackTrace = SailUtil.isDebugEnabled() ? new Throwable() : null;
		synchronized (activeConnections) {
			activeConnections.put(connection, stackTrace);
		}

		return connection;
	}

	/**
	 * Wraps a connection with a connection that deregisters itself upon being
	 * closed.
	 */
	protected SailConnection wrapConnection(SailConnection connection) {
		return new TrackingSailConnection(connection, this);
	}

	/**
	 * Force close any outstanding connections.
	 * 
	 * @throws StoreException
	 */
	public void closeAll()
		throws StoreException
	{
		synchronized (activeConnections) {
			// check if any active connections exist, if so, wait for a grace
			// period for them to finish.
			if (!activeConnections.isEmpty()) {
				logger.info("Waiting for active connections to close before shutting down...");
				try {
					activeConnections.wait(DEFAULT_CONNECTION_TIMEOUT);
				}
				catch (InterruptedException e) {
					// ignore and continue
				}
			}

			// Forcefully close any connections that are still open
			Iterator<Map.Entry<SailConnection, Throwable>> iter = activeConnections.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<SailConnection, Throwable> entry = iter.next();
				SailConnection con = entry.getKey();
				Throwable stackTrace = entry.getValue();

				iter.remove();

				if (stackTrace == null) {
					logger.warn(
							"Closing active connection due to shut down; consider setting the {} system property",
							SailUtil.DEBUG_PROP);
				}
				else {
					logger.warn("Closing active connection due to shut down, connection was acquired in",
							stackTrace);
				}

				try {
					con.close();
				}
				catch (StoreException e) {
					logger.error("Failed to close connection", e);
				}
			}
		}
	}

	/**
	 * Signals that the supplied connection has already been closed.
	 * 
	 * @param connection
	 */
	public void closed(SailConnection connection) {
		synchronized (activeConnections) {
			if (activeConnections.containsKey(connection)) {
				activeConnections.remove(connection);

				if (activeConnections.isEmpty()) {
					// only notify waiting threads if all active connections have
					// been closed.
					activeConnections.notifyAll();
				}
			}
			else {
				logger.warn("tried to remove unknown connection object from store.");
			}
		}
	}
}
