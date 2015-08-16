package com.github.sbugat.rundeck.plugins;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Rundeck slack plugin test class.
 *
 * @author Sylvain Bugat
 *
 */
public class SlackPluginTest {

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
}
