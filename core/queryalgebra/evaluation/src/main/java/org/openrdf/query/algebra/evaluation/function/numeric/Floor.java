/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.numeric;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * The SPARQL built-in {@link Function} FLOOR, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-floor">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class Floor implements Function {

	public String getURI() {
		return FN.NUMERIC_FLOOR.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("FLOOR requires exactly 1 argument, got " + args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];

			URI datatype = literal.getDatatype();

			// function accepts only numeric literals
			if (datatype != null && XMLDatatypeUtil.isNumericDatatype(datatype)) {
				if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
					return literal;
				}
				else if (XMLDatatypeUtil.isDecimalDatatype(datatype)) {
					BigDecimal floor = literal.decimalValue().setScale(0, RoundingMode.FLOOR);
					return valueFactory.createLiteral(floor.toPlainString(), datatype);
				}
				else if (XMLDatatypeUtil.isFloatingPointDatatype(datatype)) {
					double floor = Math.floor(literal.doubleValue());
					return valueFactory.createLiteral(Double.toString(floor), datatype);
				}
				else {
					throw new ValueExprEvaluationException("unexpected datatype for function operand: " + args[0]);
				}
			}
			else {
				throw new ValueExprEvaluationException("unexpected input value for function: " + args[0]);
			}
		}
		else {
			throw new ValueExprEvaluationException("unexpected input value for function: " + args[0]);
		}

	}

}
