package com.github.sbugat.rundeck.plugins;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

	public void ulrTest() throws MalformedURLException, IOException {
		new URL(null).openConnection();
	}
	
	@Override
	public boolean postNotification(final String trigger, @SuppressWarnings("rawtypes") final Map executionData, @SuppressWarnings("rawtypes") final Map config) {

		HttpURLConnection connection = null;
		try {
			
			//Prepare the connection to Slack
			connection = (HttpURLConnection) new URL(slackIncomingWebHookUrl).openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("charset", StandardCharsets.UTF_8.name());
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			//Send the WebHook message
			try( final DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream())) {
				dataOutputStream.writeBytes("payload=" + URLEncoder.encode("{" + getMessageOptions() + getMessage(trigger, executionData, config) + "}", StandardCharsets.UTF_8.name()));
			}

			//Get the HTTP response code
			if( HttpURLConnection.HTTP_OK != connection.getResponseCode() ) {
				return false;
			}
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			if( null != connection ){
				connection.disconnect();
			}
		}

		return true;
	}

	/**
	 * Return a message with overridden options.
	 * 
	 * @return optional message with channel, username and emoji to use
	 */
	private String getMessageOptions() {

		final StringBuilder stringBuilder = new StringBuilder();
		if (null != slackOverrideDefaultWebHookChannel) {
			stringBuilder.append("\"channel\":");
			stringBuilder.append("\"" + slackOverrideDefaultWebHookChannel + "\", ");
		}
		if (null != slackOverrideDefaultWebHookName) {
			stringBuilder.append("\"username\":");
			stringBuilder.append("\"" + slackOverrideDefaultWebHookName + "\", ");
		}
		if (null != slackOverrideDefaultWebHookEmoji) {
			stringBuilder.append("\"icon_emoji\":");
			stringBuilder.append("\"" + slackOverrideDefaultWebHookEmoji + "\", ");
		}

		return stringBuilder.toString();
	}

	/**
	 * Return a Slack message with the job execution data.
	 * 
	 * @param trigger execution status
	 * @param executionData current execution state
	 * @param config plugin configuration
	 * 
	 * @return complete job execution message to send to Slack
	 */
	private String getMessage(final String trigger, @SuppressWarnings("rawtypes") final Map executionData, @SuppressWarnings("rawtypes") final Map config) {

		// Success and starting execution are good(green)
		final String statusColor;
		if ("success" == trigger || "start" == trigger) {
			statusColor = SLACK_SUCCESS_COLOR;
		} else {
			statusColor = SLACK_FAILED_COLOR;
		}

		@SuppressWarnings("unchecked")
		final Map<String, String> jobMap = (Map<String, String>) executionData.get("job");

		final String jobStatus;
		final String endStatus;
		if ("aborted" == executionData.get("status") && null != executionData.get("abortedby")) {
			jobStatus = ((String) executionData.get("status")).toUpperCase() + " by " + executionData.get("abortedby");
			endStatus = executionData.get("status") + " by " + executionData.get("abortedby");
		} else if ("timedout" == executionData.get("status")) {
			jobStatus = ((String) executionData.get("status")).toUpperCase();
			endStatus = "timed-out";
		} else {
			jobStatus = ((String) executionData.get("status")).toUpperCase();
			endStatus = "ended";
		}
		
		
		// Context map containing additional information
		@SuppressWarnings("unchecked")
		final Map<String, Map<String, String>> contextMap = (Map<String, Map<String, String>>) executionData.get("context");
		final Map<String, String> jobContextMap = contextMap.get("job");

		final String projectUrl = jobContextMap.get("serverUrl") + "/" + jobContextMap.get("project");

		final StringBuilder formatedGroups = new StringBuilder();
		if (null != jobContextMap.get("group")) {
			String rootGroups = "";
			for (final String group : jobContextMap.get("group").split("/")) {
				formatedGroups.append("<" + projectUrl + "/jobs/" + rootGroups + group + "|" + group + ">/");
				rootGroups = rootGroups + group + "/";
			}
		}

		final String title = "\"<" + executionData.get("href") + "|#" + executionData.get("id") + " - " + jobStatus + " - " + jobMap.get("name") + "> - <" + projectUrl + "|" + (String) executionData.get("project") + "> - " + formatedGroups + "<" + jobMap.get("href") + "|" + jobMap.get("name") + ">\"";

		final Long startTime = (Long) executionData.get("dateStartedUnixtime");
		final Long endTime = (Long) executionData.get("dateEndedUnixtime");
		final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
		final String duration;
		if ("start" == trigger) {
			duration = "Launched by " + executionData.get("user") + " at " + dateFormat.format(new Date(startTime));
		} else {
			duration = "Launched by " + executionData.get("user") + " at " + dateFormat.format(new Date(startTime)) + ", " + endStatus + " at " + dateFormat.format(new Date(endTime)) + " (duration: " + (endTime - startTime) / 1000 + "s)";
		}

		// Download link if the job fails
		final String download;
		if ("success" != trigger && "start" != trigger) {
			download = "\n<" + projectUrl + "/execution/downloadOutput/" + executionData.get("id") + "|Download log ouput>";
		} else {
			download = "";
		}

		final Map<String, String> optionContextMap = contextMap.get("option");
		
		// Option header
		final String option;
		if (null == optionContextMap || optionContextMap.isEmpty()) {
			option = "";
		} else if (download.isEmpty()) {
			option = "\nJob options:";
		} else {
			option = ", job options:";
		}

		// Attachment begin and title
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\"attachments\":[");
		stringBuilder.append("	{");
		stringBuilder.append("		\"title\": " + title + ",");
		stringBuilder.append("		\"text\": \"" + duration + download + option + "\",");
		stringBuilder.append("		\"color\": \"" + statusColor + "\"");
		
		//Job options section
		stringBuilder.append(getOptionsMessage(optionContextMap, contextMap.get("secureOption")));

		stringBuilder.append("	}");

		//Failed nodes section
		stringBuilder.append(getFailedNodesMessage( executionData, statusColor ) );

		stringBuilder.append("]");

		return stringBuilder.toString();
	}
	
	private CharSequence getOptionsMessage( final Map<String, String> optionContextMap, final Map<String, String> secureOptionContextMap ) {
		
		final StringBuilder messageBuilder = new StringBuilder();

		// Options part, secure options values are not displayed
		if (null != optionContextMap &&  ! optionContextMap.isEmpty() ) {
			
			messageBuilder.append(",\"fields\":[");
			boolean firstOption = true;
			for (final Map.Entry<String, String> mapEntry : optionContextMap.entrySet()) {

				if (!firstOption) {
					messageBuilder.append(',');
				}
				messageBuilder.append("{");
				messageBuilder.append("\"title\":\"" + mapEntry.getKey() + "\",");
				messageBuilder.append("\"value\":\"");

				if (null != secureOptionContextMap && null != secureOptionContextMap.get(mapEntry.getKey())) {
					messageBuilder.append( "***********");
				} else {
					messageBuilder.append( mapEntry.getValue());
				}
				
				messageBuilder.append("\",");
				messageBuilder.append("\"short\":true");
				messageBuilder.append("}");

				firstOption = false;
			}
			
			messageBuilder.append("]");
		}
		
		return messageBuilder;
	}
	
	/**
	 * Construct the failed nodes section.
	 * 
	 * @param executionData current execution state
	 * @param statusColor status color to display
	 * @return char sequence containing the formated section
	 */
	private CharSequence getFailedNodesMessage( @SuppressWarnings("rawtypes") final Map executionData, final String statusColor ) {
		
		final StringBuilder messageBuilder = new StringBuilder();
		
		@SuppressWarnings("unchecked")
		final List<String> failedNodeList = (List<String>) executionData.get("failedNodeList");
		@SuppressWarnings("unchecked")
		final Map<String, Integer> nodeStatus = (Map<String, Integer>) executionData.get("nodestatus");
		
		// Failed node part if a node is failed and if it's not the only one node executed
		if (null != failedNodeList && !failedNodeList.isEmpty() && nodeStatus.get("total") > 1) {
			messageBuilder.append(",{");
			messageBuilder.append("\"fallback\":\"Failed nodes list\",");
			messageBuilder.append("\"text\":\"Failed nodes:\",");
			messageBuilder.append("\"color\":\"");
			messageBuilder.append( statusColor );
			messageBuilder.append( "\",");
			messageBuilder.append("\"fields\":[");

			//Format a list with all failed nodes
			boolean firstNode = true;
			for (final String failedNode : failedNodeList) {

				if (!firstNode) {
					messageBuilder.append(',');
				}
				messageBuilder.append("{");

				messageBuilder.append("\"title\":\"");
				messageBuilder.append( failedNode);
				messageBuilder.append( "\",");
				messageBuilder.append("\"short\":true");

				messageBuilder.append("}");

				firstNode = false;
			}

			messageBuilder.append(']');
			messageBuilder.append('}');
		}
		
		return messageBuilder;
	}
}
