/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.dp;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasConfig;
import org.geopublishing.atlasViewer.AtlasStatusDialogInterface;
import org.geopublishing.atlasViewer.dp.layer.DpLayerRaster;
import org.geopublishing.atlasViewer.exceptions.AtlasImportException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.DpEditableInterface;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.swing.GpSwingUtil;
import org.geotools.data.DataUtilities;

import schmitzm.geotools.io.GeoImportUtil;
import schmitzm.geotools.io.GeoImportUtil.WORLD_POSTFIXES;
import schmitzm.io.IOUtil;

public class DpLayerRasterEd extends DpLayerRaster implements
		DpEditableInterface {
	final static private Logger LOGGER = Logger
			.getLogger(DpLayerRasterEd.class);

	/**
	 * Constructor
	 * 
	 * @param file
	 *            GeoTIFF file that represents the raster
	 * 
	 * @throws IOException
	 */
	public DpLayerRasterEd(Component owner, AtlasConfig ac, File file)
			throws AtlasImportException {
		super(ac);
		copyAndImport(owner, file);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see skrueger.creator.dp.DatapoolEditable#getAce()
	 */
	public AtlasConfigEditable getAce() {
		return (AtlasConfigEditable) getAtlasConfig();
	}

	/**
	 * Not only copies the file(s), but also sets up basic parameters like id,
	 * name etc
	 * 
	 * @param file
	 *            The raster file to import. Can be of many raster types.
	 *            .ascii, .png, etc..
	 */
	public void copyAndImport(Component owner, File file)
			throws AtlasImportException {

		// If denied by the user, this throws an exception
		setFilename(GpSwingUtil.cleanFilenameWithUI(owner, file.getName()));

		setId(GpUtil.getRandomID("raster"));
		// Set a directory
		String dirname = getId() + "_"
				+ getFilename().substring(0, getFilename().lastIndexOf('.'));

		// setTitle(new Translation(getAc().getLanguages(), getFilename()));
		// setDesc(new Translation(getAc().getLanguages(), ""));
		// setTitle(new Translation());
		// setDesc(new Translation());

		File dataDir = new File(getAce().getDataDir(), dirname);
		setDataDirname(dirname);

		DpeImportUtil.copyFilesWithOrWithoutGUI(this, DataUtilities
				.fileToURL(file), owner, dataDir);
	}

	@Override
	public void copyFiles(URL sourceUrl, Component owner, File targetDir,
			AtlasStatusDialogInterface atlasStatusDialog) throws Exception {

		// ****************************************************************************
		// Copy the files into the atlas folder tree, clean the filenames on the
		// way.
		// ****************************************************************************
		AVUtil.copyUrl(sourceUrl, targetDir, true);

		// Try to copy pending world files...
		for (WORLD_POSTFIXES pf : GeoImportUtil.WORLD_POSTFIXES.values()) {
			AVUtil.copyURLNoException(IOUtil.changeUrlExt(sourceUrl, pf
					.toString()), targetDir, true);
		}

		// Copy optional .prj file to data directory
		AVUtil.copyURLNoException(IOUtil.changeUrlExt(sourceUrl, "prj"),
				targetDir, true);

		// Copy optional .sld file to data directory
		AVUtil.copyURLNoException(IOUtil.changeUrlExt(sourceUrl, "sld"),
				targetDir, true);
	}

}