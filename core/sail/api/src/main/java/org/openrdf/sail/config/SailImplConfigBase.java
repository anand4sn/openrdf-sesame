/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;

import static org.openrdf.sail.config.SailConfigSchema.SAILTYPE;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.util.ModelException;
import org.openrdf.store.StoreConfigException;

/**
 * @author Herko ter Horst
 */
public class SailImplConfigBase implements SailImplConfig {

	private String type;

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public SailImplConfigBase() {
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public SailImplConfigBase(String type) {
		this();
		setType(type);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void validate()
		throws StoreConfigException
	{
		if (type == null) {
			throw new StoreConfigException("No type specified for sail implementation");
		}
	}

	public Resource export(Model model) {
		ValueFactoryImpl vf = ValueFactoryImpl.getInstance();

		BNode implNode = vf.createBNode();

		if (type != null) {
			model.add(implNode, SAILTYPE, vf.createLiteral(type));
		}

		return implNode;
	}

	public void parse(Model model, Resource implNode)
		throws StoreConfigException
	{
		try {
			Literal typeLit = model.filter(implNode, SAILTYPE, null).objectLiteral();
			if (typeLit != null) {
				setType(typeLit.getLabel());
			}
		}
		catch (ModelException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}
}
