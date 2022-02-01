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

package com.liferay.segments.internal.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.messaging.Destination;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.segments.constants.SegmentsEntryConstants;
import com.liferay.segments.criteria.Criteria;
import com.liferay.segments.criteria.CriteriaSerializer;
import com.liferay.segments.criteria.contributor.SegmentsCriteriaContributor;
import com.liferay.segments.internal.constants.SegmentsDestinationNames;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.provider.SegmentsEntryProvider;
import com.liferay.segments.test.util.SegmentsTestUtil;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christopher Kian
 */
@RunWith(Arquillian.class)
public class BaseSegmentsEntryProviderTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testGetSegmentsEntryIds() throws Exception {
		_group = GroupTestUtil.addGroup();

		_user = UserTestUtil.addUser(_group.getGroupId());

		long[] existingGlobalSegments =
			_segmentsEntryProvider.getSegmentsEntryIds(
				_user.getGroupId(), User.class.getName(), _user.getUserId());

		Criteria criteria = new Criteria();

		_userSegmentsCriteriaContributor.contribute(
			criteria, "(jobTitle eq 'foo')", Criteria.Conjunction.AND);

		_segmentsEntry1 = SegmentsTestUtil.addSegmentsEntry(
			_user.getGroupId(), CriteriaSerializer.serialize(criteria),
			User.class.getName());

		criteria = new Criteria();

		_userSegmentsCriteriaContributor.contribute(
			criteria, "(jobTitle eq 'bar')", Criteria.Conjunction.AND);

		_segmentsEntry2 = SegmentsTestUtil.addSegmentsEntry(
			_user.getGroupId(), CriteriaSerializer.serialize(criteria),
			User.class.getName());

		_userLocalService.updateJobTitle(_user.getUserId(), "foo");

		long[] expectedSegmentsEntryIds = ArrayUtil.append(
			existingGlobalSegments, _segmentsEntry1.getSegmentsEntryId());

		Arrays.sort(expectedSegmentsEntryIds);

		long[] actualSegmentsEntryIds =
			_segmentsEntryProvider.getSegmentsEntryIds(
				_user.getGroupId(), User.class.getName(), _user.getUserId());

		Arrays.sort(actualSegmentsEntryIds);

		Assert.assertArrayEquals(
			expectedSegmentsEntryIds, actualSegmentsEntryIds);

		Message message = new Message();

		message.put("companyId", _segmentsEntry1.getCompanyId());
		message.put("segmentsEntryId", _segmentsEntry1.getSegmentsEntryId());
		message.put("type", _segmentsEntry1.getType());

		Destination destination = MessageBusUtil.getDestination(
			SegmentsDestinationNames.SEGMENTS_ENTRY_REINDEX);

		destination.send(message);

		_userLocalService.updateJobTitle(_user.getUserId(), "bar");

		expectedSegmentsEntryIds = ArrayUtil.append(
			existingGlobalSegments, _segmentsEntry2.getSegmentsEntryId());

		Arrays.sort(expectedSegmentsEntryIds);

		actualSegmentsEntryIds = _segmentsEntryProvider.getSegmentsEntryIds(
			_user.getGroupId(), User.class.getName(), _user.getUserId());

		Arrays.sort(actualSegmentsEntryIds);

		Assert.assertArrayEquals(
			expectedSegmentsEntryIds, actualSegmentsEntryIds);
	}

	@DeleteAfterTestRun
	private Group _group;

	@DeleteAfterTestRun
	private SegmentsEntry _segmentsEntry1;

	@DeleteAfterTestRun
	private SegmentsEntry _segmentsEntry2;

	@Inject(
		filter = "segments.entry.provider.source=" + SegmentsEntryConstants.SOURCE_DEFAULT,
		type = SegmentsEntryProvider.class
	)
	private SegmentsEntryProvider _segmentsEntryProvider;

	@DeleteAfterTestRun
	private User _user;

	@Inject
	private UserLocalService _userLocalService;

	@Inject(
		filter = "segments.criteria.contributor.key=user",
		type = SegmentsCriteriaContributor.class
	)
	private SegmentsCriteriaContributor _userSegmentsCriteriaContributor;

}