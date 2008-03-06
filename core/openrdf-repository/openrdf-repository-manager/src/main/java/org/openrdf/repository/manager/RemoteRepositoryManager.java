/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.protocol.NotAllowedException;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.http.HTTPRepository;

/**
 * A manager for {@link Repository}s that reside on a remote server. This
 * repository manager allows one to access repositories over HTTP similar to how
 * local repositories are accessed using the {@link LocalRepositoryManager}.
 * 
 * @author Arjohn Kampman
 */
public class RemoteRepositoryManager extends RepositoryManager {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The URL of the remote server, e.g. http://localhost:8080/openrdf-sesame/
	 */
	private String serverURL;

	private String username;

	private String password;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RepositoryManager that operates on the specfified base
	 * directory.
	 * 
	 * @param baseDir
	 *        The base directory where data for repositories can be stored, among
	 *        other things.
	 */
	public RemoteRepositoryManager(String serverURL) {
		super();
		this.serverURL = serverURL;
	}

	/*---------*
	 * Methods *
	 *---------*/
	/**
	 * Set the username and password for authenication with the remote server.
	 * 
	 * @param username
	 *        the username
	 * @param password
	 *        the password
	 */
	public void setUsernameAndPassword(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	protected Repository createSystemRepository()
		throws RepositoryException
	{
		HTTPRepository systemRepository = new HTTPRepository(serverURL, SystemRepository.ID);
		systemRepository.initialize();
		return systemRepository;
	}

	/**
	 * Gets the URL of the remote server, e.g.
	 * "http://localhost:8080/openrdf-sesame/".
	 */
	public String getServerURL() {
		return serverURL;
	}

	@Override
	public HTTPRepository getSystemRepository() {
		HTTPRepository result = (HTTPRepository)super.getSystemRepository();
		result.setUsernameAndPassword(username, password);
		return result;
	}

	/**
	 * Creates and initializes the repository with the specified ID.
	 * 
	 * @param id
	 *        A repository ID.
	 * @return The created repository, or <tt>null</tt> if no such repository
	 *         exists.
	 * @throws RepositoryConfigException
	 *         If no repository could be created due to invalid or incomplete
	 *         configuration data.
	 */
	@Override
	protected Repository createRepository(String id)
		throws RepositoryConfigException, RepositoryException
	{
		Repository result = null;

		if (RepositoryConfigUtil.hasRepositoryConfig(getSystemRepository(), id)) {
			result = new HTTPRepository(serverURL, id);
			result.initialize();
		}

		return result;
	}

	@Override
	protected Map<String, RepositoryInfo> createRepositoryInfos()
		throws RepositoryException
	{
		Map<String, RepositoryInfo> result = new TreeMap<String, RepositoryInfo>();

		try {
			HTTPClient httpClient = new HTTPClient();
			httpClient.setServerURL(serverURL);
			httpClient.setUsernameAndPassword(username, password);

			TupleQueryResult responseFromServer = httpClient.getRepositoryList();
			while (responseFromServer.hasNext()) {
				BindingSet bindingSet = responseFromServer.next();
				RepositoryInfo repInfo = new RepositoryInfo();

				URI uri = (URI)bindingSet.getValue("uri");
				String id = ((Literal)bindingSet.getValue("id")).getLabel();
				String type = ((Literal)bindingSet.getValue("type")).getLabel();

				Value title = bindingSet.getValue("title");
				String description = null;
				if (title instanceof Literal) {
					description = ((Literal)title).getLabel();
				}
				boolean readable = ((Literal)bindingSet.getValue("readable")).booleanValue();
				boolean writable = ((Literal)bindingSet.getValue("writable")).booleanValue();

				repInfo.setLocation(new URL(uri.toString()));
				repInfo.setId(id);
				repInfo.setType(type);
				repInfo.setDescription(description);
				repInfo.setReadable(readable);
				repInfo.setWritable(writable);

				result.put(repInfo.getId(), repInfo);
			}
		}
		catch (IOException ioe) {
			logger.warn("Unable to retrieve list of repositories", ioe);
			throw new RepositoryException(ioe);
		}
		catch (QueryEvaluationException qee) {
			logger.warn("Unable to retrieve list of repositories", qee);
			throw new RepositoryException(qee);
		}
		catch (UnauthorizedException ue) {
			logger.warn("Not authorized to retrieve list of repositories", ue);
			throw new RepositoryException(ue);
		}
		catch (NotAllowedException nae) {
			logger.warn("Not authorized to retrieve list of repositories", nae);
			throw new RepositoryException(nae);
		}
		catch (RepositoryException re) {
			logger.warn("Unable to retrieve list of repositories", re);
			throw re;
		}

		return result;
	}
}
