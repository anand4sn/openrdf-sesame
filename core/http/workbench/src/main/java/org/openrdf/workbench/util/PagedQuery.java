/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.query.QueryLanguage;

/**
 * @author dale
 */
public class PagedQuery {

	private static final Logger LOGGER = LoggerFactory.getLogger(PagedQuery.class);

	private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;

	private static final Pattern LIMIT_OR_OFFSET = Pattern.compile("((limit)|(offset))\\s+\\d+", FLAGS);

	private static final Pattern SPLITTER = Pattern.compile("\\s");

	private static final Pattern OFFSET_PATTERN = Pattern.compile("\\boffset\\s+\\d+\\b", FLAGS);

	private static final Pattern LIMIT_PATTERN = Pattern.compile("\\blimit\\s+\\d+\\b", FLAGS);

	private static final Pattern SERQL_NAMESPACE = Pattern.compile("\\busing namespace\\b", FLAGS);

	private final String modifiedQuery;
	
	/***
	 * Add or modify the limit and offset clauses of the query to be executed so
	 * that only those results to be displayed are requested from the query
	 * engine.
	 * 
	 * @param query
	 *        as it was specified by the user
	 * @param language
	 *        SPARQL or SeRQL, as specified by the user
	 * @param requestLimit
	 *        maximum number of results to return, as specified by the URL query
	 *        parameters or cookies
	 * @param requestOffset
	 *        which result to start at when populating the result set
	 * @returns the user's query with appended or modified LIMIT and OFFSET
	 *          clauses
	 */
	public PagedQuery(final String query, final QueryLanguage language, final int requestLimit,
			final int requestOffset)
	{
		LOGGER.info("Query Language: {}, requestLimit: " + requestLimit + ", requestOffset: " + requestOffset,
				language);
		LOGGER.info("Query: {}", query);

		String rval = query;

		// requestLimit <= 0 actually means don't limit display
		if (requestLimit > 0) {
			/* the matcher on the pattern will have a group for "limit l#" as 
			    well as a group for l#, similarly for "offset o#" and o#. If 
			    either doesn't exist, it can be appended at the end. */
			int queryLimit = -1;
			int queryOffset = -1;
			final Matcher matcher = LIMIT_OR_OFFSET.matcher(query);
			while (matcher.find()) {
				final String clause = matcher.group();
				final int value = Integer.parseInt(SPLITTER.split(clause)[1]);
				if (clause.startsWith("limit")) {
					queryLimit = value;
				}
				else {
					queryOffset = value;
				}
			}

			final boolean queryLimitExists = (queryLimit >= 0);
			final boolean queryOffsetExists = (queryOffset >= 0);
			final int maxQueryCount = getMaxQueryResultCount(queryLimit, queryOffset, queryLimitExists,
					queryOffsetExists);
			// gracefully handle malicious value
			final int offset = (requestOffset < 0) ? 0 : requestOffset;
			final int maxRequestCount = requestLimit + offset;
			final int limitSubstitute = (maxRequestCount < maxQueryCount) ? requestLimit : queryLimit - offset;
			rval = modifyLimit(language, rval, queryLimit, queryLimitExists, queryOffsetExists, limitSubstitute);
			rval = modifyOffset(language, offset, rval, queryOffset, queryOffsetExists);
			LOGGER.info("Modified Query: {}", rval);
		}

		this.modifiedQuery = rval;
	}
	
	@Override
	public String toString() {
		return this.modifiedQuery;
	}

	/**
	 * @param queryLimit
	 * @param queryOffset
	 * @param queryLimitExists
	 * @param queryOffsetExists
	 * @return
	 */
	private static int getMaxQueryResultCount(final int queryLimit, final int queryOffset,
			final boolean queryLimitExists, final boolean queryOffsetExists)
	{
		final int maxQueryCount = queryLimitExists ? (queryLimit + (queryOffsetExists ? queryOffset : 0))
				: Integer.MAX_VALUE;
		return maxQueryCount;
	}

	/**
	 * @param language
	 * @param offset
	 * @param query
	 * @param queryOffset
	 * @param queryOffsetExists
	 * @return
	 */
	private static String modifyOffset(final QueryLanguage language, final int offset, final String query,
			final int queryOffset, final boolean queryOffsetExists)
	{
		String rval = query;
		if (queryOffsetExists) {
			final int offsetSubstitute = queryOffset + offset;
			if (offsetSubstitute != offset) {
				// do a clause replacement
				final Matcher offsetMatcher = OFFSET_PATTERN.matcher(rval);
				final StringBuffer buffer = new StringBuffer();
				offsetMatcher.find();
				offsetMatcher.appendReplacement(buffer, "offset " + offsetSubstitute);
				offsetMatcher.appendTail(buffer);
				rval = buffer.toString();
			}
		}
		else {
			final String newOffsetClause = "offset " + offset;
			if (QueryLanguage.SPARQL == language) {
				if (offset > 0) {
					rval = ensureNewlineAndAppend(rval, newOffsetClause);
				}
			}
			else {
				/* SeRQL, add the clause before before the namespace
				 * section
				 */
				rval = insertAtMatchOnOwnLine(SERQL_NAMESPACE, rval, newOffsetClause);
			}
		}
		return rval;
	}

	/**
	 * @param original
	 * @param append
	 * @return
	 */
	private static String ensureNewlineAndAppend(final String original, final String append) {
		final StringBuffer buffer = new StringBuffer(original.length() + append.length() + 1);
		buffer.append(original);
		if (buffer.charAt(buffer.length()-1) != '\n') {
			buffer.append('\n');
		}

		return buffer.append(append).toString();
	}

	/**
	 * @param language
	 * @param query
	 * @param queryLimit
	 * @param queryLimitExists
	 * @param queryOffsetExists
	 * @param limitSubstitute
	 * @return
	 */
	private static String modifyLimit(final QueryLanguage language, final String query, final int queryLimit,
			final boolean queryLimitExists, final boolean queryOffsetExists, final int limitSubstitute)
	{
		String rval = query;
		
		/* In SPARQL, LIMIT and/or OFFSET can occur at the end, in 
		 * either order. In SeRQL, LIMIT and/or OFFSET must be 
		 * immediately prior to the *optional* namespace declaration 
		 * section (which is itself last), and LIMIT must precede OFFSET.
		 * This code makes no attempt to correct if the user places them
		 * out of order in the query. 
		 */
		if (queryLimitExists) {
			if (limitSubstitute != queryLimit) {
				// do a clause replacement
				final Matcher limitMatcher = LIMIT_PATTERN.matcher(rval);
				final StringBuffer buffer = new StringBuffer();
				limitMatcher.find();
				limitMatcher.appendReplacement(buffer, "limit " + limitSubstitute);
				limitMatcher.appendTail(buffer);
				rval = buffer.toString();
			}
		}
		else {
			final String newLimitClause = "limit " + limitSubstitute;
			if (QueryLanguage.SPARQL == language) {
				rval = ensureNewlineAndAppend(rval, newLimitClause);
			}
			else {
				/* SeRQL, add the clause before any offset clause or the 
				 * namespace section
				 */
				final Pattern pattern = queryOffsetExists ? OFFSET_PATTERN : SERQL_NAMESPACE;
				rval = insertAtMatchOnOwnLine(pattern, rval, newLimitClause);
			}
		}
		return rval;
	}

	/**
	 * Insert a given string into another string at the point at which the given
	 * matcher matches, making sure to place the insertion string on its own
	 * line. If there is no match, appends to end on own line.
	 * 
	 * @param pattern
	 *        pattern to search for insertion location
	 * @param orig
	 *        string to perform insertion on
	 * @param insert
	 *        string to insert on own line
	 * @returns result of inserting text
	 */
	private static String insertAtMatchOnOwnLine(final Pattern pattern, final String orig, final String insert)
	{
		final Matcher matcher = pattern.matcher(orig);
		final boolean found = matcher.find();
		final int location = found ? matcher.start() : orig.length();
		final StringBuilder builder = new StringBuilder(orig.length() + insert.length() + 2);
		builder.append(orig.substring(0, location));
		if (builder.charAt(builder.length() - 1) != '\n') {
			builder.append('\n');
		}

		builder.append(insert);
		final String end = orig.substring(location);
		if (!end.startsWith("\n")) {
			builder.append('\n');
		}

		builder.append(end);
		return builder.toString();
	}
}
