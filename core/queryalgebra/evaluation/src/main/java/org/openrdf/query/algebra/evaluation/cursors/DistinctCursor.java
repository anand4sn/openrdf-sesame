/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.FilteringCursor;


/**
 *
 * @author James Leigh
 */
public class DistinctCursor<E> extends FilteringCursor<E> {
	private Set<E> exclude = new HashSet<E>();

	public DistinctCursor(Cursor<? extends E> delegate) {
		super(delegate);
	}

	@Override
	protected boolean accept(E next) {
		return exclude.add(next);
	}

}
