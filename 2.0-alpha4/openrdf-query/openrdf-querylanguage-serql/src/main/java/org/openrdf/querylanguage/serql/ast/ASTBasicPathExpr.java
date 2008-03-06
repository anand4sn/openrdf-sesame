/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

public class ASTBasicPathExpr extends ASTPathExpr {

	public ASTBasicPathExpr(int id) {
		super(id);
	}

	public ASTBasicPathExpr(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTNode getHead() {
		return (ASTNode)children.get(0);
	}
	
	public ASTBasicPathExprTail getTail() {
		return (ASTBasicPathExprTail)children.get(1);
	}
}
