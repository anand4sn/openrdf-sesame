/* Generated By:JJTree: Do not edit this line. ASTDescribe.java */

package org.openrdf.querylanguage.sparql.ast;

public class ASTDescribe extends SimpleNode {

	private boolean _wildcard = false;

	public ASTDescribe(int id) {
		super(id);
	}

	public ASTDescribe(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public boolean isWildcard() {
		return _wildcard;
	}

	public void setWildcard(boolean wildcard) {
		_wildcard = wildcard;
	}

	public String toString() {
		String result = super.toString();

		if (_wildcard) {
			result += " (*)";
		}

		return result;
	}
}
