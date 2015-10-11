package com.github.sbugat.rundeck.plugins;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * URL tools class.
 *
 * @author Sylvain Bugat
 *
 */
public class URLTools {

	public HttpURLConnection openURLConnection( final String url ) throws MalformedURLException, IOException {
		return (HttpURLConnection) new URL(url).openConnection();
	}
}
