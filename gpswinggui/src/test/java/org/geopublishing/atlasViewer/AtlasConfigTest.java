package org.geopublishing.atlasViewer;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPTestingUtil;
import org.geopublishing.geopublisher.GPTestingUtil.Atlas;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

public class AtlasConfigTest {
	
	private AtlasConfigEditable ace;

	@Before
	public void setup() throws Exception
	{
		ace = GPTestingUtil.getAtlasConfigE(Atlas.small);
	}
	
	@After
	public void dispose() {
		if (ace != null) ace.dispose();
	}
	

	@Test
	public void testGetIconURL() {
		assertNotNull ("At least the fallback must be found.", ace.getIconURL() );
	}
	
	@Test
	public void getPpUpHTML() {
		assertNull ("Small atlas doesn't have a popup HTML", ace.getPopupHTMLURL());
	}

}