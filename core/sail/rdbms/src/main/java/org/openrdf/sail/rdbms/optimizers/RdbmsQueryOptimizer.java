/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.optimizers;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.sail.rdbms.optimizers.SameTermFilterRdbmsOptimizer;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.schema.BNodeTable;
import org.openrdf.sail.rdbms.schema.HashTable;
import org.openrdf.sail.rdbms.schema.LiteralTable;
import org.openrdf.sail.rdbms.schema.URITable;

/**
 * Facade to the underlying RDBMS optimizations.
 * 
 * @author James Leigh
 */
public class RdbmsQueryOptimizer {

	private RdbmsValueFactory vf;

	private URITable uris;

	private BNodeTable bnodes;

	private LiteralTable literals;

	private SelectQueryOptimizerFactory factory;

	private HashTable hashTable;

	public void setSelectQueryOptimizerFactory(SelectQueryOptimizerFactory factory) {
		this.factory = factory;
	}

	public void setValueFactory(RdbmsValueFactory vf) {
		this.vf = vf;
	}

	public void setUriTable(URITable uris) {
		this.uris = uris;
	}

	public void setBnodeTable(BNodeTable bnodes) {
		this.bnodes = bnodes;
	}

	public void setLiteralTable(LiteralTable literals) {
		this.literals = literals;
	}

	public void setHashTable(HashTable hashTable) {
		this.hashTable = hashTable;
	}

	public TupleExpr optimize(TupleExpr expr, Dataset dataset, BindingSet bindings, EvaluationStrategy strategy)
	{
		// Clone the tuple expression to allow for more aggressive optimisations
		TupleExpr tupleExpr = expr.clone();

		if (!(tupleExpr instanceof QueryRoot)) {
			// Add a dummy root node to the tuple expressions to allow the
			// optimisers to modify the actual root node
			tupleExpr = new QueryRoot(tupleExpr);
		}

		coreOptimizations(strategy, tupleExpr, dataset, bindings);

		rdbmsOptimizations(tupleExpr, dataset, bindings);

		new SqlConstantOptimizer().optimize(tupleExpr, dataset, bindings);

		return tupleExpr;
	}

	private void coreOptimizations(EvaluationStrategy strategy, TupleExpr expr, Dataset dataset,
			BindingSet bindings)
	{
		new BindingAssigner().optimize(expr, dataset, bindings);
		new ConstantOptimizer(strategy).optimize(expr, dataset, bindings);
		new CompareOptimizer().optimize(expr, dataset, bindings);
		new ConjunctiveConstraintSplitter().optimize(expr, dataset, bindings);
		new DisjunctiveConstraintOptimizer().optimize(expr, dataset, bindings);
		new SameTermFilterRdbmsOptimizer().optimize(expr, dataset, bindings);
	}

	protected void rdbmsOptimizations(TupleExpr expr, Dataset dataset, BindingSet bindings) {
		new ValueIdLookupOptimizer(vf).optimize(expr, dataset, bindings);
		factory.createRdbmsFilterOptimizer().optimize(expr, dataset, bindings);
		new VarColumnLookupOptimizer().optimize(expr, dataset, bindings);
		ValueJoinOptimizer valueJoins = new ValueJoinOptimizer();
		valueJoins.setBnodeTable(bnodes);
		valueJoins.setUriTable(uris);
		valueJoins.setLiteralTable(literals);
		valueJoins.setHashTable(hashTable);
		valueJoins.optimize(expr, dataset, bindings);
	}

}
