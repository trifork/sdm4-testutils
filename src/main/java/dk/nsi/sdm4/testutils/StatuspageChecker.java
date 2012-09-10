package dk.nsi.sdm4.testutils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.fail;

/**
 * Class meant to be used from junit-based tests.
 * Checks that a statuspage returned from the StatusReporter class in sdm4-core is running on
 * localhost 8080.
 * This class assumes the module is already deployed to an app server.
 */
public class StatuspageChecker {
	private static final int MAX_RETRIES = 10;
	private String url;

	public StatuspageChecker(String modulename) {
		this.url = "http://localhost:8080/" + modulename + "/status";
	}

	public void assertThatStatuspageReturns200OK() throws IOException, InterruptedException {
		StatuspageResult result = fetchStatusPage();

		if (result.status != 200) {
			fail("Status page on " + url + " did not respond with HTTP code 200 after " + MAX_RETRIES + " retries, last status was " + result.status);
		}
	}

	public StatuspageResult fetchStatusPage() throws IOException, InterruptedException {
		final URL u = new URL(url);
		StatuspageResult lastResultSeen = null;
		for (int i = 0; i < MAX_RETRIES; i++) {
			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
			connection.connect();

			try {
				lastResultSeen = new StatuspageChecker.StatuspageResult(connection.getResponseCode(), IOUtils.toString(connection.getInputStream()));
			} finally {
				connection.disconnect();
			}

			if (lastResultSeen.status == 200) {
				break;
			}

			if (lastResultSeen.status != 404 && lastResultSeen.status != 503) {
				fail("Status page on " + url + " did not respond with HTTP code 200, status was " + lastResultSeen.status);
			}

			Thread.sleep(1000);
		}

		return lastResultSeen;
	}

	public class StatuspageResult {
		public int status;
		public String responseBody;

		private StatuspageResult(int status, String responseBody) {
			this.status = status;
			this.responseBody = responseBody;
		}
	}
}
