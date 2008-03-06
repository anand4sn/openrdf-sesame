/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.http;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP-related utility methods.
 */
public class HTTPUtil {

	/**
	 * Selects from a set of MIME types, the MIME type that has the highest
	 * quality score when matched with the Accept headers in the supplied
	 * request.
	 * 
	 * @param mimeTypes
	 *        An array of MIME type strings.
	 * @param request
	 *        The request to match the MIME types against.
	 * @return The MIME type that best matches the types that the client finds
	 *         acceptable, or <tt>null</tt> in case no acceptable MIME type
	 *         could be found.
	 */
	public static String selectPreferredMIMEType(String[] mimeTypes, HttpServletRequest request) {
		List<HeaderElement> acceptElements = getHeaderElements(request, "Accept");

		if (acceptElements.isEmpty()) {
			// Client does not specify any requirements, return first MIME type
			// from the list
			if (mimeTypes.length > 0) {
				return mimeTypes[0];
			}
		}

		String result = null;
		double highestQuality = 0.0;

		for (String mimeType : mimeTypes) {
			HeaderElement acceptType = matchAcceptHeader(mimeType, acceptElements);

			if (acceptType != null) {
				// quality defaults to 1.0
				double quality = 1.0;

				String qualityStr = acceptType.getParameterValue("q");
				if (qualityStr != null) {
					try {
						quality = Double.parseDouble(qualityStr);
					}
					catch (NumberFormatException e) {
						// Illegal quality value, assume it has a different meaning
						// and ignore it
					}
				}

				if (quality > highestQuality) {
					result = mimeType;
					highestQuality = quality;
				}
			}
		}

		return result;
	}

	/**
	 * Gets the elements of the request header with the specified name.
	 * 
	 * @param request
	 *        The request to get the header from.
	 * @param headerName
	 *        The name of the header to get the elements of.
	 * @return A List of {@link HeaderElement} objects.
	 */
	public static List<HeaderElement> getHeaderElements(HttpServletRequest request, String headerName) {
		List<HeaderElement> elemList = new ArrayList<HeaderElement>(8);

		Enumeration headerValues = request.getHeaders(headerName);
		while (headerValues.hasMoreElements()) {
			String value = (String)headerValues.nextElement();

			List<String> subValues = splitHeaderString(value, ',');

			for (String subValue : subValues) {
				// Ignore any empty header elements
				subValue = subValue.trim();
				if (subValue.length() > 0) {
					elemList.add(HeaderElement.parse(subValue));
				}
			}
		}

		return elemList;
	}

	/**
	 * Splits the supplied string into sub parts using the specified splitChar as
	 * a separator, ignoring occurrences of this character inside quoted strings.
	 * 
	 * @param s
	 *        The header string to split into sub parts.
	 * @param splitChar
	 *        The character to use as separator.
	 * @return A <tt>List</tt> of <tt>String</tt>s.
	 */
	public static List<String> splitHeaderString(String s, char splitChar) {
		List<String> result = new ArrayList<String>(8);

		boolean parsingQuotedString = false;
		int i, startIdx = 0;

		for (i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c == splitChar && !parsingQuotedString) {
				result.add(s.substring(startIdx, i));
				startIdx = i + 1;
			}
			else if (c == '"') {
				parsingQuotedString = !parsingQuotedString;
			}
		}

		if (startIdx < s.length()) {
			result.add(s.substring(startIdx));
		}

		return result;
	}

	/**
	 * Tries to match the specified MIME type spec against the list of Accept
	 * header elements, returning the applicable header element if available.
	 * 
	 * @param mimeTypeSpec
	 *        The MIME type to determine the quality for, e.g. "text/plain" or
	 *        "application/xml; charset=utf-8".
	 * @param acceptElements
	 *        A List of {@link HeaderElement} objects.
	 * @return The Accept header element that matches the MIME type spec most
	 *         closely, or <tt>null</tt> if no such header element could be
	 *         found.
	 */
	public static HeaderElement matchAcceptHeader(String mimeTypeSpec, List<HeaderElement> acceptElements) {
		HeaderElement mimeTypeElem = HeaderElement.parse(mimeTypeSpec);

		while (mimeTypeElem != null) {
			for (HeaderElement acceptElem : acceptElements) {
				if (_matchesAcceptHeader(mimeTypeElem, acceptElem)) {
					return acceptElem;
				}
			}

			// No match found, generalize the MIME type spec and try again
			mimeTypeElem = _generalizeMIMEType(mimeTypeElem);
		}

		return null;
	}

	private static boolean _matchesAcceptHeader(HeaderElement mimeTypeElem, HeaderElement acceptElem) {
		if (!mimeTypeElem.getValue().equals(acceptElem.getValue())) {
			return false;
		}

		// Values match, check parameters
		if (mimeTypeElem.getParameterCount() > acceptElem.getParameterCount()) {
			return false;
		}

		for (int i = 0; i < mimeTypeElem.getParameterCount(); i++) {
			if (!mimeTypeElem.getParameter(i).equals(acceptElem.getParameter(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Generalizes a MIME type element. The following steps are taken for
	 * generalization:
	 * <ul>
	 * <li>If the MIME type element has one or more parameters, the last
	 * parameter is removed.
	 * <li>Otherwise, if the MIME type element's subtype is not equal to '*'
	 * then it is set to this value.
	 * <li>Otherwise, if the MIME type element's type is not equal to '*' then
	 * it is set to this value.
	 * <li>Otherwise, the MIME type is equal to "*&slash;*" and cannot be
	 * generalized any further; <tt>null</tt> is returned.
	 * </ul>
	 * <p>
	 * Example generalizations:
	 * </p>
	 * <table>
	 * <tr>
	 * <th>input</th>
	 * <th>result</th>
	 * </tr>
	 * <tr>
	 * <td>application/xml; charset=utf-8</td>
	 * <td>application/xml</td>
	 * </tr>
	 * <tr>
	 * <td>application/xml</td>
	 * <td>application/*</td>
	 * </tr>
	 * <tr>
	 * <td>application/*</td>
	 * <td>*&slash;*</td>
	 * </tr>
	 * <tr>
	 * <td>*&slash;*</td>
	 * <td><tt>null</tt></td>
	 * </tr>
	 * </table>
	 * 
	 * @param mimeTypeElem
	 *        The MIME type element that should be generalized.
	 * @return The generalized MIME type element, or <tt>null</tt> if it could
	 *         not be generalized any further.
	 */
	private static HeaderElement _generalizeMIMEType(HeaderElement mimeTypeElem) {
		int parameterCount = mimeTypeElem.getParameterCount();
		if (parameterCount > 0) {
			// remove last parameter
			mimeTypeElem.removeParameter(parameterCount - 1);
		}
		else {
			String mimeType = mimeTypeElem.getValue();

			int slashIdx = mimeType.indexOf('/');
			if (slashIdx > 0) {
				String type = mimeType.substring(0, slashIdx);
				String subType = mimeType.substring(slashIdx + 1);

				if (!subType.equals("*")) {
					// generalize subtype
					mimeTypeElem.setValue(type + "/*");
				}
				else if (!type.equals("*")) {
					// generalize type
					mimeTypeElem.setValue("*/*");
				}
				else {
					// Cannot generalize any further
					mimeTypeElem = null;
				}
			}
			else {
				// invalid MIME type
				mimeTypeElem = null;
			}
		}

		return mimeTypeElem;
	}
}
