package org.geopublishing.atlasViewer.dp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.Random;

import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster_Reader;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.junit.Ignore;
import org.junit.Test;

import de.schmitzm.geotools.LogoPosition;
import de.schmitzm.geotools.gui.ScalePanel;
import de.schmitzm.i18n.Translation;
import de.schmitzm.testing.TestingClass;

public class AMLImportTest extends TestingClass {

    @Test
    @Ignore
    // Fails with some additional style :-/
    public void testSaveAndLoad() throws Exception {
        AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);

        assertNotNull(ace.getResource("ad/data/vector_01367156967_join10/join10.shp"));

        String bnx = "testbasename_" + new Random().nextLong();
        try {
            ace.setBaseName(bnx);
            fail("A basename wit _ has wrongly been accepted.");
        } catch (IllegalArgumentException e) {
            bnx = "testbasename_" + new Random().nextLong();
            final String jnlpx = "http://asd.asd.asd/atl/" + bnx + "x/";
            ace.setJnlpBaseUrl(jnlpx);

            AtlasConfigEditable ace2 = GpTestingUtil.saveAndLoad(ace);

            assertNotNull(ace2.getResource("ad/data/vector_01367156967_join10/join10.shp"));

            assertEquals(bnx, ace2.getBaseName());
            assertEquals(jnlpx, ace2.getJnlpBaseUrl());
        }
    }

    @Test
    public void testFtpExportAuth() throws Exception {
        AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);
        ace.setGpHosterAuth(true);
        AtlasConfigEditable ace2 = GpTestingUtil.saveAndLoad(ace);
        assertTrue(ace2.getGpHosterAuth());
        ace.deleteAtlas();
        ace2.deleteAtlas();
    }

    // @Test
    // @Ignore
    // public void testAmlSaveMemoryError() throws Exception {
    // AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);
    // for (int i = 0; i < 100; i++) {
    // GpTestingUtil.saveAndLoad(ace);
    // }
    // }

    @Test
    public void testFtpExportAuth2() throws Exception {
        AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);
        ace.setGpHosterAuth(false);
        AtlasConfigEditable ace2 = GpTestingUtil.saveAndLoad(ace);
        assertFalse(ace2.getGpHosterAuth());
        ace.deleteAtlas();
        ace2.deleteAtlas();
    }

    @Test
    public void testImportExport_MetricNotVisible_MapPosition() throws Exception {
        AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE(TestAtlas.small);

        Map map1_0 = ace.getMapPool().get(0);
        map1_0.setScaleUnits(ScalePanel.ScaleUnits.METRIC);
        map1_0.setScaleVisible(false);

        ace.setMaplogoPosition(LogoPosition.TOPLEFT);

        AtlasConfigEditable ace2 = GpTestingUtil.saveAndLoad(ace);

        assertEquals(LogoPosition.TOPLEFT, ace2.getMaplogoPosition());

        Map map2_0 = ace2.getMapPool().get(0);
        assertEquals(map1_0.getScaleUnits(), map2_0.getScaleUnits());
        assertEquals(map1_0.isScaleVisible(), map2_0.isScaleVisible());

        map1_0 = ace.getMapPool().get(0);
        map1_0.setScaleUnits(ScalePanel.ScaleUnits.US);
        map1_0.setScaleVisible(true);
        map1_0.setPreviewMapExtendInGeopublisher(true);

        ace.setMaplogoPosition(LogoPosition.BOTTOMLEFT);
        ace2 = GpTestingUtil.saveAndLoad(ace);
        assertEquals(LogoPosition.BOTTOMLEFT, ace2.getMaplogoPosition());

        map2_0 = ace2.getMapPool().get(0);
        assertEquals(map1_0.getScaleUnits(), map2_0.getScaleUnits());
        assertEquals(map1_0.isScaleVisible(), map2_0.isScaleVisible());
        assertEquals(map1_0.isPreviewMapExtendInGeopublisher(),
                map2_0.isPreviewMapExtendInGeopublisher());

        ace.dispose();
        ace.deleteAtlas();
        ace2.dispose();
        ace2.deleteAtlas();
    }

    @Test
    public void testImportExport_RasterNodataValue() throws Exception {
        AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE(TestAtlas.rasters);

        DpLayerRaster_Reader dplr = (DpLayerRaster_Reader) ace.getDataPool().get(0);
        dplr.setNodataValue(-999.0);

        AtlasConfigEditable ace2 = GpTestingUtil.saveAndLoad(ace);
        final DpLayerRaster_Reader dplr2 = (DpLayerRaster_Reader) ace2.getDataPool().get(0);
        assertNotNull(dplr2);
        assertNotNull(dplr2.getNodataValue());
        assertEquals(-999., dplr2.getNodataValue(), 0.);
        ace2.dispose();
        ace2.deleteAtlas();
        ace2 = null;

        dplr.setNodataValue(null);
        AtlasConfigEditable ace3 = GpTestingUtil.saveAndLoad(ace);
        assertNull(((DpLayerRaster_Reader) ace3.getDataPool().get(0)).getNodataValue());
        ace3.dispose();
        ace3.deleteAtlas();
        ace3 = null;

        ace.dispose();
        ace.deleteAtlas();
    }

    @Test
    public void testImportExportBandNames() throws Exception {
        AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE(TestAtlas.rasters);
        Iterator<DpLayerRaster> rasterLayers = ace.getDataPool().getRasterLayers().iterator();
        DpLayerRaster dplr = rasterLayers.next();
        while (!dplr.getFilename().equals("geotiff_rgb_ohnesld.tif")){
            dplr = rasterLayers.next();
        }
        dplr.setBandNames(new Translation("a"), new Translation("b"));
        AtlasConfigEditable ace2 = GpTestingUtil.saveAndLoad(ace);
        Iterator<DpLayerRaster> rasterLayers2 = ace2.getDataPool().getRasterLayers().iterator();
        DpLayerRaster dplr2 = rasterLayers2.next();
        while (!dplr2.getFilename().equals("geotiff_rgb_ohnesld.tif")){
            dplr2 = rasterLayers2.next();
        }
        Translation[] bandNames = dplr2.getBandNames();

        assertTrue(bandNames[0].toString().equals("a"));
        assertTrue(bandNames[1].toString().equals("b"));
    }
    
//    @Test
//    public void testImportExportBarChart() throws Exception{
//    	AtlasConfigEditable ace = GpTestingUtil.getAtlasConfigE(TestAtlas.charts);
//        Iterator<DpLayerVector> vectorLayers = ace.getDataPool().getVectorLayers().iterator();
//        DpLayerVector dplv = vectorLayers.next();
////        dplv.get
//    }
    /**
     * AtlasStylerVector atlasStyler = AsTestingUtil
				.getAtlasStyler(TestDatasetsVector.countryShp);
     * 
     * if (StylingUtil.getTextSymbolizers(rule.getSymbolizers())
						.size() == rule.getSymbolizers().length)
					continue;

				if (ChartGraphic.isChart(rule.symbolizers().get(0))) {
     * PointSymbolizer pointSymbolizer = (PointSymbolizer) rule
							.symbolizers().get(0);

					ChartGraphic cg = new ChartGraphic(
							pointSymbolizer.getGraphic());
							
							<sld:PointSymbolizer>
        <sld:Geometry>
          <ogc:PropertyName>the_geom</ogc:PropertyName>
        </sld:Geometry>
        <sld:Graphic>
          <sld:ExternalGraphic>
            <sld:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://chart?cht=bvg&amp;chl=male|female&amp;chd=t:${100 * 2 / (2 + 3)},${100 * 3 / (2 + 3)}&amp;chs=200x100&amp;chf=bg,s,FFFFFF00"/>
            <sld:Format>application/chart</sld:Format>
          </sld:ExternalGraphic>
        </sld:Graphic>
      </sld:PointSymbolizer>
     */
}
