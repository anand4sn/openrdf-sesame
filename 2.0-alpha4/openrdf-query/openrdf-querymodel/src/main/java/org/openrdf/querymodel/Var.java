/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

import org.openrdf.model.Value;

/**
 * A variable that can contain a Value.
 */
public class Var extends ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String _name;

	private Value _value;

	private boolean _anonymous = false;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Var(String name) {
		_name = name;
	}

	public Var(String name, Value value) {
		this(name);
		setValue(value);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setAnonymous(boolean anonymous) {
		_anonymous = anonymous;
	}

	public boolean isAnonymous() {
		return _anonymous;
	}

	public String getName() {
		return _name;
	}

	public void setValue(Value value) {
		_value = value;
	}

	public boolean hasValue() {
		return _value != null;
	}

	public Value getValue() {
		return _value;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	public boolean equals(Object other) {
		if (other instanceof Var) {
			return _name.equals(((Var)other)._name);
		}

		return false;
	}

	public int hashCode() {
		return _name.hashCode();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append("VAR (name=").append(_name);

		if (_value != null) {
			sb.append(", value=").append(_value.toString());
		}
		
		sb.append(")");

		return sb.toString();
	}
}
