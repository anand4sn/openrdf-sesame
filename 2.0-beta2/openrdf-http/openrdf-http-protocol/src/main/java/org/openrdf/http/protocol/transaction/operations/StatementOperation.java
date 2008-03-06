/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.transaction.operations;

import info.aduna.lang.ObjectUtil;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * An operation with (optional) subject, predicate, object.
 * 
 * @author Arjohn Kampman
 */
public abstract class StatementOperation implements TransactionOperation {

	private Resource _subject;

	private URI _predicate;

	private Value _object;

	public Resource getSubject() {
		return _subject;
	}

	public void setSubject(Resource subject) {
		_subject = subject;
	}

	public URI getPredicate() {
		return _predicate;
	}

	public void setPredicate(URI predicate) {
		_predicate = predicate;
	}

	public Value getObject() {
		return _object;
	}

	public void setObject(Value object) {
		_object = object;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof StatementOperation) {
			StatementOperation o = (StatementOperation)other;

			return ObjectUtil.nullEquals(getSubject(), o.getSubject())
					&& ObjectUtil.nullEquals(getPredicate(), o.getPredicate())
					&& ObjectUtil.nullEquals(getObject(), o.getObject());
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int hashCode = ObjectUtil.nullHashCode(getSubject());
		hashCode = 31 * hashCode + ObjectUtil.nullHashCode(getPredicate());
		hashCode = 31 * hashCode + ObjectUtil.nullHashCode(getObject());
		return hashCode;
	}
}
