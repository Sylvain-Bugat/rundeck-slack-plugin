package com.github.sbugat.rundeck.plugins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Rundeck slack plugin test class.
 *
 * @author Sylvain Bugat
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SlackPluginTest {

	private static final String NODE_1 = "1node1";
	private static final String NODE_2 = "2node2";

	private static final String OPTION_1 = "option1";
	private static final String OPTION_1_VALUE = "value1";
	private static final String OPTION_2 = "option2";
	private static final String OPTION_2_VALUE = "value2";

	private static final String TOTAL = "total";

	@Mock
	private URLTools uRLTools;
	
	@Mock
	private HttpURLConnection connection;
	
	@InjectMocks
	private SlackPlugin slackPlugin;
	
	@After
	public void cleanUp() {
		Mockito.verifyNoMoreInteractions(uRLTools, connection);
	}
	
	@Test
	public void testPostNotificationOK() throws Exception {

		Mockito.doReturn(connection).when(uRLTools).openURLConnection(null);
		
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Mockito.doReturn(byteArrayOutputStream).when(connection).getOutputStream();
		Mockito.doReturn(HttpURLConnection.HTTP_OK).when(connection).getResponseCode();
		
		final Map<String, String> jobMap = ImmutableMap.of("name", "jobname");
		final Map<String, Object> executionDataMap = new HashMap<String, Object>();
		executionDataMap.put("job", jobMap);
		final Map<String, Object> executionData = ImmutableMap.copyOf(executionDataMap);
		
		Assertions.assertThat( slackPlugin.postNotification("success", executionData, null) ).isTrue();
		
		Assertions.assertThat(byteArrayOutputStream.toString(SlackPlugin.UTF_8)).isEqualTo(getFileContent("expected-notification-ok.txt"));
		
		//Verify URLTools call
		Mockito.verify(uRLTools).openURLConnection(null);
		
		//Verify connection calls
		Mockito.verify(connection).getOutputStream();
		Mockito.verify(connection).setRequestMethod("POST");
		Mockito.verify(connection).setRequestProperty("charset", SlackPlugin.UTF_8);
		Mockito.verify(connection).setUseCaches(false);
		Mockito.verify(connection).setDoInput(true);
		Mockito.verify(connection).setDoOutput(true);
		Mockito.verify(connection).getResponseCode();
		Mockito.verify(connection).disconnect();
	}
	
	@Test
	public void testPostNotificationMalformedURLException() throws Exception {

		Mockito.doThrow(new MalformedURLException()).when(uRLTools).openURLConnection(null);
		
		final Map<String, Object> executionData = ImmutableMap.of();
		
		Assertions.assertThat( slackPlugin.postNotification("success", executionData, null) ).isFalse();
		
		//Verify URLTools call
		Mockito.verify(uRLTools).openURLConnection(null);
	}
	
	@Test
	public void testPostNotificationIOException() throws Exception {

		Mockito.doThrow(new IOException()).when(uRLTools).openURLConnection(null);
		
		final Map<String, Object> executionData = ImmutableMap.of();
		
		Assertions.assertThat( slackPlugin.postNotification("success", executionData, null) ).isFalse();
		
		//Verify URLTools call
		Mockito.verify(uRLTools).openURLConnection(null);
	}
	
	@Test
	public void testPostNotificationNotFound404() throws Exception {

		Mockito.doReturn(connection).when(uRLTools).openURLConnection(null);
		
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Mockito.doReturn(byteArrayOutputStream).when(connection).getOutputStream();
		Mockito.doReturn(HttpURLConnection.HTTP_NOT_FOUND).when(connection).getResponseCode();
		
		final Map<String, String> jobMap = ImmutableMap.of("name", "jobname");
		final Map<String, Object> executionDataMap = new HashMap<String, Object>();
		executionDataMap.put("job", jobMap);
		final Map<String, Object> executionData = ImmutableMap.copyOf(executionDataMap);
		
		Assertions.assertThat( slackPlugin.postNotification("success", executionData, null) ).isFalse();
		
		Assertions.assertThat(byteArrayOutputStream.toString(SlackPlugin.UTF_8)).isEqualTo(getFileContent("expected-notification-ok.txt"));
		
		//Verify URLTools call
		Mockito.verify(uRLTools).openURLConnection(null);
		
		//Verify connection calls
		Mockito.verify(connection).getOutputStream();
		Mockito.verify(connection).setRequestMethod("POST");
		Mockito.verify(connection).setRequestProperty("charset", SlackPlugin.UTF_8);
		Mockito.verify(connection).setUseCaches(false);
		Mockito.verify(connection).setDoInput(true);
		Mockito.verify(connection).setDoOutput(true);
		Mockito.verify(connection).getResponseCode();
		Mockito.verify(connection).disconnect();
	}
	
	@Test
	public void testPostNotificationInternalError500() throws Exception {

		Mockito.doReturn(connection).when(uRLTools).openURLConnection(null);
		
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Mockito.doReturn(byteArrayOutputStream).when(connection).getOutputStream();
		Mockito.doReturn(HttpURLConnection.HTTP_INTERNAL_ERROR).when(connection).getResponseCode();
		
		final Map<String, String> jobMap = ImmutableMap.of("name", "jobname");
		final Map<String, Object> executionDataMap = new HashMap<String, Object>();
		executionDataMap.put("job", jobMap);
		final Map<String, Object> executionData = ImmutableMap.copyOf(executionDataMap);
		
		Assertions.assertThat( slackPlugin.postNotification("success", executionData, null) ).isFalse();
		
		Assertions.assertThat(byteArrayOutputStream.toString(SlackPlugin.UTF_8)).isEqualTo(getFileContent("expected-notification-ok.txt"));
		
		//Verify URLTools call
		Mockito.verify(uRLTools).openURLConnection(null);
		
		//Verify connection calls
		Mockito.verify(connection).getOutputStream();
		Mockito.verify(connection).setRequestMethod("POST");
		Mockito.verify(connection).setRequestProperty("charset", SlackPlugin.UTF_8);
		Mockito.verify(connection).setUseCaches(false);
		Mockito.verify(connection).setDoInput(true);
		Mockito.verify(connection).setDoOutput(true);
		Mockito.verify(connection).getResponseCode();
		Mockito.verify(connection).disconnect();
	}
	
	@Test
	public void testGetMessageStart() throws Exception {

		final Map<String, Object> executionData = ImmutableMap.of();
		
		final String message = (String) callMethod(slackPlugin, "getMessage", "start", executionData );
		Assertions.assertThat(message).isEqualTo(getFileContent("expected-message-start.txt"));
	}
	
	@Test
	public void testGetMessageFailure() throws Exception {

		final Map<String, Object> executionData = ImmutableMap.of();
		
		final String message = (String) callMethod(slackPlugin, "getMessage", "failure", executionData );
		Assertions.assertThat(message).isEqualTo(getFileContent("expected-message-failure.txt"));
	}
	
	@Test
	public void testGetMessageComplete() throws Exception {

		setField(slackPlugin, "slackOverrideDefaultWebHookChannel", "#general");
		setField(slackPlugin, "slackOverrideDefaultWebHookName", "Rundeck");
		setField(slackPlugin, "slackOverrideDefaultWebHookEmoji", ":beer:");
		
		final Map<String, String> optionContextMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE);
		final Map<String, String> jobContextMap = ImmutableMap.of("serverUrl", "http://serverurl:4440");
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap, "option", optionContextMap);
			
		final Map<String, String> jobMap = ImmutableMap.of("href", "http://jobnurl:4440", "name", "jobname", "group", "groupName/subGroupName/subSubGroupName");

		final List<String> failedNodeList = ImmutableList.of(NODE_1, NODE_2);
		final Map<String, Integer> nodeStatus = ImmutableMap.of(TOTAL, Integer.valueOf(2));
		
		final Map<String, Object> executionDataMap = new HashMap<String, Object>();
		executionDataMap.put("context", contextMap);
		executionDataMap.put("job", jobMap);
		executionDataMap.put("href", "http://executionnurl:4440");
		executionDataMap.put("id", "idExecution66");
		executionDataMap.put("status", "aborted");
		executionDataMap.put("name", "jobname");
		executionDataMap.put("group", "groupName/subGroupName/subSubGroupName");
		executionDataMap.put("project", "projectName");
		executionDataMap.put("abortedby", "adminUser");
		executionDataMap.put("user", "launchUser");
		executionDataMap.put("dateStartedUnixtime", Long.valueOf(1439471146429L));
		executionDataMap.put("dateEndedUnixtime", Long.valueOf(1439471158125L));
		executionDataMap.put("failedNodeList", failedNodeList);
		executionDataMap.put("nodestatus", nodeStatus);
		final Map<String, Object> executionData = ImmutableMap.copyOf(executionDataMap);
		
		final String message = (String) callMethod(slackPlugin, "getMessage", "failure", executionData );
		Assertions.assertThat(message).isEqualTo(getFileContent("expected-message-complete.txt"));
	}
	
	@Test
	public void testGetOptionsEmpty() throws Exception {

		final StringBuilder optionsPart = new StringBuilder();
		callMethod(slackPlugin, "getOptions", optionsPart);

		Assertions.assertThat(optionsPart).isEmpty();
	}
	
	@Test
	public void testGetOptionsChannel() throws Exception {

		setField(slackPlugin, "slackOverrideDefaultWebHookChannel", "#newchannel");
		
		final StringBuilder optionsPart = new StringBuilder();
		callMethod(slackPlugin, "getOptions", optionsPart);

		Assertions.assertThat(optionsPart.toString()).isEqualTo(getFileContent("expected-option-channel.txt"));
	}
	
	@Test
	public void testGetOptionsEmptyChannel() throws Exception {

		setField(slackPlugin, "slackOverrideDefaultWebHookChannel", "");
		
		final StringBuilder optionsPart = new StringBuilder();
		callMethod(slackPlugin, "getOptions", optionsPart);

		Assertions.assertThat(optionsPart).isEmpty();
	}
	
	@Test
	public void testGetOptionsWebHookName() throws Exception {

		setField(slackPlugin, "slackOverrideDefaultWebHookName", "HAL");
		
		final StringBuilder optionsPart = new StringBuilder();
		callMethod(slackPlugin, "getOptions", optionsPart);

		Assertions.assertThat(optionsPart.toString()).isEqualTo(getFileContent("expected-option-name.txt"));
	}
	
	@Test
	public void testGetOptionsEmptyWebHookName() throws Exception {

		setField(slackPlugin, "slackOverrideDefaultWebHookName", "");
		
		final StringBuilder optionsPart = new StringBuilder();
		callMethod(slackPlugin, "getOptions", optionsPart);

		Assertions.assertThat(optionsPart).isEmpty();
	}
	
	@Test
	public void testGetOptionsEmoji() throws Exception {

		setField(slackPlugin, "slackOverrideDefaultWebHookEmoji", ":cow:");
		
		final StringBuilder optionsPart = new StringBuilder();
		callMethod(slackPlugin, "getOptions", optionsPart);

		Assertions.assertThat(optionsPart.toString()).isEqualTo(getFileContent("expected-option-emoji.txt"));
	}
	
	@Test
	public void testGetOptionsEmptyEmoji() throws Exception {

		setField(slackPlugin, "slackOverrideDefaultWebHookEmoji", "");
		
		final StringBuilder optionsPart = new StringBuilder();
		callMethod(slackPlugin, "getOptions", optionsPart);

		Assertions.assertThat(optionsPart).isEmpty();
	}
	
	@Test
	public void testGetOptionsAll() throws Exception {

		setField(slackPlugin, "slackOverrideDefaultWebHookChannel", "#general");
		setField(slackPlugin, "slackOverrideDefaultWebHookName", "Rundeck");
		setField(slackPlugin, "slackOverrideDefaultWebHookEmoji", ":beer:");
		
		final StringBuilder optionsPart = new StringBuilder();
		callMethod(slackPlugin, "getOptions", optionsPart);

		Assertions.assertThat(optionsPart.toString()).isEqualTo(getFileContent("expected-option-all.txt"));
	}

	@Test
	public void testGetDownloadOptionPartEmptyExecutionData() throws Exception {

		final Map<String, Object> executionData = ImmutableMap.of();

		final StringBuilder downloadOptionPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDownloadOptionPart", downloadOptionPart, executionData);

		Assertions.assertThat(downloadOptionPart).isEmpty();
	}

	@Test
	public void testGetDownloadOptionPartSuccess() throws Exception {

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of();

		final Map<String, ? extends Object> executionData = ImmutableMap.of("context", contextMap, "status", "success");

		final StringBuilder downloadOptionPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDownloadOptionPart", downloadOptionPart, executionData);

		Assertions.assertThat(downloadOptionPart).isEmpty();
	}

	@Test
	public void testGetDownloadOptionPartFailure() throws Exception {

		final Map<String, String> optionContextMap = ImmutableMap.of();
		final Map<String, String> jobContextMap = ImmutableMap.of("serverUrl", "http://serverurl:4440");

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap, "option", optionContextMap);

		final Map<String, ? extends Object> executionData = ImmutableMap.of("context", contextMap, "status", "failure", "project", "projectName", "id", "executionId");

		final StringBuilder downloadOptionPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDownloadOptionPart", downloadOptionPart, executionData);

		Assertions.assertThat(downloadOptionPart.toString()).isEqualTo(getFileContent("expected-download-failure.txt"));
	}

	@Test
	public void testGetDownloadOptionPartAbortedWithOptions() throws Exception {

		final Map<String, String> optionContextMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE);
		final Map<String, String> jobContextMap = ImmutableMap.of("serverUrl", "http://serverurl:4440");
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap, "option", optionContextMap);

		final Map<String, ? extends Object> executionData = ImmutableMap.of("context", contextMap, "status", "aborted", "project", "projectName", "id", "executionId");

		final StringBuilder downloadOptionPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDownloadOptionPart", downloadOptionPart, executionData);

		Assertions.assertThat(downloadOptionPart.toString()).isEqualTo(getFileContent("expected-download-with-options.txt"));
	}

	@Test
	public void testGetDownloadOptionPartRunning() throws Exception {

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of();

		final Map<String, ? extends Object> executionData = ImmutableMap.of("context", contextMap, "status", "running");

		final StringBuilder downloadOptionPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDownloadOptionPart", downloadOptionPart, executionData);

		Assertions.assertThat(downloadOptionPart).isEmpty();
	}
	
	@Test
	public void testGetDownloadOptionPartRunningWithOptions() throws Exception {

		final Map<String, String> optionContextMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE);
		final Map<String, String> jobContextMap = ImmutableMap.of("serverUrl", "http://serverurl:4440");
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap, "option", optionContextMap);

		final Map<String, ? extends Object> executionData = ImmutableMap.of("context", contextMap, "status", "running", "project", "projectName", "id", "executionId");

		final StringBuilder downloadOptionPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDownloadOptionPart", downloadOptionPart, executionData);

		Assertions.assertThat(downloadOptionPart.toString()).isEqualTo(getFileContent("expected-download-running-with-options.txt"));
	}

	@Test
	public void testGetDurationPartEmptyExecutionData() throws Exception {

		final Map<String, Object> executionData = ImmutableMap.of();

		final StringBuilder durationPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDurationPart", durationPart, executionData);

		Assertions.assertThat(durationPart).isEmpty();
	}

	@Test
	public void testGetDurationPartOnlyStartDate() throws Exception {

		final Map<String, ? extends Object> executionData = ImmutableMap.of("dateStartedUnixtime", Long.valueOf(1439471146429L), "status", "running");

		final StringBuilder durationPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDurationPart", durationPart, executionData);

		Assertions.assertThat(durationPart.toString()).isEqualTo(getFileContent("expected-duration-only-start-date.txt"));
	}

	@Test
	public void testGetDurationPartRunning() throws Exception {

		final Map<String, ? extends Object> executionData = ImmutableMap.of("dateStartedUnixtime", Long.valueOf(1439471146429L), "user", "launchUser", "status", "running");

		final StringBuilder durationPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDurationPart", durationPart, executionData);

		Assertions.assertThat(durationPart.toString()).isEqualTo(getFileContent("expected-duration-launched.txt"));
	}

	@Test
	public void testGetDurationPartAbortedByNone() throws Exception {

		final Map<String, ? extends Object> executionData = ImmutableMap.of("dateStartedUnixtime", Long.valueOf(1439471146429L), "user", "launchUser", "status", "aborted");

		final StringBuilder durationPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDurationPart", durationPart, executionData);

		Assertions.assertThat(durationPart.toString()).isEqualTo(getFileContent("expected-duration-aborted-bynone.txt"));
	}

	@Test
	public void testGetDurationPartAbortedByUser() throws Exception {

		final Map<String, ? extends Object> executionData = ImmutableMap.of("dateStartedUnixtime", Long.valueOf(1439471146429L), "user", "launchUser", "status", "failure", "abortedby", "abortUser");

		final StringBuilder durationPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDurationPart", durationPart, executionData);

		Assertions.assertThat(durationPart.toString()).isEqualTo(getFileContent("expected-duration-aborted.txt"));
	}

	@Test
	public void testGetDurationPartEnded() throws Exception {

		final Map<String, ? extends Object> executionData = ImmutableMap.of("dateStartedUnixtime", Long.valueOf(1439471146429L), "user", "launchUser", "status", "failure", "dateEndedUnixtime", Long.valueOf(1439471158125L));

		final StringBuilder durationPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDurationPart", durationPart, executionData);

		Assertions.assertThat(durationPart.toString()).isEqualTo(getFileContent("expected-duration-ended.txt"));
	}

	@Test
	public void testGetDurationPartTimedOut() throws Exception {

		final Map<String, ? extends Object> executionData = ImmutableMap.of("dateStartedUnixtime", Long.valueOf(1439471146429L), "user", "launchUser", "status", "timedout", "dateEndedUnixtime", Long.valueOf(1439471158125L));

		final StringBuilder durationPart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getDurationPart", durationPart, executionData);

		Assertions.assertThat(durationPart.toString()).isEqualTo(getFileContent("expected-duration-timedout.txt"));
	}

	@Test
	public void testGetTitlePartEmptyExecutionData() throws Exception {

		final Map<String, Object> executionData = ImmutableMap.of();

		final StringBuilder titlePart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getTitlePart", titlePart, executionData);

		Assertions.assertThat(titlePart).isEmpty();
	}

	@Test
	public void testGetTitlePartEmptyEmptyJob() throws Exception {

		final Map<String, String> jobMap = ImmutableMap.of();

		final Map<String, Map<String, String>> executionData = ImmutableMap.of("job", jobMap);

		final StringBuilder titlePart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getTitlePart", titlePart, executionData);

		Assertions.assertThat(titlePart).isEmpty();
	}

	@Test
	public void testGetTitlePartEmptyJobEmptyContext() throws Exception {

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of();
		final Map<String, String> jobMap = ImmutableMap.of();

		final Map<String, Map<String, ? extends Object>> executionData = ImmutableMap.of("context", contextMap, "job", jobMap);

		final StringBuilder titlePart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getTitlePart", titlePart, executionData);

		Assertions.assertThat(titlePart).isEmpty();
	}

	@Test
	public void testGetTitlePartEmptyJobEmptyContextEmptyJobContext() throws Exception {

		final Map<String, String> jobContextMap = ImmutableMap.of();
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap);
		final Map<String, String> jobMap = ImmutableMap.of();

		final Map<String, Map<String, ? extends Object>> executionData = ImmutableMap.of("context", contextMap, "job", jobMap);

		final StringBuilder titlePart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getTitlePart", titlePart, executionData);

		Assertions.assertThat(titlePart.toString()).isEqualTo(getFileContent("expected-null-title.txt"));
	}

	@Test
	public void testGetTitlePartRunningWithoutGroup() throws Exception {

		final Map<String, String> jobContextMap = ImmutableMap.of("serverUrl", "http://serverurl:4440");
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap);
		final Map<String, String> jobMap = ImmutableMap.of("href", "http://jobnurl:4440", "name", "jobname");

		final Map<String, Object> executionDataMap = new HashMap<String, Object>();
		executionDataMap.put("context", contextMap);
		executionDataMap.put("job", jobMap);
		executionDataMap.put("href", "http://executionnurl:4440");
		executionDataMap.put("id", "idExecution66");
		executionDataMap.put("status", "running");
		executionDataMap.put("project", "projectName");
		final Map<String, Object> executionData = ImmutableMap.copyOf(executionDataMap);

		final StringBuilder titlePart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getTitlePart", titlePart, executionData);

		Assertions.assertThat(titlePart.toString()).isEqualTo(getFileContent("expected-running-nogroup-title.txt"));
	}

	@Test
	public void testGetTitlePartAbortedByNoneWithGroup() throws Exception {

		final Map<String, String> jobContextMap = ImmutableMap.of("serverUrl", "http://serverurl:4440");
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap);
		final Map<String, String> jobMap = ImmutableMap.of("href", "http://jobnurl:4440", "name", "jobname", "group", "groupName/subGroupName/subSubGroupName");

		final Map<String, Object> executionDataMap = new HashMap<String, Object>();
		executionDataMap.put("context", contextMap);
		executionDataMap.put("job", jobMap);
		executionDataMap.put("href", "http://executionnurl:4440");
		executionDataMap.put("id", "idExecution66");
		executionDataMap.put("status", "aborted");
		executionDataMap.put("project", "projectName");
		final Map<String, Object> executionData = ImmutableMap.copyOf(executionDataMap);

		final StringBuilder titlePart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getTitlePart", titlePart, executionData);

		Assertions.assertThat(titlePart.toString()).isEqualTo(getFileContent("expected-aborted-bynone-group-title.txt"));
	}

	@Test
	public void testGetTitlePartAbortedWithGroup() throws Exception {

		final Map<String, String> jobContextMap = ImmutableMap.of("serverUrl", "http://serverurl:4440");
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("job", jobContextMap);
		final Map<String, String> jobMap = ImmutableMap.of("href", "http://jobnurl:4440", "name", "jobname", "group", "groupName/subGroupName/subSubGroupName");

		final Map<String, Object> executionDataMap = new HashMap<String, Object>();
		executionDataMap.put("context", contextMap);
		executionDataMap.put("job", jobMap);
		executionDataMap.put("href", "http://executionnurl:4440");
		executionDataMap.put("id", "idExecution66");
		executionDataMap.put("status", "aborted");
		executionDataMap.put("project", "projectName");
		executionDataMap.put("abortedby", "adminUser");
		final Map<String, Object> executionData = ImmutableMap.copyOf(executionDataMap);

		final StringBuilder titlePart = new StringBuilder();
		callStaticMethod(SlackPlugin.class, "getTitlePart", titlePart, executionData);

		Assertions.assertThat(titlePart.toString()).isEqualTo(getFileContent("expected-aborted-group-title.txt"));
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

		Assertions.assertThat(jobOptionsPart.toString()).isEqualTo(getFileContent("expected-1option-list.txt"));
	}

	@Test
	public void testGetJobOptionsPartTwoOption() throws Exception {

		final Map<String, String> optionsMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE, OPTION_2, OPTION_2_VALUE);
		final Map<String, String> secureOptionsMap = ImmutableMap.of();
		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("option", optionsMap, "secureoption", secureOptionsMap);

		final Map<String, Map<String, Map<String, String>>> executionData = ImmutableMap.of("context", contextMap);

		final CharSequence jobOptionsPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getJobOptionsPart", executionData);

		Assertions.assertThat(jobOptionsPart.toString()).isEqualTo(getFileContent("expected-2options-list.txt"));
	}

	@Test
	public void testGetJobOptionsPartOneSecureOption() throws Exception {

		final Map<String, String> optionsMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE, OPTION_2, OPTION_2_VALUE);
		final Map<String, String> secureOptionsMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE);

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("option", optionsMap, "secureOption", secureOptionsMap);

		final Map<String, Map<String, Map<String, String>>> executionData = ImmutableMap.of("context", contextMap);

		final CharSequence jobOptionsPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getJobOptionsPart", executionData);

		Assertions.assertThat(jobOptionsPart.toString()).isEqualTo(getFileContent("expected-1secureoption-list.txt"));
	}

	@Test
	public void testGetJobOptionsPartTwoSecureOption() throws Exception {

		final Map<String, String> optionsMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE, OPTION_2, OPTION_2_VALUE);
		final Map<String, String> secureOptionsMap = ImmutableMap.of(OPTION_1, OPTION_1_VALUE, OPTION_2, OPTION_2_VALUE);

		final Map<String, Map<String, String>> contextMap = ImmutableMap.of("option", optionsMap, "secureOption", secureOptionsMap);

		final Map<String, Map<String, Map<String, String>>> executionData = ImmutableMap.of("context", contextMap);

		final CharSequence jobOptionsPart = (CharSequence) callStaticMethod(SlackPlugin.class, "getJobOptionsPart", executionData);

		Assertions.assertThat(jobOptionsPart.toString()).isEqualTo(getFileContent("expected-2secureoptions-list.txt"));
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

		Assertions.assertThat(failedNodesAttachment.toString()).isEqualTo(getFileContent("expected-2failed-nodes-list.txt"));
	}

	@Test
	public void testFormatDuration() {

		Assertions.assertThat(SlackPlugin.formatDuration(0L)).isEqualTo("0s");
		Assertions.assertThat(SlackPlugin.formatDuration(999L)).isEqualTo("0s");

		for (long seconds = 1L; seconds < 60L; seconds++) {
			Assertions.assertThat(SlackPlugin.formatDuration(seconds * 1000L)).isEqualTo(seconds + "s");
			Assertions.assertThat(SlackPlugin.formatDuration(seconds * 1000L + 999L)).isEqualTo(seconds + "s");
		}

		for (long minutes = 1L; minutes < 60L; minutes++) {
			Assertions.assertThat(SlackPlugin.formatDuration(minutes * 60000L)).isEqualTo(minutes + "m00s");
			Assertions.assertThat(SlackPlugin.formatDuration(minutes * 60000L + 59999L)).isEqualTo(minutes + "m59s");
		}

		for (long hours = 1L; hours < 24L; hours++) {
			Assertions.assertThat(SlackPlugin.formatDuration(hours * 3600000L)).isEqualTo(hours + "h00m");
			Assertions.assertThat(SlackPlugin.formatDuration(hours * 3600000L + 3599999L)).isEqualTo(hours + "h59m");
		}

		for (long days = 1L; days < 100L; days++) {
			Assertions.assertThat(SlackPlugin.formatDuration(days * 86400000L)).isEqualTo(days + "d00h");
			Assertions.assertThat(SlackPlugin.formatDuration(days * 86400000L + 86399999L)).isEqualTo(days + "d23h");
		}
	}
	
	
	public static void setField( final Object object, final String fieldName, final Object fieldValue ) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		if (object == null || fieldName == null) {
			return;
		}
		
		final Class<?> baseClass = object.getClass();
		final Field field = baseClass.getDeclaredField(fieldName);
		if( !field.isAccessible() ) {
			field.setAccessible( true);
		}
		field.set(object, fieldValue);
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
	
	private String getFileContent( final String fileName ) throws IOException {
	        
		final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
		if( null == inputStream ) {
			return "";
		}
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(inputStream).useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		}
		finally {
			if( null != scanner ) {
				scanner.close();
			}
			inputStream.close();
		}
	}
}
