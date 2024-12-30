///**
// * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
// * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
// */
//
//package com.liferay.captcha.configuration;
//
//import com.liferay.osgi.util.ServiceTrackerFactory;
//import org.osgi.framework.FrameworkUtil;
//import org.osgi.util.tracker.ServiceTracker;
//
///**
// * @author Christopher Kian
// */
//public class CaptchaConfigurationHelperUtil {
//
//	public static CaptchaConfiguration getCaptchaConfiguration() {
//		CaptchaConfigurationHelper captchaConfigurationHelper =
//			getCaptchaConfigurationHelper();
//
//		return captchaConfigurationHelper.getCaptchaConfiguration();
//	}
//
//	public static CaptchaConfigurationHelper
//		getCaptchaConfigurationHelper() {
//
//		return _serviceTracker.getService();
//	}
//
//	private static final ServiceTracker
//		<CaptchaConfigurationHelper, CaptchaConfigurationHelper>
//			_serviceTracker = ServiceTrackerFactory.open(
//				FrameworkUtil.getBundle(
//					CaptchaConfigurationHelperUtil.class),
//				CaptchaConfigurationHelper.class);
//}