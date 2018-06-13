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

package com.liferay.portal.dao.orm.hibernate;

import com.liferay.portal.kernel.dao.orm.Criterion;
import com.liferay.portal.kernel.dao.orm.Disjunction;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.util.PropsValues;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Roberto Díaz
 */
public class RestrictionsFactory {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testInWithLessThanDatabaseInMaxParametersValue() {
		List<Integer> parameterList = new ArrayList();

		for (int i = 0; i < (PropsValues.DATABASE_IN_MAX_PARAMETERS - 1); i++) {
			parameterList.add(i);
		}

		Criterion inCriterion = RestrictionsFactoryUtil.in(
			"property", parameterList);

		Assert.assertFalse(inCriterion instanceof Disjunction);
	}

	@Test
	public void testInWithMoreThanDatabaseInMaxParametersValue() {
		List<Integer> parameterList = new ArrayList();

		for (int i = 0; i < (PropsValues.DATABASE_IN_MAX_PARAMETERS + 1); i++) {
			parameterList.add(i);
		}

		Criterion inCriterion = RestrictionsFactoryUtil.in(
			"property", parameterList);

		Assert.assertTrue(inCriterion instanceof Disjunction);
	}

}