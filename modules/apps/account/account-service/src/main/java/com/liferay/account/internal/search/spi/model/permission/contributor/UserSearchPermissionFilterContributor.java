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

package com.liferay.account.internal.search.spi.model.permission.contributor;

import com.liferay.account.model.AccountEntry;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroupRoleTable;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.ExistsFilter;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserGroupRoleLocalService;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.search.spi.model.permission.SearchPermissionFilterContributor;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Eric Yan
 */
@Component(immediate = true, service = SearchPermissionFilterContributor.class)
public class UserSearchPermissionFilterContributor
	implements SearchPermissionFilterContributor {

	@Override
	public void contribute(
		BooleanFilter booleanFilter, long companyId, long[] groupIds,
		long userId, PermissionChecker permissionChecker, String className) {

		if (!className.equals(User.class.getName())) {
			return;
		}

		if (_hasPermissionToManageAccountEntryUsers(companyId, userId)) {
			_addAllAccountEntryUsersFilter(booleanFilter);
		}
	}

	private void _addAllAccountEntryUsersFilter(BooleanFilter booleanFilter) {
		booleanFilter.add(new ExistsFilter("accountEntryIds"));
	}

	private boolean _hasPermissionToManageAccountEntryUsers(
		long companyId, long userId) {

		List<Role> roles = _roleLocalService.getResourceRoles(
			companyId, AccountEntry.class.getName(),
			ResourceConstants.SCOPE_GROUP_TEMPLATE, "0",
			ActionKeys.MANAGE_USERS);

		if (ListUtil.isNotEmpty(roles)) {
			DSLQuery dslQuery = DSLQueryFactoryUtil.count(
			).from(
				UserGroupRoleTable.INSTANCE
			).where(
				UserGroupRoleTable.INSTANCE.companyId.eq(
					companyId
				).and(
					UserGroupRoleTable.INSTANCE.userId.eq(userId)
				).and(
					UserGroupRoleTable.INSTANCE.roleId.in(
						ListUtil.toArray(roles, Role.ROLE_ID_ACCESSOR))
				)
			);

			long matchingUserGroupRolesCount =
				_userGroupRoleLocalService.dslQuery(dslQuery);

			if (matchingUserGroupRolesCount > 0) {
				return true;
			}
		}

		return false;
	}

	@Reference
	private RoleLocalService _roleLocalService;

	@Reference
	private UserGroupRoleLocalService _userGroupRoleLocalService;

}