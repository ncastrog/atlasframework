package org.geopublishing.geopublisher.swing;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.geopublishing.atlasViewer.http.Webserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.schmitzm.testing.TestingClass;

public class HTMLEditPaneJHTMLEditorTest extends TestingClass {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@Ignore
	public void testD() {
		File f = new File(
				"file:/home/stefan/Desktop/GP/Atlanten/ChartDemoAtlas/AWC/ad/html/map_01357691812");
		assertNotNull(f);
		assertTrue(f.exists());
		assertTrue(f.isDirectory());
	}

	@Test
	public void testD2() throws MalformedURLException, IOException {
		InputStream openStream = new URL("http://localhost:" + Webserver.PORT
				+ "/browser.html").openStream();
		assertNotNull(openStream);
		openStream.close();
	}

}