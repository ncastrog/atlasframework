package org.geopublishing.geopublisher.gui.map;

import static org.junit.Assert.assertNotNull;

import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.AtlasMapLegend;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPTestingUtil;
import org.geopublishing.geopublisher.GPTestingUtil.Atlas;
import org.junit.Test;

import schmitzm.geotools.gui.GeoMapPane;
import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.TestingUtil;

public class ManageChartsForMapDialogTest {

	@Test
	public void testManageChartsForMapDialog() throws Throwable {

		Map map = null;

		DpLayerVectorFeatureSource dplv = null;
		AtlasConfigEditable ace = GPTestingUtil.getAtlasConfigE(Atlas.small);
		for (Map m : ace.getMapPool().values()) {
			for (DpRef<DpLayer<?, ? extends ChartStyle>> dpe : m.getLayers()) {

				if (dpe.getTarget() instanceof DpLayerVectorFeatureSource
						&& dpe.getTarget().getCharts().size() > 0) {
					map = m;
					dplv = (DpLayerVectorFeatureSource) dpe.getTarget();
					continue;
				}
			}
			if (map != null)
				continue;
		}

		assertNotNull("One vector layer should be in the chart demo atlas",
				dplv);

		if (TestingUtil.INTERACTIVE) {
			AtlasMapLegend mapLegend = new AtlasMapLegend(new GeoMapPane(),
					map, ace, null);
			ManageChartsForMapDialog manageChartsForMapDialog = new ManageChartsForMapDialog(
					null, dplv, mapLegend);

			TestingUtil.testGui(manageChartsForMapDialog);
		}
	}

}