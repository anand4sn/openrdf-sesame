/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql;

import java.util.Map;

import org.openrdf.querylanguage.serql.ast.ASTQName;
import org.openrdf.querylanguage.serql.ast.ASTURI;
import org.openrdf.querylanguage.serql.ast.Node;
import org.openrdf.querylanguage.serql.ast.SyntaxTreeBuilderTreeConstants;
import org.openrdf.querylanguage.serql.ast.VisitorException;


/**
 * Processes any ASTQname objects in a SeRQL query model, replacing them with
 * equivalent ASTURI objects.
 */
class QNameResolver extends ASTVisitorBase {

	private Map<String, String> _namespaces;

	public QNameResolver(Map<String, String> namespaces) {
		assert namespaces != null : "namespace must not be null";
		_namespaces = namespaces;
	}

	public Object visit(ASTQName node, Object data)
		throws VisitorException
	{
		String qname = node.getValue();

		int colonIdx = qname.indexOf(':');
		assert colonIdx >= 0 : "colonIdx should >= 0: " + colonIdx;

		String prefix = qname.substring(0, colonIdx);
		String localName = qname.substring(colonIdx + 1);

		String namespace = _namespaces.get(prefix);
		if (namespace == null) {
			throw new VisitorException("QName '" + qname + "' uses an undefined namespace prefix");
		}

		// Replace the qname node with a new URI node in the parent node
		ASTURI uriNode = new ASTURI(SyntaxTreeBuilderTreeConstants.JJTURI);
		uriNode.setValue(namespace + localName);

		Node parentNode = node.jjtGetParent();
		uriNode.jjtSetParent(parentNode);
		parentNode.jjtReplaceChild(node, uriNode);

		return data;
	}
}
