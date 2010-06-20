package org.geopublishing.geopublisher.gui.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPTestingUtil;
import org.geopublishing.geopublisher.GPTestingUtil.Atlas;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

public class MapPoolDuplicateActionTest {

	@Test
	public void testDuplicateMap() throws AtlasException, FactoryException,
			TransformException, SAXException, IOException,
			ParserConfigurationException {
		AtlasConfigEditable ace = GPTestingUtil.getAtlasConfigE(Atlas.small);
		Map map1 = ace.getMapPool().get(0);
		File htmlDir1 = ace.getHtmlDirFor(map1);

		int count1 = htmlDir1.list().length;
		long size1 = FileUtils.sizeOfDirectory(htmlDir1);
		
		assertTrue("map html dir may not be empty for this test", count1 > 0);

		if (GPTestingUtil.INTERACTIVE) {
			MapPoolDuplicateAction mapPoolDuplicateAction = new MapPoolDuplicateAction(
					new MapPoolJTable(ace));
			
			// Start copy now!
			Map map2 = mapPoolDuplicateAction
					.actionPerformed(map1);
			
			
			assertFalse(map1.equals(map2));
			File htmlDir2 = ace.getHtmlDirFor(map2);
			assertFalse(htmlDir1.equals(htmlDir2));
			
			long size2 = FileUtils.sizeOfDirectory(htmlDir2);
			int count2 = htmlDir2.list().length;
			
			assertEquals(size1, size2);
			assertEquals(count1, count2);
			
			// Cleanup
			FileUtils.deleteDirectory(htmlDir2);
		}
		
		
		
	}

}