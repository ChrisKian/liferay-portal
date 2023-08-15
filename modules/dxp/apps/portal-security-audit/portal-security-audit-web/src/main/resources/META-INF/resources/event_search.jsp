<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
AuditDisplayContext auditDisplayContext = (AuditDisplayContext)request.getAttribute(AuditDisplayContext.class.getName());

SearchContainer<?> searchContainer = (SearchContainer<?>)request.getAttribute("liferay-ui:search:searchContainer");
%>

<liferay-ui:search-toggle
	buttonLabel="search"
	displayTerms="<%= searchContainer.getDisplayTerms() %>"
	id="toggle_id_audit_event_search"
>
	<aui:input label="user-id" name="userId" value="<%= (userId != 0) ? String.valueOf(userId) : StringPool.BLANK %>" />

	<aui:input label="user-name" name="userName" value="<%= userName %>" />

	<aui:input label="group-id" name="groupId" value="<%= (groupId != 0) ? String.valueOf(groupId) : StringPool.BLANK %>" />

	<aui:input label="resource-id" name="classPK" value="<%= classPK %>" />

	<aui:input label="class-name" name="className" value="<%= className %>" />

	<aui:input label="resource-action" name="eventType" value="<%= eventType %>" />

	<aui:input label="client-ip" name="clientIP" value="<%= clientIP %>" />

	<aui:input label="client-host" name="clientHost" value="<%= clientHost %>" />

	<aui:input label="server-name" name="serverName" value="<%= serverName %>" />

	<aui:input label="server-port" name="serverPort" value="<%= (serverPort != 0) ? String.valueOf(serverPort) : StringPool.BLANK %>" />

	<aui:field-wrapper label="start-date">
		<liferay-ui:input-date
			dayParam="startDateDay"
			dayValue="<%= auditDisplayContext.getStartDateDay() %>"
			monthParam="startDateMonth"
			monthValue="<%= auditDisplayContext.getStartDateMonth() %>"
			name="startDate"
			useNamespace="<%= false %>"
			yearParam="startDateYear"
			yearValue="<%= auditDisplayContext.getStartDateYear() %>"
		/>

		<liferay-ui:input-time
			amPmParam="startDateAmPm"
			amPmValue="<%= auditDisplayContext.getStartDateAmPm() %>"
			hourParam="startDateHour"
			hourValue="<%= auditDisplayContext.getStartDateHour() %>"
			minuteParam="startDateMinute"
			minuteValue="<%= auditDisplayContext.getStartDateMinute() %>"
			name="startDateTime"
			useNamespace="<%= false %>"
		/>
	</aui:field-wrapper>

	<aui:field-wrapper label="end-date">
		<liferay-ui:input-date
			dayParam="endDateDay"
			dayValue="<%= auditDisplayContext.getEndDateDay() %>"
			monthParam="endDateMonth"
			monthValue="<%= auditDisplayContext.getEndDateMonth() %>"
			name="endDate"
			useNamespace="<%= false %>"
			yearParam="endDateYear"
			yearValue="<%= auditDisplayContext.getEndDateYear() %>"
		/>

		<liferay-ui:input-time
			amPmParam="endDateAmPm"
			amPmValue="<%= auditDisplayContext.getEndDateAmPm() %>"
			hourParam="endDateHour"
			hourValue="<%= auditDisplayContext.getEndDateHour() %>"
			minuteParam="endDateMinute"
			minuteValue="<%= auditDisplayContext.getEndDateMinute() %>"
			name="endDateTime"
			useNamespace="<%= false %>"
		/>
	</aui:field-wrapper>
</liferay-ui:search-toggle>