/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail.memory;

import org.openrdf.repository.Repository;
import org.openrdf.repository.SparqlDatasetTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class MemorySparqlDatasetTest extends SparqlDatasetTest {

	protected Repository newRepository() {
		return new SailRepository(new MemoryStore());
	}
}
