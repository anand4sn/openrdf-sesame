/* Generated By:JJTree: Do not edit this line. ASTStr.java */

package org.openrdf.query.parser.sparql.ast;

public class ASTStr extends SimpleNode {

	public ASTStr(int id) {
		super(id);
	}

	public ASTStr(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}
}
