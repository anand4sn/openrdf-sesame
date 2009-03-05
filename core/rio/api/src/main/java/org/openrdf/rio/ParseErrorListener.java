/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

/**
 * An interface defining methods for receiving warning and error messages from
 * an RDF parser.
 */
public interface ParseErrorListener {

	/**
	 * Reports a warning from the parser. Warning messages are generated by the
	 * parser when it encounters data that is syntactically correct but which is
	 * likely to be a typo. Examples are the use of unknown or deprecated RDF
	 * URIs, e.g. <tt>rdfs:Property</tt> instead of <tt>rdf:Property</tt>.
	 * 
	 * @param msg
	 *        A warning message.
	 * @param lineNo
	 *        A line number related to the warning, or -1 if not available or
	 *        applicable.
	 * @param colNo
	 *        A column number related to the warning, or -1 if not available or
	 *        applicable.
	 */
	public void warning(String msg, int lineNo, int colNo);

	/**
	 * Reports an error from the parser. Error messages are generated by the
	 * parser when it encounters an error in the RDF document. The parser will
	 * try its best to recover from the error and continue parsing when
	 * <tt>stopAtFirstError</tt> has been set to <tt>false</tt>.
	 * 
	 * @param msg
	 *        A error message.
	 * @param lineNo
	 *        A line number related to the error, or -1 if not available or
	 *        applicable.
	 * @param colNo
	 *        A column number related to the error, or -1 if not available or
	 *        applicable.
	 * @see org.openrdf.rio.RDFParser#setStopAtFirstError
	 */
	public void error(String msg, int lineNo, int colNo);

	/**
	 * Reports a fatal error from the parser. A fatal error is an error of which
	 * the RDF parser cannot recover. The parser will stop parsing directly after
	 * it reported the fatal error. Example fatal errors are unbalanced start-
	 * and end-tags in an XML-encoded RDF document.
	 * 
	 * @param msg
	 *        A error message.
	 * @param lineNo
	 *        A line number related to the error, or -1 if not available or
	 *        applicable.
	 * @param colNo
	 *        A column number related to the error, or -1 if not available or
	 *        applicable.
	 */
	public void fatalError(String msg, int lineNo, int colNo);
}
