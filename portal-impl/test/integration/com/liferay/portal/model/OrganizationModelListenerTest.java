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

package com.liferay.portal.model;

import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.OrganizationConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.OrganizationTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.apache.commons.lang.ArrayUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Lianne Louie
 */
public class OrganizationModelListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_organization = OrganizationTestUtil.addOrganization();

		_indexer = IndexerRegistryUtil.nullSafeGetIndexer(User.class);

		_user = UserTestUtil.addUser();
		_user2 = UserTestUtil.addUser();
		_user3 = UserTestUtil.addUser();

		_indexer.reindex(_user);
		_indexer.reindex(_user2);
		_indexer.reindex(_user3);

		_suborganization = OrganizationTestUtil.addOrganization();

		UserLocalServiceUtil.addOrganizationUser(
			_suborganization.getOrganizationId(), _user.getUserId());

		_suborganization2 = OrganizationTestUtil.addOrganization();

		UserLocalServiceUtil.addOrganizationUser(
			_suborganization2.getOrganizationId(), _user2.getUserId());

		_suborganization3 = OrganizationTestUtil.addOrganization();

		UserLocalServiceUtil.addOrganizationUser(
			_suborganization3.getOrganizationId(), _user3.getUserId());
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		UserLocalServiceUtil.deleteUser(_user);
		UserLocalServiceUtil.deleteUser(_user2);
		UserLocalServiceUtil.deleteUser(_user3);

		OrganizationLocalServiceUtil.deleteOrganization(_suborganization3);
		OrganizationLocalServiceUtil.deleteOrganization(_suborganization2);
		OrganizationLocalServiceUtil.deleteOrganization(_suborganization);
		OrganizationLocalServiceUtil.deleteOrganization(_organization);
	}

	@Test
	public void testAddMultipleParentOrganizations() throws Exception {
		_suborganization.setParentOrganizationId(
			_organization.getOrganizationId());

		_updateOrganization(_suborganization);

		Assert.assertEquals(
			_organization.getOrganizationId(),
			_suborganization.getParentOrganizationId());

		_suborganization2.setParentOrganizationId(
			_suborganization.getOrganizationId());

		_updateOrganization(_suborganization2);

		Assert.assertEquals(
			_suborganization.getOrganizationId(),
			_suborganization2.getParentOrganizationId());

		long suborganizationParentId =
			_suborganization.getParentOrganizationId();
		long suborganization2ParentId =
			_suborganization2.getParentOrganizationId();

		//user2 should have access to suborg and org stuff
		Document document = _indexer.getDocument(_user2);
		String[] indexedParentOrganizationIds = document.getValues(
			"ancestorOrganizationIds");

		Assert.assertTrue(ArrayUtils.contains(
			indexedParentOrganizationIds,
			String.valueOf(suborganizationParentId)));
		Assert.assertTrue(ArrayUtils.contains(
			indexedParentOrganizationIds,
			String.valueOf(suborganization2ParentId)));
	}

	@Test
	public void testAddParentOrganization() throws Exception {
		_suborganization.setParentOrganizationId(
			_organization.getOrganizationId());

		_updateOrganization(_suborganization);

		Assert.assertEquals(
			_organization.getOrganizationId(),
			_suborganization.getParentOrganizationId());

		String[] expectedParentOrganizationIds = new String[1];

		expectedParentOrganizationIds[0] = String.valueOf(
			_suborganization.getParentOrganizationId());

		Document document = _indexer.getDocument(_user);
		String[] indexedParentOrganizationIds = document.getValues(
			"ancestorOrganizationIds");

		Assert.assertArrayEquals(
			expectedParentOrganizationIds,indexedParentOrganizationIds);
	}

	@Test
	public void testRemoveParentOrganization() throws Exception {
		//check setup
		_suborganization3.setParentOrganizationId(
			_organization.getOrganizationId());

		_updateOrganization(_suborganization3);

		Assert.assertEquals(
			_organization.getOrganizationId(),
			_suborganization3.getParentOrganizationId());

		//now remove

		_suborganization3.setParentOrganizationId(
			OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID);

		_updateOrganization(_suborganization3);

		//user should no longer have access to parent stuff
		Assert.assertEquals(
			_suborganization3.getParentOrganizationId(),
			OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID);

		Document document = _indexer.getDocument(_user3);
		String[] indexedParentOrganizationIds = document.getValues(
			"ancestorOrganizationIds");

		String[] expectedParentOrganizationIds = new String[0];
		Assert.assertArrayEquals(
			expectedParentOrganizationIds,indexedParentOrganizationIds);
	}

	private Organization _updateOrganization(Organization organization)
		throws Exception {
		
		Group organizationGroup = organization.getGroup();

		return OrganizationLocalServiceUtil.updateOrganization(
			organization.getCompanyId(), organization.getOrganizationId(),
			organization.getParentOrganizationId(), organization.getName(),
			organization.getType(), organization.getRegionId(),
			organization.getCountryId(), organization.getStatusId(),
			organization.getComments(), false, null, organizationGroup.isSite(),
			null);
	}

	private static Indexer<User> _indexer;
	private static Organization _organization;
	private static Organization _suborganization;
	private static Organization _suborganization2;
	private static Organization _suborganization3;
	private static User _user;
	private static User _user2;
	private static User _user3;

}