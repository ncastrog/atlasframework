package org.geopublishing.geopublisher.export;

import static org.junit.Assert.assertNotNull;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.TestResultProgressHandle;
import org.geopublishing.gpsync.AtlasFingerprint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.lang.LangUtil;
import de.schmitzm.testing.TestingClass;

public class GpFtpAtlasExportTest extends TestingClass {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRequestFingerprint() {
		AtlasConfigEditable ace = GpTestingUtil.TestAtlas.small.getAce();
		ace.setBaseName("chartdemo");
		GpFtpAtlasExport gpFtpAtlasExport = new GpFtpAtlasExport(ace);
		AtlasFingerprint requestFingerprint = gpFtpAtlasExport
				.requestFingerprint(null);
		System.out.println(requestFingerprint);
	}

	@Test
	public void testUpload() throws Exception {
		AtlasConfigEditable ace = GpTestingUtil.TestAtlas.small.getAce();
		ace.setBaseName("testUpload-" + System.currentTimeMillis());
		GpFtpAtlasExport gpFtpAtlasExport = new GpFtpAtlasExport(ace);
		AtlasFingerprint requestFingerprint = gpFtpAtlasExport
				.requestFingerprint(null);
		System.out.println(requestFingerprint);

		gpFtpAtlasExport.export(new TestResultProgressHandle());

		// wait a minute for gphoster to locate the zip
		Thread.sleep(LangUtil.MIN_MILLIS * 2);

		requestFingerprint = gpFtpAtlasExport.requestFingerprint(null);
		assertNotNull("After the upload the atlas should have a fingerprint",
				requestFingerprint);

	}

}