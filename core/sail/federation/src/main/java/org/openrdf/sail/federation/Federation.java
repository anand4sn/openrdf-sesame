package org.openrdf.sail.federation;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.store.StoreException;

public class Federation extends SailBase {

	private ValueFactory vf = ValueFactoryImpl.getInstance();

	private List<Repository> members = new ArrayList<Repository>();

	private boolean writable;

	public ValueFactory getValueFactory() {
		return vf;
	}

	public void addMember(Repository member) {
		members.add(member);
	}

	public void initialize()
		throws StoreException
	{
		for (Repository member : members) {
			member.initialize();
		}
	}

	@Override
	protected void shutDownInternal()
		throws StoreException
	{
		for (Repository member : members) {
			member.shutDown();
		}
	}

	public boolean isWritable()
		throws StoreException
	{
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	@Override
	protected SailConnection getConnectionInternal()
		throws StoreException
	{
		List<RepositoryConnection> connections = new ArrayList<RepositoryConnection>(members.size());
		try {
			for (Repository member : members) {
				connections.add(member.getConnection());
			}
			if (writable) {
				return new RoundRobinConnection(this, connections);
			}
			else {
				return new ReadOnlyConnection(this, connections);
			}
		}
		catch (StoreException e) {
			closeAll(connections);
			throw e;
		}
		catch (RuntimeException e) {
			closeAll(connections);
			throw e;
		}
	}

	private void closeAll(List<RepositoryConnection> connections) {
		for (RepositoryConnection con : connections) {
			try {
				con.close();
			}
			catch (StoreException e) {
				logger.error(e.toString(), e);
			}
		}
	}

}
