/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * A comparison between two values.
 */
public class Compare extends BinaryValueOperator {

	/*---------------*
	 * enum Operator *
	 *---------------*/

	public enum CompareOp {
		/** equal to */
		EQ("="),

		/** not equal to */
		NE("!="),

		/** lower than */
		LT("<"),

		/** lower than or equal to */
		LE("<="),

		/** greater than or equal to */
		GE(">="),

		/** greater than */
		GT(">");

		private String _symbol;

		CompareOp(String symbol) {
			_symbol = symbol;
		}

		public String getSymbol() {
			return _symbol;
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private CompareOp _operator;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Compare() {
	}

	public Compare(ValueExpr leftArg, ValueExpr rightArg) {
		this(leftArg, rightArg, CompareOp.EQ);
	}

	public Compare(ValueExpr leftArg, ValueExpr rightArg, CompareOp operator) {
		super(leftArg, rightArg);
		setOperator(operator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public CompareOp getOperator() {
		return _operator;
	}

	public void setOperator(CompareOp operator) {
		assert operator != null : "operator must not be null";
		_operator = operator;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public String toString() {
		return "COMPARE (" + _operator.getSymbol() + ")";
	}

	public ValueExpr cloneValueExpr() {
		ValueExpr leftArg = getLeftArg().cloneValueExpr();
		ValueExpr rightArg = getRightArg().cloneValueExpr();
		return new Compare(leftArg, rightArg, getOperator());
	}
}
