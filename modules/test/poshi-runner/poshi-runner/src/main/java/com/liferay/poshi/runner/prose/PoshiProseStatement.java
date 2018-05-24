/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.poshi.runner.prose;

import com.liferay.poshi.runner.util.Dom4JUtil;
import com.liferay.poshi.runner.util.StringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;
import org.dom4j.tree.DefaultAttribute;

/**
 * @author Yi-Chen Tsai
 */
public class PoshiProseStatement {

	public PoshiProseStatement(String proseStatement) {
		for (String proseKeyword : PoshiProseStatement.KEYWORDS) {
			if (proseStatement.startsWith(proseKeyword)) {
				proseStatement = StringUtil.replaceFirst(
					proseStatement, proseKeyword, "");

				break;
			}
		}

		_proseStatement = proseStatement.trim();

		_poshiProseMatcher = PoshiProseMatcher.getPoshiProseMatcher(
			_proseStatement.replaceAll(_varValuePattern.pattern(), "\"\""));

		List<String> varNames = _poshiProseMatcher.getVarNames();

		List<String> varValues = new ArrayList<>();

		Matcher matcher = _varValuePattern.matcher(_proseStatement);

		while (matcher.find()) {
			varValues.add(matcher.group(1));
		}

		for (int i = 0; i < varNames.size(); i++) {
			String varName = varNames.get(i);

			if (_varMap.containsKey(varName)) {
				StringBuilder sb = new StringBuilder();

				sb.append("Duplicate variable value assignment: ${");
				sb.append(varName);
				sb.append("} already has a value of ");
				sb.append(_varMap.get(varName));
				sb.append("\nProse statement: ");
				sb.append(_proseStatement);
				sb.append("\nMatching macro prose statement: ");
				sb.append(_poshiProseMatcher.getPoshiProse());

				throw new RuntimeException(sb.toString());
			}

			_varMap.put(varName, varValues.get(i));
		}
	}

	public Element toElement() {
		Element element = Dom4JUtil.getNewElement(
			"execute", null,
			new DefaultAttribute(
				"macro",
				_poshiProseMatcher.getMacroNamespacedClassCommandName()));

		for (Map.Entry<String, String> varMapEntry : _varMap.entrySet()) {
			element.add(
				Dom4JUtil.getNewElement(
					"var", null,
					new DefaultAttribute("name", varMapEntry.getKey()),
					new DefaultAttribute("value", varMapEntry.getValue())));
		}

		return element;
	}

	protected static final String[] KEYWORDS = {"And", "Given", "Then", "When"};

	private static final Pattern _varValuePattern = Pattern.compile(
		"\"(.*?)\"");

	private final PoshiProseMatcher _poshiProseMatcher;
	private final String _proseStatement;
	private final Map<String, String> _varMap = new LinkedHashMap<>();

}