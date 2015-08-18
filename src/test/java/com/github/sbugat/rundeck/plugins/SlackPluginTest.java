package com.github.sbugat.rundeck.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

	private static final String TOTAL = "total";

	@Test
	public void testGetFailedNodesAttachmentEmptyExecutionData() throws Exception {

		final Map<String, Object> executionData = ImmutableMap.of();

		final CharSequence failedNodesAttachment = (CharSequence) callStaticMethod(SlackPlugin.class, "getFailedNodesAttachment", executionData, SlackPlugin.SLACK_SUCCESS_COLOR);

		Assertions.assertThat(failedNodesAttachment).isEmpty();
	}

	@Test
	public void testGetFailedNodesAttachmentEmptyFailedNodeList() throws Exception {

		final Map<String, Object> executionData = ImmutableMap.of("failedNodeList", (Object) ImmutableList.of(""), "nodestatus", (Object) ImmutableMap.of("aaa", ""));

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
