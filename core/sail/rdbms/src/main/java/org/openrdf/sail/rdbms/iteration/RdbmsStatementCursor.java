/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.iteration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.iteration.base.RdbmCursorBase;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsStatement;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.rdbms.schema.IdSequence;
import org.openrdf.sail.rdbms.schema.ValueTable;

/**
 * Converts a {@link ResultSet} into a {@link RdbmsStatement} in an iteration.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsStatementCursor extends RdbmCursorBase<RdbmsStatement> {

	private RdbmsValueFactory vf;

	private IdSequence ids;

	public RdbmsStatementCursor(RdbmsValueFactory vf, PreparedStatement stmt, IdSequence ids)
		throws SQLException
	{
		super(stmt);
		this.vf = vf;
		this.ids = ids;
	}

	@Override
	protected RdbmsStatement convert(ResultSet rs)
		throws SQLException
	{
		RdbmsResource ctx = createResource(rs, 1);
		RdbmsResource subj = createResource(rs, 3);
		RdbmsURI pred = (RdbmsURI)createResource(rs, 5);
		RdbmsValue obj = createValue(rs, 7);
		return new RdbmsStatement(subj, pred, obj, ctx);
	}

	private RdbmsResource createResource(ResultSet rs, int index)
		throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (id.longValue() == ValueTable.NIL_ID)
			return null;
		String stringValue = rs.getString(index + 1);
		return vf.getRdbmsResource(id, stringValue);
	}

	private RdbmsValue createValue(ResultSet rs, int index)
		throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (ids.isLiteral(id)) {
			String label = rs.getString(index + 1);
			String datatype = rs.getString(index + 2);
			String language = rs.getString(index + 3);
			return vf.getRdbmsLiteral(id, label, language, datatype);
		}
		return createResource(rs, index);
	}

}
