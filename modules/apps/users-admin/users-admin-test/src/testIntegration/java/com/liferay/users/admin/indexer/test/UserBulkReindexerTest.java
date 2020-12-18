package com.liferay.users.admin.indexer.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.search.spi.reindexer.BulkReindexer;
import com.liferay.portal.search.internal.reindexer.BulkReindexersHolder;

@RunWith(Arquillian.class)
public class UserBulkReindexerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testBulkReindexer() throws Exception {
		System.out.println("Testing Bulk reindexer");
		List<Long> classPKs = LongStream.rangeClosed(1000, 2100).boxed().collect(Collectors.toList());

		System.out.println(classPKs);

		BulkReindexer userBulkReindexer = _bulkReindexersHolder.getBulkReindexer(User.class.getName());

		userBulkReindexer.reindex(TestPropsValues.getCompanyId(), classPKs);
	}

	@Inject
	private static BulkReindexersHolder _bulkReindexersHolder;
}
