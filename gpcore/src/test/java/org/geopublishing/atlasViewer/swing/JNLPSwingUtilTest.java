package org.geopublishing.atlasViewer.swing;

import java.io.IOException;

import org.geopublishing.atlasViewer.swing.internal.AtlasStatusDialog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import schmitzm.swing.TestingUtil;

public class JNLPSwingUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoadPartEvenHeadless() throws IOException {
		if (TestingUtil.isInteractive()) {
			JNLPSwingUtil.loadPartAndCreateDialogForIt(null, "a");
			
			AtlasStatusDialog myAsg = new AtlasStatusDialog(null, "my","my");
			JNLPSwingUtil.loadPartAndCreateDialogForIt(myAsg, "a");
		}
		
	}

}