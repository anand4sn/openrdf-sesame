/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.trix;

/**
 * Interface defining a number of constants for the TriX document format.
 */
public interface TriXConstants {

	/** The TriX namespace. */
	public static final String NAMESPACE = "http://www.w3.org/2004/03/trix/trix-1/";

	/** The root tag. */
	public static final String ROOT_TAG = "TriX";

	/** The tag that starts a new context/graph. */
	public static final String CONTEXT_TAG = "graph";

	/** The tag that starts a new triple. */
	public static final String TRIPLE_TAG = "triple";

	/** The tag for URI values. */
	public static final String URI_TAG = "uri";

	/** The tag for BNode values. */
	public static final String BNODE_TAG = "id";

	/** The tag for plain literal values. */
	public static final String PLAIN_LITERAL_TAG = "plainLiteral";

	/** The tag for typed literal values. */
	public static final String TYPED_LITERAL_TAG = "typedLiteral";

	/** The attribute for language tags of plain literal. */
	public static final String LANGUAGE_ATT = "xml:lang";

	/** The attribute for datatypes of typed literal. */
	public static final String DATATYPE_ATT = "datatype";
}
