/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.lang.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry that stores services by some key. Upon initialization, the
 * registry searches for service description files at
 * <tt>META-INF/services/&lt;service class name&gt;</tt> and initializes
 * itself accordingly.
 * 
 * @see javax.imageio.spi.ServiceRegistry
 * @author Arjohn Kampman
 */
public abstract class ServiceRegistry<K, S> {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected Map<K, S> services = new HashMap<K, S>();

	protected ServiceRegistry(Class<S> serviceClass) {
		// Note: Using javax.imageio.spi.ServiceRegistry as it publicly exposes
		// the sun.misc.Service functionality. Starting from Java 6, this
		// functionality is also available java.util.ServiceLoader
		Iterator<S> services = javax.imageio.spi.ServiceRegistry.lookupProviders(serviceClass, serviceClass.getClassLoader());

		while (true) {
			try {
				if (services.hasNext()) {
					S service = services.next();

					S oldService = add(service);

					if (oldService != null) {
						logger.warn("New service {} replaces existing service {}", service.getClass(),
								oldService.getClass());
					}

					logger.debug("Registered service class {}", service.getClass().getName());
				}
				else {
					break;
				}
			}
			catch (Error e) {
				logger.error("Failed to instantiate service", e);
			}
		}
	}

	/**
	 * Adds a service to the registry. Any service that is currently registered
	 * for the same key (as specified by {@link #getKey(Object)}) will be
	 * replaced with the new service.
	 * 
	 * @param service
	 *        The service that should be added to the registry.
	 * @return The previous service that was registered for the same key, or
	 *         <tt>null</tt> if there was no such service.
	 */
	public S add(S service) {
		return services.put(getKey(service), service);
	}

	/**
	 * Removes a service from the registry.
	 * 
	 * @param service
	 *        The service be removed from the registry.
	 */
	public void remove(S service) {
		services.remove(getKey(service));
	}

	/**
	 * Gets the service for the specified key, if any.
	 * 
	 * @param key
	 *        The key identifying which service to get.
	 * @return The service for the specified key, or <tt>null</tt> if no such
	 *         service is avaiable.
	 */
	public S get(K key) {
		return services.get(key);
	}

	/**
	 * Checks whether a service for the specified key is available.
	 * 
	 * @param key
	 *        The key identifying which service to search for.
	 * @return <tt>true</tt> if a service for the specific key is available,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean has(K key) {
		return services.containsKey(key);
	}

	/**
	 * Gets all registered services.
	 * 
	 * @return An unmodifiable collection containing all registered servivces.
	 */
	public Collection<S> getAll() {
		return Collections.unmodifiableCollection(services.values());
	}

	/**
	 * Gets the set of registered keys.
	 * 
	 * @return An unmodifiable set containing all registered keys.
	 */
	public Set<K> getKeys() {
		return Collections.unmodifiableSet(services.keySet());
	}

	/**
	 * Gets the key for the specified service.
	 * 
	 * @param service
	 *        The service to get the key for.
	 * @return The key for the specified service.
	 */
	protected abstract K getKey(S service);
}
