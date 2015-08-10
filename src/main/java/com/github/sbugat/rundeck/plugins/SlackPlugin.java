package com.github.sbugat.rundeck.plugins;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
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
		if ("success" == trigger || "start" == trigger) {
			statusColor = SLACK_SUCCESS_COLOR;
		} else {
			statusColor = SLACK_FAILED_COLOR;
		}

		@SuppressWarnings("unchecked")
		final Map<String, String> jobMap = (Map<String, String>) executionData.get("job");

		@SuppressWarnings("unchecked")
		final Map<String, Map<String, String>> contextMap = (Map<String, Map<String, String>>) executionData.get("context");
		final Map<String, String> jobContextMap = contextMap.get("job");
		final Map<String, String> optionContextMap = contextMap.get("option");
		final Map<String, String> secureOptionContextMap = contextMap.get("secureOption");

		@SuppressWarnings("unchecked")
		final List<String> failedNodeList = (List<String>) executionData.get("failedNodeList");
		@SuppressWarnings("unchecked")
		final Map<String, Integer> nodeStatus = (Map<String, Integer>) executionData.get("nodestatus");
		
		
		final String jobStatus;
		final String endStatus;
		if ("aborted" == executionData.get("status") && null != executionData.get("abortedby")) {
			jobStatus = ((String) executionData.get("status")).toUpperCase() + " by " + executionData.get("abortedby");
			endStatus = executionData.get("status") + " by " + executionData.get("abortedby");
		}
		else if ("timedout" == executionData.get("status")) {
			jobStatus = ((String) executionData.get("status")).toUpperCase();
			endStatus = "timed-out";
		}
		else {
			jobStatus = ((String) executionData.get("status")).toUpperCase();
			endStatus = "ended";
		}

		final String projectUrl = jobContextMap.get("serverUrl") + "/" + jobContextMap.get("project");
		
		final StringBuilder formatedGroups = new StringBuilder();
		if( null != jobContextMap.get("group")) {
			String rootGroups = "";
			for( final String group : jobContextMap.get("group").split("/") ) {
				formatedGroups.append("<" + projectUrl + "/jobs/"+ rootGroups + group + "|" + group + ">/");
				rootGroups =  rootGroups + group + "/";
			}
		}
		
		final String title = "\"<" + executionData.get("href") + "|#" + executionData.get("id") + " - " + jobStatus + " - " + jobMap.get("name") + "> - <" + projectUrl + "|" + (String) executionData.get("project") + "> - " + formatedGroups + "<" + jobMap.get("href") + "|" + jobMap.get("name") + ">\"";

		final Long startTime = (Long) executionData.get("dateStartedUnixtime");
		final Long endTime = (Long) executionData.get("dateEndedUnixtime");
		final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
		final String duration;
		if( "start" == trigger ) {
			duration = "Launched by " + executionData.get("user") + " at " + dateFormat.format(new Date(startTime));
		}
		else {
			duration = "Launched by " + executionData.get("user") + " at " + dateFormat.format(new Date(startTime)) + ", " + endStatus + " at " + dateFormat.format(new Date(endTime)) + " (duration: " + (endTime - startTime) / 1000 + "s)";
		}

		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{");
		stringBuilder.append("	\"attachments\":[");
		stringBuilder.append("		{");
		stringBuilder.append("			\"title\": " + title + ",");
		
		final String option;
		if( optionContextMap.isEmpty() ) {
			option = "";
		}
		else {
			option = "\nJob options:";
		}
		stringBuilder.append("			\"text\": \"" + duration + option + "\",");
		stringBuilder.append("			\"color\": \"" + statusColor + "\",");
		stringBuilder.append("			\"fields\":[");

		boolean firstOption = true;
		for (final Map.Entry<String, String> mapEntry : optionContextMap.entrySet()) {

			if (!firstOption) {
				stringBuilder.append(',');
			}
			stringBuilder.append("				{");
			stringBuilder.append("					\"title\":\"" + mapEntry.getKey() + "\",");
			final String value;
			if( null == secureOptionContextMap.get(mapEntry.getKey())) {
				value = mapEntry.getValue();
			}
			else {
				value = "***********";
			}
			stringBuilder.append("					\"value\":\"" + value + "\",");
			stringBuilder.append("					\"short\":true");
			stringBuilder.append("				}");

			firstOption = false;
		}
		stringBuilder.append("			]");
		stringBuilder.append("		}");
		
		if( ! failedNodeList.isEmpty() && nodeStatus.get("total") > 1 ) {
			stringBuilder.append(",");
			stringBuilder.append("		{");
			stringBuilder.append("			\"fallback\": \"Failed nodes\",");
			stringBuilder.append("			\"text\": \"Failed nodes:\",");
			stringBuilder.append("			\"color\": \"" + statusColor + "\",");
			stringBuilder.append("			\"fields\":[");
			
			boolean firstNode = true;
			for( final String failedNode : failedNodeList ) {
				
				if( ! firstNode ) {
					stringBuilder.append(',');
				}
				stringBuilder.append("				{");
				stringBuilder.append("					\"title\":\"" + failedNode + "\",");
				stringBuilder.append("					\"short\":true");
				stringBuilder.append("				}");
				
				firstNode = false;
			}
			
			stringBuilder.append("			]");
			stringBuilder.append("		}");
		}
		
		stringBuilder.append("	]");
		stringBuilder.append("}");

		return stringBuilder.toString();
	}
}
