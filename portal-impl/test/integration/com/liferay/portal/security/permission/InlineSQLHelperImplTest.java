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

package com.liferay.portal.security.permission;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Christopher Kian
 */
public class InlineSQLHelperImplTest extends InlineSQLHelperImpl {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_groupOne = GroupTestUtil.addGroup();
		_groupTwo = GroupTestUtil.addGroup();

		_groupIds = new long[] {_groupOne.getGroupId(), _groupTwo.getGroupId()};

		_originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();
	}

	@After
	public void tearDown() {
		PermissionThreadLocal.setPermissionChecker(_originalPermissionChecker);
	}

	@Test
	public void testAllGroupsMember() throws Exception {
		User user = UserTestUtil.addUser();

		_addRole(user, _groupOne, RoleConstants.SITE_MEMBER);
		_addRole(user, _groupTwo, RoleConstants.SITE_MEMBER);

		_users.add(user);

		String sql = _replacePermissionCheckJoin(
			_SQL_PLAIN, _CLASS_NAME, _CLASS_PK_FIELD, _USER_ID_FIELD,
			_GROUP_ID_FIELD, _groupIds, user);

		StringBundler groupPrimKeySB = new StringBundler(4);

		groupPrimKeySB.append(_RESOURCE_PERMISSION_PRIM_KEY);
		groupPrimKeySB.append(" IN (");
		groupPrimKeySB.append(StringUtil.merge(_groupIds));
		groupPrimKeySB.append(")");

		_assertContains(groupPrimKeySB.toString(), sql);
	}

	@Test
	public void testClauseOrdering() throws Exception {
		User user = UserTestUtil.addUser();

		_addRole(user, _groupOne, RoleConstants.SITE_MEMBER);
		_addRole(user, _groupTwo, RoleConstants.SITE_MEMBER);

		_users.add(user);

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(user);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		_assertClauseOrdering(_SQL_PLAIN + _SQL_WHERE, _WHERE_CLAUSE);
		_assertClauseOrdering(_SQL_PLAIN + _SQL_GROUP_BY, _GROUP_BY_CLAUSE);
		_assertClauseOrdering(_SQL_PLAIN + _SQL_ORDER_BY, _ORDER_BY_CLAUSE);
		_assertClauseOrdering(
			_SQL_PLAIN + _SQL_WHERE + _SQL_GROUP_BY, _GROUP_BY_CLAUSE);
		_assertClauseOrdering(
			_SQL_PLAIN + _SQL_WHERE + _SQL_ORDER_BY, _ORDER_BY_CLAUSE);
		_assertClauseOrdering(
			_SQL_PLAIN + _SQL_GROUP_BY + _SQL_ORDER_BY, _ORDER_BY_CLAUSE);
		_assertClauseOrdering(
			_SQL_PLAIN + _SQL_WHERE + _SQL_GROUP_BY + _SQL_ORDER_BY,
			_ORDER_BY_CLAUSE);
	}

	@Test
	public void testCompanyScope() throws Exception {
		Role role = RoleTestUtil.addRole(
			"scopeCompanyRole", RoleConstants.TYPE_REGULAR);

		_roles.add(role);

		User user = UserTestUtil.addUser();

		RoleLocalServiceUtil.addUserRole(user.getUserId(), role);

		_users.add(user);

		ResourcePermissionLocalServiceUtil.addResourcePermission(
			CompanyThreadLocal.getCompanyId(), _CLASS_NAME,
			ResourceConstants.SCOPE_COMPANY,
			String.valueOf(role.getCompanyId()), role.getRoleId(),
			ActionKeys.VIEW);

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(user);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		String sql = replacePermissionCheckJoin(
			_SQL_PLAIN, _CLASS_NAME, _CLASS_PK_FIELD, _USER_ID_FIELD,
			_GROUP_ID_FIELD, new long[] {_groupOne.getGroupId()}, null);

		Assert.assertSame(_SQL_PLAIN, sql);

		Assert.assertTrue(isEnabled(_groupOne.getGroupId()));
	}

	@Test
	public void testGroupScope() throws Exception {
		Role role = RoleTestUtil.addRole(
			"scopeGroupRole", RoleConstants.TYPE_SITE);

		_roles.add(role);

		User user = UserTestUtil.addUser();

		_addRole(user, _groupOne, "scopeGroupRole");

		_users.add(user);

		ResourcePermissionLocalServiceUtil.addResourcePermission(
			CompanyThreadLocal.getCompanyId(), _CLASS_NAME,
			ResourceConstants.SCOPE_GROUP,
			String.valueOf(_groupOne.getGroupId()), role.getRoleId(),
			ActionKeys.VIEW);

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(user);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		String sql = replacePermissionCheck(
			_SQL_PLAIN, _CLASS_NAME, _CLASS_PK_FIELD, _USER_ID_FIELD,
			_GROUP_ID_FIELD, new long[]{_groupOne.getGroupId()}, null);

		Assert.assertSame(_SQL_PLAIN, sql);

		Assert.assertTrue(isEnabled(_groupOne.getGroupId()));
	}

	@Test
	public void testGroupTemplateScope() throws Exception {
		Role role = RoleTestUtil.addRole(
			"scopeGroupTemplateRole", RoleConstants.TYPE_SITE);

		_roles.add(role);

		User user = UserTestUtil.addUser();

		_addRole(user, _groupOne, "scopeGroupTemplateRole");

		_users.add(user);

		ResourcePermissionLocalServiceUtil.addResourcePermission(
			CompanyThreadLocal.getCompanyId(), _CLASS_NAME,
			ResourceConstants.SCOPE_GROUP_TEMPLATE,
			String.valueOf(GroupConstants.DEFAULT_PARENT_GROUP_ID),
			role.getRoleId(), ActionKeys.VIEW);

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(user);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		String sql = replacePermissionCheckJoin(
			_SQL_PLAIN, _CLASS_NAME, _CLASS_PK_FIELD, _USER_ID_FIELD,
			_GROUP_ID_FIELD, new long[] {_groupOne.getGroupId()}, null);

		Assert.assertSame(_SQL_PLAIN, sql);

		Assert.assertTrue(isEnabled(_groupOne.getGroupId()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidCompany() throws Exception {
		Company company = CompanyTestUtil.addCompany();

		Group group = GroupTestUtil.addGroup();

		group.setCompanyId(company.getCompanyId());

		GroupLocalServiceUtil.updateGroup(group);

		User user = UserTestUtil.addUser();

		_addRole(user, group, RoleConstants.SITE_MEMBER);
		_addRole(user, _groupOne, RoleConstants.SITE_MEMBER);

		_users.add(user);

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(user);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		replacePermissionCheck(
			_SQL_PLAIN, _CLASS_NAME, _CLASS_PK_FIELD, _USER_ID_FIELD,
			_GROUP_ID_FIELD, new long[]{
				_groupOne.getGroupId(), group.getGroupId()
			},
			null);
	}

	@Test
	public void testIsEnabledSiteAdmin() throws Exception {
		User user = UserTestUtil.addUser();

		_addRole(user, _groupOne, RoleConstants.SITE_ADMINISTRATOR);

		_users.add(user);

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(user);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		Assert.assertFalse(isEnabled(_groupOne.getGroupId()));
		Assert.assertTrue(isEnabled(_groupTwo.getGroupId()));
	}

	@Test
	public void testIsNotEnabledForOmniAdmin() throws Exception {
		User user = UserTestUtil.addOmniAdminUser();

		_users.add(user);

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(user);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		Assert.assertFalse(isEnabled(_groupIds));
	}

	@Test
	public void testOneGroupAdminOneGroupMember() throws Exception {
		User user = UserTestUtil.addUser();

		_addRole(user, _groupOne, RoleConstants.SITE_ADMINISTRATOR);
		_addRole(user, _groupTwo, RoleConstants.SITE_MEMBER);

		_users.add(user);

		String sql = _replacePermissionCheckJoin(
			_SQL_PLAIN, _CLASS_NAME, _CLASS_PK_FIELD, _USER_ID_FIELD,
			_GROUP_ID_FIELD, _groupIds, user);

		StringBundler groupPrimKeySB = new StringBundler(4);

		groupPrimKeySB.append(_RESOURCE_PERMISSION_PRIM_KEY);
		groupPrimKeySB.append(" IN (");
		groupPrimKeySB.append(StringUtil.merge(_groupIds));
		groupPrimKeySB.append(")");

		_assertContains(groupPrimKeySB.toString(), sql);

		StringBundler adminPrimKeySB = new StringBundler(3);

		adminPrimKeySB.append(_RESOURCE_PERMISSION_PRIM_KEY);
		adminPrimKeySB.append(" = ");
		adminPrimKeySB.append(_groupOne.getGroupId());

		_assertContains(adminPrimKeySB.toString(), sql);
	}

	@Test
	public void testOneGroupMember() throws Exception {
		User user = UserTestUtil.addUser();

		_addRole(user, _groupOne, RoleConstants.SITE_MEMBER);

		_users.add(user);

		String sql = _replacePermissionCheckJoin(
			_SQL_PLAIN, _CLASS_NAME, _CLASS_PK_FIELD, _USER_ID_FIELD,
			_GROUP_ID_FIELD, new long[]{_groupOne.getGroupId()}, user);

		StringBundler groupPrimKeySB = new StringBundler(3);

		groupPrimKeySB.append(_RESOURCE_PERMISSION_PRIM_KEY);
		groupPrimKeySB.append(" = ");
		groupPrimKeySB.append(_groupOne.getGroupId());

		_assertContains(groupPrimKeySB.toString(), sql);
	}

	@Test
	public void testSQLComposition() throws Exception {
		User user = UserTestUtil.addUser();

		_addRole(user, _groupOne, RoleConstants.SITE_MEMBER);
		_addRole(user, _groupTwo, RoleConstants.SITE_MEMBER);

		_users.add(user);

		String sql = _replacePermissionCheckJoin(
			_SQL_PLAIN, _CLASS_NAME, _CLASS_PK_FIELD, _USER_ID_FIELD,
			_GROUP_ID_FIELD, _groupIds, user);

		_assertAndOrWhereClause(sql, _CLASS_PK_FIELD, false);

		StringBundler classNameSB = new StringBundler(4);

		classNameSB.append(_RESOURCE_PERMISSION);
		classNameSB.append(".name = '");
		classNameSB.append(_CLASS_NAME);
		classNameSB.append("'");

		_assertContains(classNameSB.toString(), sql);

		StringBundler companySB = new StringBundler(3);

		companySB.append(_RESOURCE_PERMISSION);
		companySB.append(".companyId = ");
		companySB.append(CompanyThreadLocal.getCompanyId());

		_assertContains(companySB.toString(), sql);

		StringBundler ownerSB = new StringBundler(3);

		ownerSB.append(_USER_ID_FIELD);
		ownerSB.append(" = ");
		ownerSB.append(user.getUserId());

		_assertContains(ownerSB.toString(), sql);

		_assertValidSql(sql);

		sql = _replacePermissionCheckJoin(
			_SQL_PLAIN + _SQL_WHERE, _CLASS_NAME, _CLASS_PK_FIELD,
			_USER_ID_FIELD, _GROUP_ID_FIELD, _groupIds, user);

		_assertAndOrWhereClause(sql, _CLASS_PK_FIELD, true);
		_assertValidSql(sql);
	}

	private void _addRole(User user, Group group, String roleName)
		throws Exception {

		Role role = RoleLocalServiceUtil.getRole(
			TestPropsValues.getCompanyId(), roleName);

		UserGroupRoleLocalServiceUtil.addUserGroupRoles(
			new long[] {user.getUserId()}, group.getGroupId(),
			role.getRoleId());
	}

	private void _assertAndOrWhereClause(
			String sql, String classPK, Boolean hasExistingWhereClause)
		throws Exception {

		String clause;

		if (hasExistingWhereClause) {
			clause = " AND ";
		}
		else {
			clause = _WHERE_CLAUSE;
		}

		String expectedSQL = clause + "(" + classPK + " IN (";

		_assertContains(expectedSQL, sql);
	}

	private void _assertClauseOrdering(String sql, String endingClause)
		throws Exception {

		String actualSql = replacePermissionCheckJoin(
			sql, _CLASS_NAME, _CLASS_PK_FIELD, _USER_ID_FIELD, _GROUP_ID_FIELD,
			_groupIds, null);

		int wherePos = actualSql.lastIndexOf(_WHERE_CLAUSE);
		int groupByPos = actualSql.indexOf(_GROUP_BY_CLAUSE);
		int orderByPos = actualSql.indexOf(_ORDER_BY_CLAUSE);

		Assert.assertNotEquals(wherePos, -1);

		if (endingClause.equals(_WHERE_CLAUSE)) {
			Assert.assertEquals(groupByPos, -1);
			Assert.assertEquals(orderByPos, -1);
		}
		else if (endingClause.equals(_GROUP_BY_CLAUSE)) {
			Assert.assertTrue(wherePos < groupByPos);
			Assert.assertEquals(orderByPos, -1);
		}
		else {
			Assert.assertTrue(wherePos < orderByPos);
		}
	}

	private void _assertContains(String containee, String container) {
		StringBundler msg = new StringBundler(5);

		msg.append("Assertion Failed: The following String:<[");
		msg.append(containee);
		msg.append("]> was not contained within:<[");
		msg.append(container);
		msg.append("]>");

		Assert.assertTrue(msg.toString(), container.contains(containee));
	}

	private void _assertValidSql(String sql) throws Exception {
		try (Connection connection = DataAccess.getConnection();
			PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.execute();
		}
	}

	private String _replacePermissionCheckJoin(
			String sql, String className, String classPKField,
			String userIdField, String groupIdField, long[] groupIds, User user)
		throws Exception {

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(user);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);

		return replacePermissionCheckJoin(
			sql, className, classPKField, userIdField, groupIdField, groupIds,
			null);
	}

	private static final String _CLASS_NAME =
		"com.liferay.journal.model.JournalArticle";

	private static final String _CLASS_PK_FIELD =
		"JournalArticle.resourcePrimKey";

	private static final String _GROUP_BY_CLAUSE = " GROUP BY ";

	private static final String _GROUP_ID_FIELD = "groupIdField";

	private static final String _ORDER_BY_CLAUSE = " ORDER BY ";

	private static final String _RESOURCE_PERMISSION = "ResourcePermission";

	private static final String _RESOURCE_PERMISSION_PRIM_KEY =
		_RESOURCE_PERMISSION + ".primKeyId";

	private static final String _SQL_GROUP_BY = " GROUP BY " + _CLASS_PK_FIELD;

	private static final String _SQL_ORDER_BY = " ORDER BY " + _CLASS_PK_FIELD;

	private static final String _SQL_PLAIN =
		"SELECT COUNT(*) FROM JournalArticle";

	private static final String _SQL_WHERE =
		" WHERE " + _CLASS_PK_FIELD + " != 0";

	private static final String _USER_ID_FIELD =
		_RESOURCE_PERMISSION + ".ownerId";

	private static final String _WHERE_CLAUSE = " WHERE ";

	private long[] _groupIds;

	@DeleteAfterTestRun
	private Group _groupOne;

	@DeleteAfterTestRun
	private Group _groupTwo;

	private PermissionChecker _originalPermissionChecker;

	@DeleteAfterTestRun
	private final List<Role> _roles = new ArrayList<>();

	@DeleteAfterTestRun
	private final List<User> _users = new ArrayList<>();

}