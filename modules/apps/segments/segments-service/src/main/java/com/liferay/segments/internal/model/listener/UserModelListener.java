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

package com.liferay.segments.internal.model.listener;

import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.segments.service.SegmentsEntryRelLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christopher Kian
 */
@Component(immediate = true, service = ModelListener.class)
public class UserModelListener extends BaseModelListener<User> {

	@Override
	public void onAfterUpdate(User originalUser, User user)
		throws ModelListenerException {

		if (originalUser.getModifiedDate() != user.getModifiedDate()) {
			long classNameId = _classNameLocalService.getClassNameId(
				User.class.getName());

			_segmentsEntryRelLocalService.deleteSegmentsEntryRels(
				classNameId, user.getUserId());
		}
	}

	@Reference
	private ClassNameLocalService _classNameLocalService;

	@Reference
	private SegmentsEntryRelLocalService _segmentsEntryRelLocalService;

}