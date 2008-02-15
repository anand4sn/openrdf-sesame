/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import static org.openrdf.sail.rdbms.schema.LiteralTable.getCalendarValue;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.SqlConstant;

/**
 * A static long value in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class LongValue extends SqlConstant<Long> {

	public LongValue(Long value) {
		super(value);
	}

	public LongValue(XMLGregorianCalendar value) {
		this(getCalendarValue(value));
	}

	@Override
	public <X extends Exception> void visit(
			RdbmsQueryModelVisitorBase<X> visitor) throws X {
		visitor.meet(this);
	}
}
