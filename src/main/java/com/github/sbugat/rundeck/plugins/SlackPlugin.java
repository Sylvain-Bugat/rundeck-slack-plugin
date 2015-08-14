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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
@Plugin(service = ServiceNameConstants.Notification, name = "SlackNotificationPlugin")
@PluginDescription(title = "Slack")
public class SlackPlugin implements NotificationPlugin {

	private static final String SLACK_SUCCESS_COLOR = "good";
	private static final String SLACK_FAILED_COLOR = "danger";
	
	private Logger logger = Logger.getLogger(SlackPlugin.class.getName());

	@PluginProperty(title = "Incoming WebHook URL", description = "Slack incoming WebHook URL", required = true)
	private String slackIncomingWebHookUrl;

	@PluginProperty(title = "WebHook channel", description = "Override default WebHook channel (#channel")
	private String slackOverrideDefaultWebHookChannel;

	@PluginProperty(title = "WebHook name", description = "Override default WebHook name")
	private String slackOverrideDefaultWebHookName;

	@PluginProperty(title = "WebHook emoji", description = "Override default WebHook icon (:emoji:)")
	private String slackOverrideDefaultWebHookEmoji;

	public void ulrTest() throws MalformedURLException, IOException {
		new URL(null).openConnection();
	}
	
	@Override
	public boolean postNotification(final String trigger, @SuppressWarnings("rawtypes") final Map executionData, @SuppressWarnings("rawtypes") final Map config) {

		@SuppressWarnings("unchecked")
		final Map<String, String> jobMap = (Map<String, String>) executionData.get("job");
		
		final String jobName;
		if( null != jobMap ) {
			jobName=jobMap.get("name");
		}
		else {
			jobName = null;
		}
		
		logger.log(Level.FINE, "Start to send Slack notification to WebHook URL {0} for the job {1} with trigger {2}", new Object[] {slackIncomingWebHookUrl, jobName, trigger});
		
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
			final String messagePayload= "{" + getMessageOptions() + getMessage(trigger, executionData, config) + "}";
			try( final DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream())) {
				dataOutputStream.writeBytes("payload=" + URLEncoder.encode(messagePayload, StandardCharsets.UTF_8.name()));
			}

			//Get the HTTP response code
			final int httpResponseCode = connection.getResponseCode();
			if( HttpURLConnection.HTTP_OK != httpResponseCode ) {
				
				if( HttpURLConnection.HTTP_NOT_FOUND == httpResponseCode) {
					logger.log(Level.SEVERE, "Invalid Slack WebHook URL {0} when sending {1} job notification with trigger {2}", new Object[] {slackIncomingWebHookUrl, jobName, trigger});
				}
				else {
					logger.log(Level.SEVERE, "Error sending {0} job notification with trigger {1}, http code: {2}", new Object[] {jobName, trigger, connection.getResponseCode()});
					logger.log(Level.FINE, "Error sending {0} job notification with trigger {1}, http code: {2}, payload:{3}", new Object[] {jobName, trigger, connection.getResponseCode(), messagePayload} );
				}
				return false;
			}
		}
		catch( final MalformedURLException e ) {
			logger.log(Level.SEVERE, "Malformed Slack WebHook URL {0} when sending {1} job notification with trigger {2}", new Object[] {slackIncomingWebHookUrl, jobName, trigger});
			return false;
		}
		catch (final IOException e) {
			logger.log(Level.SEVERE, e.getMessage());
			logger.log(Level.FINE, e.getMessage(), e );
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
		if ("success".equals(trigger) || "start".equals(trigger) ) {
			statusColor = SLACK_SUCCESS_COLOR;
		} else {
			statusColor = SLACK_FAILED_COLOR;
		}
		
		// Context map containing additional information
		@SuppressWarnings("unchecked")
		final Map<String, Map<String, String>> contextMap = (Map<String, Map<String, String>>) executionData.get("context");

		// Attachment begin and title
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\"attachments\":[");
		stringBuilder.append("	{");
		stringBuilder.append("		\"title\": " + getTitleMessage( executionData ) + ",");
		stringBuilder.append("		\"text\": \"" + getDurationMessage(executionData) + getDownloadOptionMessage(executionData) + "\",");
		stringBuilder.append("		\"color\": \"" + statusColor + "\"");
		
		//Job options section
		stringBuilder.append(getOptionsMessage(contextMap.get("option"), contextMap.get("secureOption")));

		stringBuilder.append("	}");

		//Failed nodes section
		stringBuilder.append(getFailedNodesMessage( executionData, statusColor ) );

		stringBuilder.append("]");

		return stringBuilder.toString();
	}
	
	private CharSequence getDownloadOptionMessage( @SuppressWarnings("rawtypes") final Map executionData ) {
		
		// Context map containing additional information
		@SuppressWarnings("unchecked")
		final Map<String, Map<String, String>> contextMap = (Map<String, Map<String, String>>) executionData.get("context");
		final Map<String, String> jobContextMap = contextMap.get("job");
				
		final StringBuilder downloadOptionBuilder = new StringBuilder();
		
		// Download link if the job fails
		boolean download = false;
		if (! "running".equals( executionData.get("status")) && "running".equals(executionData.get("status"))) {
			downloadOptionBuilder.append( "\n<" + jobContextMap.get("serverUrl") + "/" + executionData.get("project") + "/execution/downloadOutput/" + executionData.get("id") + "|Download log ouput>" );
			download = true;
		}

		final Map<String, String> optionContextMap = contextMap.get("option");
		
		// Option header
		if (null != optionContextMap && ! optionContextMap.isEmpty() ) {
			if (! download) {
				downloadOptionBuilder.append( "\nJob options:" );
			} else {
				downloadOptionBuilder.append( ", job options:" );
			}
		}
		
		return downloadOptionBuilder;
	}
	
	private CharSequence getDurationMessage( @SuppressWarnings("rawtypes") final Map executionData ) {

		final Long startTime = (Long) executionData.get("dateStartedUnixtime");
		
		final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
	
		final StringBuilder durationBuilder = new StringBuilder();
		
		durationBuilder.append("Launched by ");
		durationBuilder.append(executionData.get("user"));
		durationBuilder.append( " at ");
		durationBuilder.append(dateFormat.format(new Date(startTime)));

		if ("aborted".equals(executionData.get("status")) && null != executionData.get("abortedby")) {
			
			durationBuilder.append(executionData.get("status"));
			durationBuilder.append(" by ");
			durationBuilder.append(executionData.get("abortedby"));
		}
		else if ( ! "running".equals(executionData.get("status"))){
			
			final Long endTime = (Long) executionData.get("dateEndedUnixtime");
			
			if ("timedout".equals(executionData.get("status"))) {
				durationBuilder.append("timed-out");
			} else {
				durationBuilder.append("ended");
			}
			
			durationBuilder.append(dateFormat.format(new Date(endTime)));
			durationBuilder.append(" (duration: ");
			durationBuilder.append(formatDuration(endTime - startTime));
			durationBuilder.append(')');
		}
		
		return durationBuilder;
	}
	
	private CharSequence getTitleMessage( @SuppressWarnings("rawtypes") final Map executionData ) {
		
		@SuppressWarnings("unchecked")
		final Map<String, String> jobMap = (Map<String, String>) executionData.get("job");
		
		// Context map containing additional information
		@SuppressWarnings("unchecked")
		final Map<String, Map<String, String>> contextMap = (Map<String, Map<String, String>>) executionData.get("context");
		final Map<String, String> jobContextMap = contextMap.get("job");
	
		final StringBuilder titleBuilder = new StringBuilder();
		titleBuilder.append("\"<");
		titleBuilder.append( executionData.get("href") );
		titleBuilder.append( "|#" );
		titleBuilder.append( executionData.get("id") );
		titleBuilder.append( " - " );
		titleBuilder.append( executionData.get("status") );
		
		if ("aborted".equals(executionData.get("status")) && null != executionData.get("abortedby")) {
			titleBuilder.append( " by " );
			titleBuilder.append( executionData.get("abortedby") );
		}
		
		titleBuilder.append( " - " );
		titleBuilder.append( jobMap.get("name") );
		titleBuilder.append( "> - <" );
		titleBuilder.append( jobContextMap.get("serverUrl") );
		titleBuilder.append( '/' );
		titleBuilder.append( executionData.get("project") );
		titleBuilder.append( '|' );
		titleBuilder.append( executionData.get("project") );
		titleBuilder.append( "> - " );
		
		if (null != jobMap.get("group")) {
			
			final StringBuilder rootGroups = new StringBuilder();
			for (final String group : jobMap.get("group").split("/")) {
				
				rootGroups.append( '/' );
				rootGroups.append( group );
				
				titleBuilder.append('<');
				titleBuilder.append(jobContextMap.get("serverUrl"));
				titleBuilder.append('/');
				titleBuilder.append(executionData.get("project"));
				titleBuilder.append("/jobs");
				titleBuilder.append(rootGroups);
				titleBuilder.append('|');
				titleBuilder.append(group);
				titleBuilder.append(">/");				
				
				rootGroups.append( group );
				rootGroups.append( '/' );
			}
		}
		
		titleBuilder.append('<');
		titleBuilder.append(jobMap.get("href"));
		titleBuilder.append('|');
		titleBuilder.append(jobMap.get("name"));
		titleBuilder.append(">\"");
		
		return titleBuilder;
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
	
	/**
     * Format a millisecond duration to a readeable formatted String.
     * 
     * @param milliseconds a positive duration in milliseconds to convert
     * @return A string of the form "Xd Yh" or "Xh Ym" or "Xm Ys" or "Xs".
     */
    public static CharSequence formatDuration(final long milliseconds) {

    	long millisecondsReminder = milliseconds;
    	
        final long days = TimeUnit.MILLISECONDS.toDays(millisecondsReminder);
        millisecondsReminder -= TimeUnit.DAYS.toMillis(days);
        final long hours = TimeUnit.MILLISECONDS.toHours(millisecondsReminder);
        millisecondsReminder -= TimeUnit.HOURS.toMillis(hours);
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(millisecondsReminder);
        millisecondsReminder -= TimeUnit.MINUTES.toMillis(minutes);
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(millisecondsReminder);
        
        if( days > 0 ){
        	return days + "d " + hours + "h";
        }
        else  if( hours > 0 ){
        	return hours + "h " + minutes + "m";
        }
        else  if( minutes > 0 ){
        	return minutes + "m " + seconds + "s";
        }

        return seconds + "s";
    }
}
