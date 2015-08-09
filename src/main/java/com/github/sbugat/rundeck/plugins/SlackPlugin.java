package com.github.sbugat.rundeck.plugins;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;

/**
 * Rundeck slack plugin class.
 *
 * @author Sylvain Bugat
 *
 */
@Plugin(service = ServiceNameConstants.Notification, name = "SlackNotification")
@PluginDescription(title = "Slack")
public class SlackPlugin implements NotificationPlugin {

	private static final String SLACK_SUCCESS_COLOR = "good";
	private static final String SLACK_FAILED_COLOR = "danger";

	@PluginProperty(title = "Incoming WebHook URL", description = "Slack incoming WebHook URL", required = true)
	private String slackIncomingWebHookUrl;

	@PluginProperty(title = "WebHook channel", description = "Override default WebHook channel")
	private String slackOverrideDefaultWebHookChannel;

	@PluginProperty(title = "WebHook name", description = "Override default WebHook name")
	private String slackOverrideDefaultWebHookName;

	@PluginProperty(title = "WebHook emoji", description = "Override default WebHook icon (emoji)")
	private String slackOverrideDefaultWebHookEmoji;

	@Override
	public boolean postNotification(final String trigger, @SuppressWarnings("rawtypes") final Map executionData, @SuppressWarnings("rawtypes") final Map config) {

		System.out.println(trigger);
		System.out.println(slackIncomingWebHookUrl);
		for (final Object entry : executionData.keySet()) {
			if (null != executionData.get(entry)) {
				System.out.println(entry + " -> " + executionData.get(entry) + executionData.get(entry).getClass().getName());
			} else {
				System.out.println(entry + " -> " + executionData.get(entry));
			}
		}
		System.out.println("");
		for (final Object entry : config.keySet()) {
			if (null != config.get(entry)) {
				System.out.println(entry + " -> " + config.get(entry) + config.get(entry).getClass().getName());
			} else {
				System.out.println(entry + " -> " + config.get(entry));
			}
		}

		try {
			final HttpURLConnection connection = (HttpURLConnection) new URL(slackIncomingWebHookUrl).openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("charset", StandardCharsets.UTF_8.name());
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			final DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes("payload=" + URLEncoder.encode(getMessage(trigger, executionData, config), StandardCharsets.UTF_8.name()));
			wr.close();

			System.out.println(connection.getResponseCode());
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public String getSlackIncomingWebHookUrl() {
		return slackIncomingWebHookUrl;
	}

	public void setSlackIncomingWebHookUrl(final String slackIncomingWebHookUrl) {
		this.slackIncomingWebHookUrl = slackIncomingWebHookUrl;
	}

	private String getMessage(final String trigger, @SuppressWarnings("rawtypes") final Map executionData, @SuppressWarnings("rawtypes") final Map config) {

		final String statusColor;
		if ("success" == trigger) {
			statusColor = SLACK_SUCCESS_COLOR;
		} else {
			statusColor = SLACK_FAILED_COLOR;
		}

		@SuppressWarnings("unchecked")
		final Map<String, String> jobMap = (Map<String, String>) executionData.get("job");

		@SuppressWarnings("unchecked")
		final Map<String, Map<String, String>> contextMap = (Map<String, Map<String, String>>) executionData.get("context");
		final Map<String, String> jobContextMap = contextMap.get("job");

		final String jobStatus;
		final String endStatus;
		if ("aborted" == executionData.get("status") && null != executionData.get("abortedby")) {
			jobStatus = ((String) executionData.get("status")).toUpperCase() + " by " + executionData.get("abortedby");
			endStatus = executionData.get("status") + " by " + executionData.get("abortedby");
		} else {
			jobStatus = ((String) executionData.get("status")).toUpperCase();
			endStatus = "ended ";
		}

		final String projectUrl = jobContextMap.get("serverUrl") + "/" + jobContextMap.get("project");
		final String groupUrl = jobContextMap.get("serverUrl") + "/jobs/" + jobContextMap.get("project") + "/" + jobContextMap.get("group");
		final String title = "\"<" + executionData.get("href") + "|#" + executionData.get("id") + " - " + jobStatus + " - " + jobMap.get("name") + "> - <" + projectUrl + "|" + (String) executionData.get("project") + "> - <" + groupUrl + "|" + jobMap.get("group") + ">/<" + jobMap.get("href") + "|" + jobMap.get("name") + ">\"";

		final Long startTime = (Long) executionData.get("dateStartedUnixtime");
		final Long endTime = (Long) executionData.get("dateEndedUnixtime");
		final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
		final String duration = "Launched by " + executionData.get("user") + " at " + dateFormat.format(new Date(startTime)) + ", " + endStatus + " at " + dateFormat.format(new Date(endTime)) + " (duration: " + (endTime - startTime) / 1000 + "s)";

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{");
		stringBuilder.append("	\"attachments\":[");
		stringBuilder.append("		{");
		stringBuilder.append("			\"title\": " + title + ",");
		stringBuilder.append("			\"text\": \"" + duration + "\nOptions:\",");
		stringBuilder.append("			\"color\": \"" + statusColor + "\",");
		stringBuilder.append("			\"fields\":[");
		stringBuilder.append("				{");
		stringBuilder.append("					\"title\":\"Job Name\",");
		stringBuilder.append("					\"value\":\"test1\",");
		stringBuilder.append("					\"short\":true");
		stringBuilder.append("				},");
		stringBuilder.append("				{");
		stringBuilder.append("					\"title\":\"Job Name2\",");
		stringBuilder.append("					\"value\":\"test2\",");
		stringBuilder.append("					\"short\":true");
		stringBuilder.append("				},");
		stringBuilder.append("				{");
		stringBuilder.append("					\"title\":\"Job Name3\",");
		stringBuilder.append("					\"value\":\"test3\",");
		stringBuilder.append("					\"short\":true");
		stringBuilder.append("				},");
		stringBuilder.append("				{");
		stringBuilder.append("					\"title\":\"Job Name4\",");
		stringBuilder.append("					\"value\":\"test4\",");
		stringBuilder.append("					\"short\":false");
		stringBuilder.append("				}");
		stringBuilder.append("			]");
		stringBuilder.append("		},");
		stringBuilder.append("		{");
		stringBuilder.append("			\"fallback\": \"Failed node\",");
		stringBuilder.append("			\"text\": \"Failed node\",");
		stringBuilder.append("			\"color\": \"" + statusColor + "\",");
		stringBuilder.append("			\"fields\":[");
		stringBuilder.append("				{");
		stringBuilder.append("					\"title\":\"node1\",");
		stringBuilder.append("					\"value\":\"node1\",");
		stringBuilder.append("					\"short\":true");
		stringBuilder.append("				},");
		stringBuilder.append("				{");
		stringBuilder.append("					\"title\":\"node2\",");
		stringBuilder.append("					\"value\":\"node1\",");
		stringBuilder.append("					\"short\":true");
		stringBuilder.append("				},");
		stringBuilder.append("				{");
		stringBuilder.append("					\"title\":\"node3\",");
		stringBuilder.append("					\"value\":\"node3\",");
		stringBuilder.append("					\"short\":true");
		stringBuilder.append("				}");
		stringBuilder.append("			]");
		stringBuilder.append("		}");
		stringBuilder.append("	]");
		stringBuilder.append("}");

		return stringBuilder.toString();
	}
}
