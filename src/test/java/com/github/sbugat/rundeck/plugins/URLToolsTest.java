package com.github.sbugat.rundeck.plugins;

import java.io.IOException;
import java.net.MalformedURLException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class URLToolsTest {

	private static final String TEST_URL = "http://unknown";
	
	@Test
	public void testOpenURLConnection() throws MalformedURLException, IOException {
		URLTools uRLTools = new URLTools();
		Assertions.assertThat(uRLTools.openURLConnection(TEST_URL)).isNotNull();
	}
	
	@Test(expected=MalformedURLException.class)
	public void testOpenURLConnectionNull() throws MalformedURLException, IOException {
		URLTools uRLTools = new URLTools();
		uRLTools.openURLConnection(null);
	}
	

}
