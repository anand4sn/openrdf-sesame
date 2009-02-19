/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;

import info.aduna.io.file.FileUtil;

import org.openrdf.repository.Repository;
import org.openrdf.repository.TupleQueryResultTest;
import org.openrdf.repository.sail.SailRepository;

public class NativeTupleQueryResultTest extends TupleQueryResultTest {

	private File dataDir;

	@Override
	protected Repository newRepository()
		throws IOException
	{
		dataDir = FileUtil.createTempDir("nativestore");
		return new SailRepository(new NativeStore(dataDir, "spoc"));
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		try {
			super.tearDown();
		}
		finally {
			FileUtil.deleteDir(dataDir);
		}
	}
}
