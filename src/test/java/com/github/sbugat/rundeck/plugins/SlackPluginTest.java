package com.github.sbugat.rundeck.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Rundeck slack plugin test class.
 *
 * @author Sylvain Bugat
 *
 */
public class SlackPluginTest {

	private static final String NODE_1 = "1node1";
	private static final String NODE_2 = "2node2";

	private static final String OPTION_1 = "option1";
	private static final String OPTION_1_VALUE = "value1";
	private static final String OPTION_2 = "option2";
	private static final String OPTION_2_VALUE = "value2";

	private static final String TOTAL = "total";

	@Test
	public void testGetOptionsEmpty() throws Exception {

		final String optionsPart = (String) callMethod(new SlackPlugin(), "getOptions");

		Assertions.assertThat(optionsPart).isEmpty();
	}

	@Test
	public void testGetDownloadOptionPartEmptyExecutionData() throws Exception {

		final Map<String, Object> executionData = ImmutableMap.of();

		final CharSequence downloadOptionPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getDownloadOptionPart", executionData);

		Assertions.assertThat(downloadOptionPart).isEmpty();
	}

	@Test
	public void testGetDownloadOptionPartSuccess() throws Exception {

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of();

		final Map<String, ? extends Object> executionData = ImmutableMap.of("context", contextMap, "status", "success");

		final CharSequence downloadOptionPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getDownloadOptionPart", executionData);

		Assertions.assertThat(downloadOptionPart).isEmpty();
	}

	@Test
	public void testGetDownloadOptionPartFailure() throws Exception {

		final Map<String, String> optionContextMap = ImmutableMap.of();
		final Map<String, String> jobContextMap = ImmutableMap.of("serverUrl", "http://serverurl:4440");

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap, "option", optionContextMap);

		final Map<String, ? extends Object> executionData = ImmutableMap.of("context", contextMap, "status", "failure", "project", "projectName", "id", "executionId");

		final CharSequence downloadOptionPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getDownloadOptionPart", executionData);

		Assertions.assertThat(downloadOptionPart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-download-failure.txt")));
	}

	@Test
	public void testGetDownloadOptionPartAbortedWithOptions() throws Exception {

		final Map<String, String> optionContextMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE);
		final Map<String, String> jobContextMap = ImmutableMap.of("serverUrl", "http://serverurl:4440");
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap, "option", optionContextMap);

		final Map<String, ? extends Object> executionData = ImmutableMap.of("context", contextMap, "status", "aborted", "project", "projectName", "id", "executionId");

		final CharSequence downloadOptionPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getDownloadOptionPart", executionData);

		Assertions.assertThat(downloadOptionPart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-download-with-options.txt")));
	}

	@Test
	public void testGetDownloadOptionPartRunning() throws Exception {

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of();

		final Map<String, ? extends Object> executionData = ImmutableMap.of("context", contextMap, "status", "running");

		final CharSequence downloadOptionPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getDownloadOptionPart", executionData);

		Assertions.assertThat(downloadOptionPart).isEmpty();
	}

	@Test
	public void testGetDurationPartEmptyExecutionData() throws Exception {

		final Map<String, Object> executionData = ImmutableMap.of();

		final CharSequence durationPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getDurationPart", executionData);

		Assertions.assertThat(durationPart).isEmpty();
	}

	@Test
	public void testGetDurationPartOnlyStartDate() throws Exception {

		final Map<String, ? extends Object> executionData = ImmutableMap.of("dateStartedUnixtime", Long.valueOf(1439471146429L), "status", "running");

		final CharSequence durationPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getDurationPart", executionData);

		Assertions.assertThat(durationPart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-duration-only-start-date.txt")));
	}

	@Test
	public void testGetDurationPartRunning() throws Exception {

		final Map<String, ? extends Object> executionData = ImmutableMap.of("dateStartedUnixtime", Long.valueOf(1439471146429L), "user", "launchUser", "status", "running");

		final CharSequence durationPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getDurationPart", executionData);

		Assertions.assertThat(durationPart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-duration-launched.txt")));
	}

	@Test
	public void testGetDurationPartAbortedByNone() throws Exception {

		final Map<String, ? extends Object> executionData = ImmutableMap.of("dateStartedUnixtime", Long.valueOf(1439471146429L), "user", "launchUser", "status", "aborted");

		final CharSequence durationPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getDurationPart", executionData);

		Assertions.assertThat(durationPart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-duration-aborted-bynone.txt")));
	}

	@Test
	public void testGetDurationPartAbortedByUser() throws Exception {

		final Map<String, ? extends Object> executionData = ImmutableMap.of("dateStartedUnixtime", Long.valueOf(1439471146429L), "user", "launchUser", "status", "failure", "abortedby", "abortUser");

		final CharSequence durationPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getDurationPart", executionData);

		Assertions.assertThat(durationPart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-duration-aborted.txt")));
	}

	@Test
	public void testGetDurationPartEnded() throws Exception {

		final Map<String, ? extends Object> executionData = ImmutableMap.of("dateStartedUnixtime", Long.valueOf(1439471146429L), "user", "launchUser", "status", "failure", "dateEndedUnixtime", Long.valueOf(1439471158125L));

		final CharSequence durationPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getDurationPart", executionData);

		Assertions.assertThat(durationPart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-duration-ended.txt")));
	}

	@Test
	public void testGetDurationPartTimedOut() throws Exception {

		final Map<String, ? extends Object> executionData = ImmutableMap.of("dateStartedUnixtime", Long.valueOf(1439471146429L), "user", "launchUser", "status", "timedout", "dateEndedUnixtime", Long.valueOf(1439471158125L));

		final CharSequence durationPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getDurationPart", executionData);

		Assertions.assertThat(durationPart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-duration-timedout.txt")));
	}

	@Test
	public void testGetTitlePartEmptyExecutionData() throws Exception {

		final Map<String, Object> executionData = ImmutableMap.of();

		final CharSequence titlePart = (CharSequence) callStaticMethod(SlackPlugin.class, "getTitlePart", executionData);

		Assertions.assertThat(titlePart).isEmpty();
	}

	@Test
	public void testGetTitlePartEmptyEmptyJob() throws Exception {

		final Map<String, String> jobMap = ImmutableMap.of();

		final Map<String, Map<String, String>> executionData = ImmutableMap.of("job", jobMap);

		final CharSequence titlePart = (CharSequence) callStaticMethod(SlackPlugin.class, "getTitlePart", executionData);

		Assertions.assertThat(titlePart).isEmpty();
	}

	@Test
	public void testGetTitlePartEmptyJobEmptyContext() throws Exception {

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of();
		final Map<String, String> jobMap = ImmutableMap.of();

		final Map<String, Map<String, ? extends Object>> executionData = ImmutableMap.of("context", contextMap, "job", jobMap);

		final CharSequence titlePart = (CharSequence) callStaticMethod(SlackPlugin.class, "getTitlePart", executionData);

		Assertions.assertThat(titlePart).isEmpty();
	}

	@Test
	public void testGetTitlePartEmptyJobEmptyContextEmptyJobContext() throws Exception {

		final Map<String, String> jobContextMap = ImmutableMap.of();
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap);
		final Map<String, String> jobMap = ImmutableMap.of();

		final Map<String, Map<String, ? extends Object>> executionData = ImmutableMap.of("context", contextMap, "job", jobMap);

		final CharSequence titlePart = (CharSequence) callStaticMethod(SlackPlugin.class, "getTitlePart", executionData);

		Assertions.assertThat(titlePart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-null-title.txt")));
	}

	@Test
	public void testGetTitlePartRunningWithoutGroup() throws Exception {

		final Map<String, String> jobContextMap = ImmutableMap.of("serverUrl", "http://serverurl:4440");
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap);
		final Map<String, String> jobMap = ImmutableMap.of("href", "http://jobnurl:4440", "name", "jobname");

		final Map<String, Object> executionDataMap = new HashMap<>();
		executionDataMap.put("context", contextMap);
		executionDataMap.put("job", jobMap);
		executionDataMap.put("href", "http://executionnurl:4440");
		executionDataMap.put("id", "idExecution66");
		executionDataMap.put("status", "running");
		executionDataMap.put("project", "projectName");
		final Map<String, Object> executionData = ImmutableMap.copyOf(executionDataMap);

		final CharSequence titlePart = (CharSequence) callStaticMethod(SlackPlugin.class, "getTitlePart", executionData);

		Assertions.assertThat(titlePart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-running-nogroup-title.txt")));
	}

	@Test
	public void testGetTitlePartAbortedByNoneWithGroup() throws Exception {

		final Map<String, String> jobContextMap = ImmutableMap.of("serverUrl", "http://serverurl:4440");
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap);
		final Map<String, String> jobMap = ImmutableMap.of("href", "http://jobnurl:4440", "name", "jobname", "group", "groupName/subGroupName/subSubGroupName");

		final Map<String, Object> executionDataMap = new HashMap<>();
		executionDataMap.put("context", contextMap);
		executionDataMap.put("job", jobMap);
		executionDataMap.put("href", "http://executionnurl:4440");
		executionDataMap.put("id", "idExecution66");
		executionDataMap.put("status", "aborted");
		executionDataMap.put("project", "projectName");
		final Map<String, Object> executionData = ImmutableMap.copyOf(executionDataMap);

		final CharSequence titlePart = (CharSequence) callStaticMethod(SlackPlugin.class, "getTitlePart", executionData);

		Assertions.assertThat(titlePart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-aborted-bynone-group-title.txt")));
	}

	@Test
	public void testGetTitlePartAbortedWithGroup() throws Exception {

		final Map<String, String> jobContextMap = ImmutableMap.of("serverUrl", "http://serverurl:4440");
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap);
		final Map<String, String> jobMap = ImmutableMap.of("href", "http://jobnurl:4440", "name", "jobname", "group", "groupName/subGroupName/subSubGroupName");

		final Map<String, Object> executionDataMap = new HashMap<>();
		executionDataMap.put("context", contextMap);
		executionDataMap.put("job", jobMap);
		executionDataMap.put("href", "http://executionnurl:4440");
		executionDataMap.put("id", "idExecution66");
		executionDataMap.put("status", "aborted");
		executionDataMap.put("project", "projectName");
		executionDataMap.put("abortedby", "adminUser");
		final Map<String, Object> executionData = ImmutableMap.copyOf(executionDataMap);

		final CharSequence titlePart = (CharSequence) callStaticMethod(SlackPlugin.class, "getTitlePart", executionData);

		Assertions.assertThat(titlePart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-aborted-group-title.txt")));
	}

	@Test
	public void testGetJobOptionsPartEmptyExecutionData() throws Exception {

		final Map<String, Object> executionData = ImmutableMap.of();

		final CharSequence jobOptionsPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getJobOptionsPart", executionData);

		Assertions.assertThat(jobOptionsPart).isEmpty();
	}

	@Test
	public void testGetJobOptionsPartEmptyContext() throws Exception {

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of();
		final Map<String, Map<String, Map<String, String>>> executionData = ImmutableMap.of("context", contextMap);

		final CharSequence jobOptionsPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getJobOptionsPart", executionData);

		Assertions.assertThat(jobOptionsPart).isEmpty();
	}

	@Test
	public void testGetJobOptionsPartOneOption() throws Exception {

		final Map<String, String> optionsMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE);
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("option", optionsMap);

		final Map<String, Map<String, Map<String, String>>> executionData = ImmutableMap.of("context", contextMap);

		final CharSequence jobOptionsPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getJobOptionsPart", executionData);

		Assertions.assertThat(jobOptionsPart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-1option-list.txt")));
	}

	@Test
	public void testGetJobOptionsPartTwoOption() throws Exception {

		final Map<String, String> optionsMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE, OPTION_2, OPTION_2_VALUE);
		final Map<String, String> secureOptionsMap = ImmutableMap.of();
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("option", optionsMap, "secureoption", secureOptionsMap);

		final Map<String, Map<String, Map<String, String>>> executionData = ImmutableMap.of("context", contextMap);

		final CharSequence jobOptionsPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getJobOptionsPart", executionData);

		Assertions.assertThat(jobOptionsPart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-2options-list.txt")));
	}

	@Test
	public void testGetJobOptionsPartOneSecureOption() throws Exception {

		final Map<String, String> optionsMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE, OPTION_2, OPTION_2_VALUE);
		final Map<String, String> secureOptionsMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE);

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("option", optionsMap, "secureOption", secureOptionsMap);

		final Map<String, Map<String, Map<String, String>>> executionData = ImmutableMap.of("context", contextMap);

		final CharSequence jobOptionsPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getJobOptionsPart", executionData);

		Assertions.assertThat(jobOptionsPart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-1secureoption-list.txt")));
	}

	@Test
	public void testGetJobOptionsPartTwoSecureOption() throws Exception {

		final Map<String, String> optionsMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE, OPTION_2, OPTION_2_VALUE);
		final Map<String, String> secureOptionsMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE, OPTION_2, OPTION_2_VALUE);

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("option", optionsMap, "secureOption", secureOptionsMap);

		final Map<String, Map<String, Map<String, String>>> executionData = ImmutableMap.of("context", contextMap);

		final CharSequence jobOptionsPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getJobOptionsPart", executionData);

		Assertions.assertThat(jobOptionsPart.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-2secureoptions-list.txt")));
	}

	@Test
	public void testGetFailedNodesAttachmentEmptyExecutionData() throws Exception {

		final Map<String, Object> executionData = ImmutableMap.of();

		final CharSequence failedNodesAttachment = (CharSequence) callStaticMethod(SlackPlugin.class, "getFailedNodesAttachment", executionData, SlackPlugin.SLACK_SUCCESS_COLOR);

		Assertions.assertThat(failedNodesAttachment).isEmpty();
	}

	@Test
	public void testGetFailedNodesAttachmentOneNodeOnly() throws Exception {

		final List<String> failedNodeList = ImmutableList.of(NODE_1);
		final Map<String, Integer> nodeStatus = ImmutableMap.of(TOTAL, Integer.valueOf(1));

		final Map<String, Object> executionData = ImmutableMap.of("failedNodeList", failedNodeList, "nodestatus", nodeStatus);

		final CharSequence failedNodesAttachment = (CharSequence) callStaticMethod(SlackPlugin.class, "getFailedNodesAttachment", executionData, SlackPlugin.SLACK_SUCCESS_COLOR);

		Assertions.assertThat(failedNodesAttachment).isEmpty();
	}

	@Test
	public void testGetFailedNodesAttachmentZeroFailedNode() throws Exception {

		final List<String> failedNodeList = ImmutableList.of();
		final Map<String, Integer> nodeStatus = ImmutableMap.of(TOTAL, Integer.valueOf(2));

		final Map<String, Object> executionData = ImmutableMap.of("failedNodeList", failedNodeList, "nodestatus", nodeStatus);

		final CharSequence failedNodesAttachment = (CharSequence) callStaticMethod(SlackPlugin.class, "getFailedNodesAttachment", executionData, SlackPlugin.SLACK_SUCCESS_COLOR);

		Assertions.assertThat(failedNodesAttachment).isEmpty();
	}

	@Test
	public void testGetFailedNodesAttachmentTwoFailedNode() throws Exception {

		final List<String> failedNodeList = ImmutableList.of(NODE_1, NODE_2);
		final Map<String, Integer> nodeStatus = ImmutableMap.of(TOTAL, Integer.valueOf(2));

		final Map<String, Object> executionData = ImmutableMap.of("failedNodeList", failedNodeList, "nodestatus", nodeStatus);

		final CharSequence failedNodesAttachment = (CharSequence) callStaticMethod(SlackPlugin.class, "getFailedNodesAttachment", executionData, SlackPlugin.SLACK_SUCCESS_COLOR);

		Assertions.assertThat(failedNodesAttachment.toString()).isEqualTo(Assertions.contentOf(getClass().getClassLoader().getResource("expected-2failed-nodes-list.txt")));
	}

	@Test
	public void testFormatDuration() {

		Assertions.assertThat(SlackPlugin.formatDuration(0l)).isEqualTo("0s");
		Assertions.assertThat(SlackPlugin.formatDuration(999l)).isEqualTo("0s");

		for (long seconds = 1; seconds < 60; seconds++) {
			Assertions.assertThat(SlackPlugin.formatDuration(seconds * 1_000l)).isEqualTo(seconds + "s");
			Assertions.assertThat(SlackPlugin.formatDuration(seconds * 1_000l + 999l)).isEqualTo(seconds + "s");
		}

		for (long minutes = 1; minutes < 60; minutes++) {
			Assertions.assertThat(SlackPlugin.formatDuration(minutes * 60_000l)).isEqualTo(minutes + "m00s");
			Assertions.assertThat(SlackPlugin.formatDuration(minutes * 60_000l + 59_999l)).isEqualTo(minutes + "m59s");
		}

		for (long hours = 1; hours < 24; hours++) {
			Assertions.assertThat(SlackPlugin.formatDuration(hours * 3_600_000l)).isEqualTo(hours + "h00m");
			Assertions.assertThat(SlackPlugin.formatDuration(hours * 3_600_000l + 3_599_999l)).isEqualTo(hours + "h59m");
		}

		for (long days = 1; days < 100; days++) {
			Assertions.assertThat(SlackPlugin.formatDuration(days * 86_400_000l)).isEqualTo(days + "d00h");
			Assertions.assertThat(SlackPlugin.formatDuration(days * 86_400_000l + 86_399_999l)).isEqualTo(days + "d23h");
		}
	}

	public static Object callMethod(final Object object, final String methodName, final Object... methodArguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (object == null || methodName == null) {
			return null;
		}

		final Class<?> baseClass = object.getClass();

		final Method targetMethod = getMethod(baseClass, methodName, methodArguments);
		if (!targetMethod.isAccessible()) {
			targetMethod.setAccessible(true);
		}
		return targetMethod.invoke(object, methodArguments);
	}

	public static Object callStaticMethod(final Class<?> baseClass, final String methodName, final Object... methodArguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (baseClass == null || methodName == null) {
			return null;
		}

		final Method targetMethod = getMethod(baseClass, methodName, methodArguments);
		if (!targetMethod.isAccessible()) {
			targetMethod.setAccessible(true);
		}
		return targetMethod.invoke(null, methodArguments);
	}

	public static Method getMethod(final Class<?> baseClass, final String methodName, final Object... methodArguments) throws NoSuchMethodException, SecurityException {

		if (null == methodArguments) {
			try {
				return baseClass.getDeclaredMethod(methodName);
			} catch (final NoSuchMethodException e) {
				return baseClass.getMethod(methodName);
			}
		}

		final Method[] declaredMethods = baseClass.getDeclaredMethods();
		int methodNum = 0;
		while (methodNum < declaredMethods.length) {

			final Method currentMethod = declaredMethods[methodNum];
			if (methodName.equals(currentMethod.getName())) {

				final Class<?>[] argumentTypes = currentMethod.getParameterTypes();
				if (argumentTypes != null && argumentTypes.length == methodArguments.length) {

					int argumentNum = 0;
					while (argumentNum < argumentTypes.length && argumentTypes[argumentNum].isAssignableFrom(methodArguments[argumentNum].getClass())) {

						argumentNum++;
					}

					if (argumentNum >= argumentTypes.length) {
						return currentMethod;
					}
				}
			}

			methodNum++;
		}

		final Method[] methods = baseClass.getMethods();
		methodNum = 0;
		while (methodNum < methods.length) {

			final Method currentMethod = methods[methodNum];
			if (methodName.equals(currentMethod.getName())) {

				final Class<?>[] argumentTypes = currentMethod.getParameterTypes();
				if (argumentTypes != null && argumentTypes.length == methodArguments.length) {

					int argumentNum = 0;
					while (argumentNum < argumentTypes.length && argumentTypes[argumentNum].isAssignableFrom(methodArguments[argumentNum].getClass())) {

						argumentNum++;
					}

					if (argumentNum >= argumentTypes.length) {
						return currentMethod;
					}
				}
			}

			methodNum++;
		}

		throw new NoSuchMethodException(methodName);
	}
}
