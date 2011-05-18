/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.lang.ObjectUtil;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.AggregateOperator;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupConcat;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.Sample;
import org.openrdf.query.algebra.Sum;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.MathUtil;
import org.openrdf.query.algebra.evaluation.util.ValueComparator;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * @author David Huynh
 * @author Arjohn Kampman
 * @author Jeen Broekstra
 */
public class GroupIterator extends CloseableIteratorIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final EvaluationStrategy strategy;

	private final BindingSet parentBindings;

	private final Group group;

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean ordered = false;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public GroupIterator(EvaluationStrategy strategy, Group group, BindingSet parentBindings)
		throws QueryEvaluationException
	{
		this.strategy = strategy;
		this.group = group;
		this.ordered = (group.getArg() instanceof Order);
		this.parentBindings = parentBindings;
		super.setIterator(createIterator());
	}

	/*---------*
	 * Methods *
	 *---------*/

	private Iterator<BindingSet> createIterator()
		throws QueryEvaluationException
	{
		Collection<BindingSet> bindingSets;
		Collection<Entry> entries;

		if (ordered) {
			bindingSets = new ArrayList<BindingSet>();
			entries = buildOrderedEntries();
		}
		else {
			bindingSets = new HashSet<BindingSet>();
			entries = buildUnorderedEntries();
		}

		for (Entry entry : entries) {
			QueryBindingSet sol = new QueryBindingSet(parentBindings);

			for (String name : group.getGroupBindingNames()) {
				BindingSet prototype = entry.getPrototype();
				if (prototype != null) {
					Value value = prototype.getValue(name);
					if (value != null) {
						// Potentially overwrites bindings from super
						sol.setBinding(name, value);
					}
				}
			}

			for (GroupElem ge : group.getGroupElements()) {
				Value value = processAggregate(entry.getSolutions(), ge.getOperator());
				if (value != null) {
					// Potentially overwrites bindings from super
					sol.setBinding(ge.getName(), value);
				}
			}

			bindingSets.add(sol);
		}

		return bindingSets.iterator();
	}

	private Collection<Entry> buildOrderedEntries()
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> iter = strategy.evaluate(group.getArg(),
				parentBindings);

		try {
			List<Entry> orderedEntries = new ArrayList<Entry>();
			Map<Key, Entry> entries = new HashMap<Key, Entry>();

			if (!iter.hasNext()) {
				// no solutions, still need to process any aggregates to produce a
				// zero-result.
				orderedEntries.add(new Entry(null));
			}

			while (iter.hasNext()) {
				BindingSet bindingSet = iter.next();
				Key key = new Key(bindingSet);
				Entry entry = entries.get(key);

				if (entry == null) {
					entry = new Entry(bindingSet);
					entries.put(key, entry);
					orderedEntries.add(entry);
				}

				entry.addSolution(bindingSet);
			}

			return orderedEntries;
		}
		finally {
			iter.close();
		}
	}

	private Collection<Entry> buildUnorderedEntries()
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> iter = strategy.evaluate(group.getArg(),
				parentBindings);

		try {
			Map<Key, Entry> entries = new HashMap<Key, Entry>();

			if (!iter.hasNext()) {
				// no solutions, still need to process aggregates to produce a
				// zero-result.
				entries.put(new Key(new EmptyBindingSet()), new Entry(null));
			}

			while (iter.hasNext()) {
				BindingSet sol = iter.next();
				Key key = new Key(sol);
				Entry entry = entries.get(key);

				if (entry == null) {
					entry = new Entry(sol);
					entries.put(key, entry);
				}

				entry.addSolution(sol);
			}

			return entries.values();
		}
		finally {
			iter.close();
		}

	}

	private Value processAggregate(Collection<BindingSet> bindingSets, AggregateOperator operator)
		throws QueryEvaluationException
	{

		boolean distinct = operator.isDistinct();
		if (operator instanceof Count) {
			Count countOp = (Count)operator;

			ValueExpr arg = countOp.getArg();

			if (arg != null) {
				Collection<Value> values = createValueCollection(arg, bindingSets, distinct);
				return new LiteralImpl(Integer.toString(values.size()), XMLSchema.INTEGER);
			}
			else {
				return new LiteralImpl(Integer.toString(bindingSets.size()), XMLSchema.INTEGER);
			}
		}
		else if (operator instanceof Min) {
			Min minOp = (Min)operator;

			Collection<Value> values = createValueCollection(minOp.getArg(), bindingSets, distinct);

			Value result = null;

			ValueComparator comparator = new ValueComparator();

			for (Value v : values) {
				if (result == null) {
					result = v;
				}
				else if (comparator.compare(v, result) < 0) {
					result = v;
				}
			}
			return result;
		}

		else if (operator instanceof Max) {
			Max maxOp = (Max)operator;

			Collection<Value> values = createValueCollection(maxOp.getArg(), bindingSets, distinct);

			Value result = null;

			ValueComparator comparator = new ValueComparator();

			for (Value v : values) {
				if (result == null) {
					result = v;
				}
				else if (comparator.compare(v, result) > 0) {
					result = v;
				}
			}
			return result;
		}
		else if (operator instanceof Sum) {

			Sum sumOp = (Sum)operator;

			Collection<Value> values = createValueCollection(sumOp.getArg(), bindingSets, distinct);

			return calculateSum(values);

		}
		else if (operator instanceof Avg) {

			Avg avgOp = (Avg)operator;

			Collection<Value> values = createValueCollection(avgOp.getArg(), bindingSets, distinct);

			int size = values.size();
			if (size == 0) {
				return new LiteralImpl("0.0", XMLSchema.DOUBLE);
			}

			Literal sizeLit = new ValueFactoryImpl().createLiteral(size);
			Literal sum = calculateSum(values);
			Literal avg = MathUtil.compute(sum, sizeLit, MathOp.DIVIDE);

			return avg;
		}
		else if (operator instanceof Sample) {

			Sample sampleOp = (Sample)operator;

			// just get a single value and return it.
			Value value = strategy.evaluate(sampleOp.getArg(), bindingSets.iterator().next());

			return value;
		}
		else if (operator instanceof GroupConcat) {
			GroupConcat groupConcatOp = (GroupConcat)operator;
			Collection<Value> values = createValueCollection(groupConcatOp.getArg(), bindingSets, distinct);

			String separator = " ";
			ValueExpr separatorExpr = groupConcatOp.getSeparator();

			if (separatorExpr != null) {
				Value separatorValue = strategy.evaluate(separatorExpr, parentBindings);
				separator = separatorValue.stringValue();
			}

			StringBuilder concatenated = new StringBuilder();
			for (Value v : values) {
				concatenated.append(v.stringValue());
				concatenated.append(separator);
			}

			if (values.size() > 0) {
				// remove separator at the end.
				concatenated.delete(concatenated.lastIndexOf(separator), concatenated.length());
			}

			return new LiteralImpl(concatenated.toString(), XMLSchema.STRING);
		}

		return null;
	}

	private Literal calculateSum(Collection<Value> values)
		throws ValueExprEvaluationException
	{
		List<Literal> literals = new ArrayList<Literal>();
		for (Value v : values) {
			if (v instanceof Literal) {
				literals.add((Literal)v);
			}
			else {
				throw new ValueExprEvaluationException("not a number: " + v);
			}
		}
		return calculateSum(literals);
	}

	private Literal calculateSum(List<Literal> literals)
		throws ValueExprEvaluationException
	{
		Literal result = literals.get(0);

		for (int i = 1; i < literals.size(); i++) {
			result = MathUtil.compute(result, literals.get(i), MathOp.PLUS);
		}

		return result;
	}

	private Collection<Value> createValueCollection(ValueExpr arg, Collection<BindingSet> bindingSets,
			boolean distinctValues)
		throws QueryEvaluationException
	{
		Collection<Value> values = null;

		if (distinctValues) {
			// TODO handle ordered in combination with distinct
			values = new HashSet<Value>();
		}
		else {
			values = new ArrayList<Value>();
		}

		for (BindingSet s : bindingSets) {
			Value value = strategy.evaluate(arg, s);
			if (value != null) {
				values.add(value);
			}
		}

		return values;
	}

	/**
	 * A unique key for a set of existing bindings.
	 * 
	 * @author David Huynh
	 */
	protected class Key {

		private BindingSet bindingSet;

		private int hash;

		public Key(BindingSet bindingSet) {
			this.bindingSet = bindingSet;

			for (String name : group.getGroupBindingNames()) {
				Value value = bindingSet.getValue(name);
				if (value != null) {
					this.hash ^= value.hashCode();
				}
			}
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Key && other.hashCode() == hash) {
				BindingSet otherSolution = ((Key)other).bindingSet;

				for (String name : group.getGroupBindingNames()) {
					Value v1 = bindingSet.getValue(name);
					Value v2 = otherSolution.getValue(name);

					if (!ObjectUtil.nullEquals(v1, v2)) {
						return false;
					}
				}

				return true;
			}

			return false;
		}
	}

	private class Entry {

		private BindingSet prototype;

		private Collection<BindingSet> bindingSets;

		public Entry(BindingSet prototype) {
			this.prototype = prototype;
			if (ordered) {
				this.bindingSets = new ArrayList<BindingSet>();
			}
			else {
				this.bindingSets = new HashSet<BindingSet>();
			}

		}

		public BindingSet getPrototype() {
			return prototype;
		}

		public void addSolution(BindingSet bindingSet) {
			bindingSets.add(bindingSet);
		}

		public Collection<BindingSet> getSolutions() {
			return bindingSets;
		}
	}
}
