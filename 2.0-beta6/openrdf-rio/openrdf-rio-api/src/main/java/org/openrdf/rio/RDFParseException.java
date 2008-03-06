/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import org.openrdf.OpenRDFException;

/**
 * A parse exception that can be thrown by a parser when it encounters an error
 * from which it cannot or doesn't want to recover.
 */
public class RDFParseException extends OpenRDFException {

	private static final long serialVersionUID = -4686126837948873012L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private int _lineNo = -1;

	private int _columnNo = -1;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new ParseException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public RDFParseException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new ParseException.
	 * 
	 * @param msg
	 *        An error message.
	 * @param lineNo
	 *        A line number associated with the message.
	 * @param columnNo
	 *        A column number associated with the message.
	 */
	public RDFParseException(String msg, int lineNo, int columnNo) {
		super(msg + getLocationString(lineNo, columnNo));
		_lineNo = lineNo;
		_columnNo = columnNo;
	}

	/**
	 * Creates a new ParseException wrapping another exception. The
	 * ParseException will inherit its message from the supplied source
	 * exception.
	 * 
	 * @param t
	 *        The source exception.
	 */
	public RDFParseException(Throwable t) {
		super(t);
	}

	/**
	 * Creates a new ParseException wrapping another exception. The
	 * ParseException will inherit its message from the supplied source
	 * exception.
	 * 
	 * @param t
	 *        The source exception.
	 * @param lineNo
	 *        A line number associated with the message.
	 * @param columnNo
	 *        A column number associated with the message.
	 */
	public RDFParseException(Throwable t, int lineNo, int columnNo) {
		super(t.getMessage() + getLocationString(lineNo, columnNo), t);
		_lineNo = lineNo;
		_columnNo = columnNo;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the line number associated with this parse exception.
	 * 
	 * @return A line number, or -1 if no line number is available or applicable.
	 */
	public int getLineNumber() {
		return _lineNo;
	}

	/**
	 * Gets the column number associated with this parse exception.
	 * 
	 * @return A column number, or -1 if no column number is available or
	 *         applicable.
	 */
	public int getColumnNumber() {
		return _columnNo;
	}

	protected static String getLocationString(int lineNo, int columnNo) {
		if (lineNo < 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(16);
		sb.append(" [line ");
		sb.append(lineNo);

		if (columnNo >= 0) {
			sb.append(", column ");
			sb.append(columnNo);
		}

		sb.append("]");
		return sb.toString();
	}
}
