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

package com.liferay.blogs.service.impl.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.blogs.kernel.model.BlogsEntry;
import com.liferay.blogs.kernel.service.BlogsEntryLocalService;
import com.liferay.blogs.kernel.service.BlogsEntryLocalServiceUtil;
import com.liferay.portal.kernel.comment.CommentManagerUtil;
import com.liferay.portal.kernel.service.IdentityServiceContextFunction;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Date;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Adolfo Pérez
 */
@RunWith(Arquillian.class)
@Sync
public class BlogsEntryLocalServiceImplTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@Test
	public void testAddDiscussion() throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		BlogsEntry blogsEntry = BlogsEntryLocalServiceUtil.addEntry(
			TestPropsValues.getUserId(), StringUtil.randomString(),
			StringUtil.randomString(), new Date(), serviceContext);

		long initialCommentsCount = CommentManagerUtil.getCommentsCount(
			BlogsEntry.class.getName(), blogsEntry.getEntryId());

		CommentManagerUtil.addComment(
			TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			BlogsEntry.class.getName(), blogsEntry.getEntryId(),
			StringUtil.randomString(),
			new IdentityServiceContextFunction(serviceContext));

		Assert.assertEquals(
			initialCommentsCount + 1,
			CommentManagerUtil.getCommentsCount(
				BlogsEntry.class.getName(), blogsEntry.getEntryId()));
	}

	@Test
	public void testDeleteDiscussion() throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		BlogsEntry blogsEntry = BlogsEntryLocalServiceUtil.addEntry(
			TestPropsValues.getUserId(), StringUtil.randomString(),
			StringUtil.randomString(), new Date(), serviceContext);

		Assert.assertTrue(
			CommentManagerUtil.hasDiscussion(
				BlogsEntry.class.getName(), blogsEntry.getEntryId()));

		CommentManagerUtil.deleteDiscussion(
			BlogsEntry.class.getName(), blogsEntry.getEntryId());

		Assert.assertFalse(
			CommentManagerUtil.hasDiscussion(
				BlogsEntry.class.getName(), blogsEntry.getEntryId()));
	}


	@Test
	public void testFindPreviousAndNext() throws Exception {

		int[][] testOne = new int[][]{
			new int[]{0, 0},
			new int[]{1, 1},
			new int[]{2, 2}};

		generateBlogsEntries(testOne);

		int[][] testTwo = new int[][]{
			new int[]{0, 0},
			new int[]{2, 2},
			new int[]{1, 1}};

		generateBlogsEntries(testTwo);

		int[][] testThree = new int[][]{
			new int[]{0, 0},
			new int[]{0, 1},
			new int[]{1, 2},
			new int[]{1, 3}};

		generateBlogsEntries(testThree);

		int[][] testFour = new int[][]{
			new int[]{0, 0},
			new int[]{0, 1},
			new int[]{0, 2},
			new int[]{2, 7},
			new int[]{2, 8},
			new int[]{2, 9},
			new int[]{1, 6},
			new int[]{1, 7},
			new int[]{1, 8}};

		generateBlogsEntries(testFour);
	}

	public void generateBlogsEntries(int[][] displayDateOrder)
		throws Exception {

		long day = 86400000;

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext();

		BlogsEntry[] blogsEntries = new BlogsEntry[displayDateOrder.length];

		for (int i = 0; i < displayDateOrder.length; i++) {
			int displayDate = displayDateOrder[i][0];
			int order = displayDateOrder[i][1];

			blogsEntries[order] = BlogsEntryLocalServiceUtil.addEntry(
				TestPropsValues.getUserId(), StringUtil.randomString(),
				StringUtil.randomString(), new Date(displayDate * day),
				serviceContext);
		}

		for (int i = 0; i < blogsEntries.length; i++) {
			long entryId = blogsEntries[i].getEntryId();

			BlogsEntry[] prevAndNextValues =
				_blogsEntryLocalService.getEntriesPrevAndNext(entryId);

			if (i > 0) {
				Assert.assertEquals(prevAndNextValues[0].getEntryId(),
					blogsEntries[i-1].getEntryId());
			}
			else {
				Assert.assertNull(prevAndNextValues[0]);
			}

			if (i < blogsEntries.length) {
				Assert.assertEquals(prevAndNextValues[2].getEntryId(),
					blogsEntries[i+1].getEntryId());
			}
			else {
				Assert.assertNull(prevAndNextValues[2]);
			}
		}

	}

	@Reference(unbind = "-")
	protected void setBlogsEntryLocalService(BlogsEntryLocalService blogsEntryLocalService) {
			_blogsEntryLocalService = blogsEntryLocalService;
	}

	private BlogsEntryLocalService _blogsEntryLocalService;
}