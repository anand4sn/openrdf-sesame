/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.result.MultipleResultException;
import org.openrdf.result.NoResultException;
import org.openrdf.result.Result;
import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;

/**
 * An implementation of the {@link TupleResult} interface that stores the
 * complete query result in memory. The query results in a
 * MutableTupleQueryResult can be iterated over multiple times and can also be
 * iterated over in reverse order.
 * 
 * @author Arjohn Kampman
 */
public class MutableTupleResult implements TupleResult, Cloneable {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Set<String> bindingNames = new LinkedHashSet<String>();

	private List<BindingSet> bindingSets = new LinkedList<BindingSet>();

	/**
	 * The index of the next element that will be returned by a call to
	 * {@link #next()}.
	 */
	private int currentIndex = 0;

	/**
	 * The index of the last element that was returned by a call to
	 * {@link #next()} or {@link #previous()}. Equal to -1 if there is no such
	 * element.
	 */
	private int lastReturned = -1;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public <E extends Exception> MutableTupleResult(Collection<String> bindingNames, BindingSet... bindingSets)
	{
		this(bindingNames, Arrays.asList(bindingSets));
	}

	/**
	 * Creates a query result table with the supplied binding names.
	 * <em>The supplied list of binding names is assumed to be constant</em>;
	 * care should be taken that the contents of this list doesn't change after
	 * supplying it to this solution.
	 * 
	 * @param bindingNames
	 *        The binding names, in order of projection.
	 */
	public MutableTupleResult(Collection<String> bindingNames, Collection<? extends BindingSet> bindingSets) {
		this.bindingNames.addAll(bindingNames);
		this.bindingSets.addAll(bindingSets);
	}

	public MutableTupleResult(Collection<String> bindingNames, Result<? extends BindingSet> bindingSetIter)
		throws StoreException
	{
		this.bindingNames.addAll(bindingNames);
		bindingSetIter.addTo(this.bindingSets);
	}

	public MutableTupleResult(TupleResult tqr)
		throws StoreException
	{
		this(tqr.getBindingNames(), tqr);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public List<String> getBindingNames() {
		return new ArrayList<String>(bindingNames);
	}

	public int size() {
		return bindingSets.size();
	}

	public BindingSet get(int index) {
		return bindingSets.get(index);
	}

	public int getIndex() {
		return currentIndex;
	}

	public void setIndex(int index) {
		if (index < 0 || index > bindingSets.size() + 1) {
			throw new IllegalArgumentException("Index out of range: " + index);
		}

		this.currentIndex = index;
	}

	public boolean hasNext() {
		return currentIndex < bindingSets.size();
	}

	public BindingSet next() {
		if (hasNext()) {
			BindingSet result = get(currentIndex);
			lastReturned = currentIndex;
			currentIndex++;
			return result;
		}

		throw new NoSuchElementException();
	}

	public boolean hasPrevious() {
		return currentIndex > 0;
	}

	public BindingSet previous() {
		if (hasPrevious()) {
			BindingSet result = bindingSets.get(currentIndex - 1);
			currentIndex--;
			lastReturned = currentIndex;
			return result;
		}

		throw new NoSuchElementException();
	}

	/**
	 * Moves the cursor to the start of the query result, just before the first
	 * binding set. After calling this method, the result can be iterated over
	 * from scratch.
	 */
	public void beforeFirst() {
		currentIndex = 0;
	}

	/**
	 * Moves the cursor to the end of the query result, just after the last
	 * binding set.
	 */
	public void afterLast() {
		currentIndex = bindingSets.size() + 1;
	}

	/**
	 * Inserts the specified binding set into the list. The binding set is
	 * inserted immediately before the next element that would be returned by
	 * {@link #next()}, if any, and after the next element that would be returned
	 * by {@link #previous}, if any. (If the table contains no binding sets, the
	 * new element becomes the sole element on the table.) The new element is
	 * inserted before the implicit cursor: a subsequent call to <tt>next()</tt>
	 * would be unaffected, and a subsequent call to <tt>previous()</tt> would
	 * return the new binding set.
	 * 
	 * @param bindingSet
	 *        The binding set to insert.
	 */
	public void insert(BindingSet bindingSet) {
		insert(currentIndex, bindingSet);
	}

	public void insert(int index, BindingSet bindingSet) {
		bindingSets.add(index, bindingSet);

		if (currentIndex > index) {
			currentIndex++;
		}

		lastReturned = -1;
	}

	public void append(BindingSet bindingSet) {
		bindingSets.add(bindingSet);
		lastReturned = -1;
	}

	public void set(BindingSet bindingSet) {
		if (lastReturned == -1) {
			throw new IllegalStateException();
		}

		set(lastReturned, bindingSet);
	}

	public BindingSet set(int index, BindingSet bindingSet) {
		return bindingSets.set(index, bindingSet);
	}

	public void remove() {
		if (lastReturned == -1) {
			throw new IllegalStateException();
		}

		remove(lastReturned);

		if (currentIndex > lastReturned) {
			currentIndex--;
		}

		lastReturned = -1;
	}

	public BindingSet remove(int index) {
		BindingSet result = bindingSets.remove(index);

		if (currentIndex > index) {
			currentIndex--;
		}

		lastReturned = -1;

		return result;
	}

	public BindingSet singleResult()
		throws StoreException
	{
		if (bindingSets.isEmpty())
			throw new NoResultException("expected zero, but was:" + bindingSets.size());
		if (bindingSets.size() > 1)
			throw new MultipleResultException("expected zero, but was:" + bindingSets.size());
		return this.bindingSets.get(0);
	}

	public <C extends Collection<? super BindingSet>> C addTo(C bindingSets)
		throws StoreException
	{
		bindingSets.addAll(this.bindingSets);
		return bindingSets;
	}

	public List<BindingSet> asList()
		throws StoreException
	{
		return this.bindingSets;
	}

	public Set<BindingSet> asSet()
		throws StoreException
	{
		return new HashSet<BindingSet>(this.bindingSets);
	}

	public void clear() {
		bindingNames.clear();
		bindingSets.clear();
		currentIndex = 0;
		lastReturned = -1;
	}

	public void close() {
		// no-opp
	}

	@Override
	public MutableTupleResult clone()
		throws CloneNotSupportedException
	{
		MutableTupleResult clone = (MutableTupleResult)super.clone();
		clone.bindingNames = new LinkedHashSet<String>(bindingNames);
		clone.bindingSets = new LinkedList<BindingSet>(bindingSets);
		return clone;
	}
}
