/* Generated By:JJTree: Do not edit this line. ASTMath.java */

package org.openrdf.query.parser.sparql.ast;

import org.openrdf.query.algebra.MathExpr.MathOp;

public class ASTMath extends SimpleNode {

	private MathOp _operator;

	public ASTMath(int id) {
		super(id);
	}

	public ASTMath(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public MathOp getOperator() {
		return _operator;
	}

	public void setOperator(MathOp operator) {
		_operator = operator;
	}

	public String toString() {
		return super.toString() + " (" + _operator + ")";
	}
}
