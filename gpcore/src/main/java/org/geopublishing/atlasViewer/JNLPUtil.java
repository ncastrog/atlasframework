/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer;

import java.net.URL;
import java.util.ArrayList;

import javax.jnlp.DownloadService;
import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.AtlasJWSCachedResourceLoader;
import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.DpRef;
import org.geopublishing.atlasViewer.exceptions.AtlasFatalException;
import org.geopublishing.atlasViewer.map.Map;
import org.geopublishing.atlasViewer.swing.AtlasViewerGUI;

/**
 * A utility class with static methods that deal with JNLP / JavaWebStart
 * related stuff.
 * 
 * @author Stefan Alfons Tzeggai
 * 
 */
public class JNLPUtil {
	final static private Logger LOGGER = Logger.getLogger(JNLPUtil.class);

	private static DownloadService jnlpDownloadService;

	/** Caches whether the AtlasData comes from a **/
	private static Boolean isAtlasDataFromJWS = null;

	/**
	 * We cache the JNLP DownloadService object so we don't have to look it up
	 * again and again.
	 * 
	 * @return always the same {@link DownloadService}
	 * @throws UnavailableServiceException
	 *             if we are not in a JWS context.
	 */
	public static DownloadService getJNLPDownloadService()
			throws UnavailableServiceException {
		if (jnlpDownloadService == null) {
			jnlpDownloadService = (DownloadService) ServiceManager
					.lookup("javax.jnlp.DownloadService");

		}
		return jnlpDownloadService;
	}

	/**
	 * <code>true</code>, if the application has been started by Java Web Start
	 * (JWS)
	 */
	public static boolean isJnlpServiceAvailable() {
		try {
			getJNLPDownloadService();
			return true;
		} catch (UnavailableServiceException e) {
			return false;
		}
	}

	/**
	 * Registers the given instance to the {@link SingleInstanceService} - if we
	 * are running in JavaWebStart context.
	 * 
	 * @param instance
	 *            The {@link SingleInstanceListener} instance.
	 * @param add
	 *            If <code>true</code> the instance will be added to the
	 *            {@link SingleInstanceService}. Otherwise it will be removed.
	 */
	public static void registerAsSingleInstance(
			SingleInstanceListener instance, boolean add) {

		Logger LOGGER = Logger.getLogger(instance.getClass());

		try {
			SingleInstanceService singleInstanceService = (SingleInstanceService) ServiceManager
					.lookup("javax.jnlp.SingleInstanceService");
			// add the listener to this application!

			if (add) {
				LOGGER.info("Registering as a JNLP SingleInstance...");
				singleInstanceService.addSingleInstanceListener(instance);
			} else {
				LOGGER.info("Un-registering as a JNLP SingleInstance...");
				singleInstanceService.removeSingleInstanceListener(instance);
			}
			LOGGER.info(" ...OK!");
		} catch (UnavailableServiceException use) {
		}
	}

	/**
	 * Checks which parts of the JNLP resources have not yet been cached.
	 * 
	 * @see #getJWSDownloadAllMenuItem()
	 * 
	 * @throws UnavailableServiceException
	 *             if we are not using JWS
	 */
	public static ArrayList<String> countPartsToDownload(DataPool dataPool) {
		ArrayList<String> haveToDownload = new ArrayList<String>();

		try {

			if (AtlasViewerGUI.getInstance().isPreviewMode()) {
				// When Geopublisher is started via JWS, all data is on disk,
				// even though it is not in JWS cache.
				return haveToDownload;
			}

			for (DpEntry<?> dpe : dataPool.values()) {
				// LOGGER.debug("Checking if part "
				// + dpe.getId()
				// + " is cached = "
				// + JNLPUtil.getJNLPDownloadService().isPartCached(
				// dpe.getId()));

				if (!JNLPUtil.getJNLPDownloadService()
						.isPartCached(dpe.getId())) {
					haveToDownload.add(dpe.getId());
				}
			}
		} catch (javax.jnlp.UnavailableServiceException ue) {
			// Return empty list!
			return haveToDownload;
		} catch (Exception e) {
			LOGGER.error(e);
		}

		if (haveToDownload.size() > 0)
			LOGGER.debug(haveToDownload.size() + " parts not cached.");

		return haveToDownload;
	}

	/**
	 * Checks which parts of the JNLP resources have not yet been cached.
	 * 
	 * @see #getJWSDownloadAllMenuItem()
	 * 
	 * @throws UnavailableServiceException
	 *             if we are not using JWS
	 */
	public static String[] countPartsToDownload(Map map) {
		ArrayList<String> partsToDownload = new ArrayList<String>();

		DownloadService ds;
		try {
			ds = JNLPUtil.getJNLPDownloadService();
		} catch (final UnavailableServiceException e1) {
			// return if we are not using JWS
			return new String[] {};
		}

		for (final DpRef<?> ref : map.getLayers()) {
			final DpEntry<?> dpe = ref.getTarget();
			if (!dpe.isDownloadedAndVisible()) {
				if (!ds.isPartCached(dpe.getId())) {
					partsToDownload.add(dpe.getId());
				}
			}
		}

		return partsToDownload.toArray(new String[partsToDownload.size()]);
	}

	/**
	 * Evaluates where the atlas.xml comes from. There are 3 options:
	 * <ul>
	 * <li>* jar://aufCD.jar!/atlas.xml => From CD, no JWS download needed,
	 * returns <code>false</code></li>
	 * <li>* jar://http://asdasasda/online/av.jar!/atlas.xml => From the
	 * Internet, JWS download nötig, returns <code>true</code></li>
	 * <li>* file://mein/verz/atlas.xml => Aus lokal dir, kein JWS download
	 * nötig, returns <code>false</code></li>
	 * </ul>
	 * The result is cached in {@value #isAtlasDataFromJWS}.
	 */
	public static boolean isAtlasDataFromJWS(AtlasConfig ac) {

		if (isAtlasDataFromJWS == null) {
			URL atlasXmlRes = ac.getResource(AtlasConfig.ATLASDATA_DIRNAME
					+ "/" + AtlasConfig.ATLAS_XML_FILENAME);

			LOGGER.info("resourceAsUrl " + atlasXmlRes);

			String protocol = atlasXmlRes.getProtocol();

			if (protocol.startsWith("file")) {
				LOGGER.info("resourceAsUrl " + atlasXmlRes + " is not from JWS");
				return isAtlasDataFromJWS = Boolean.FALSE;
			}

			if (protocol.startsWith("jar")) {
				LOGGER.info("resourceAsUrl starts with JAR");

				// First check for JWS and eventually download un-cached JARs
				if (atlasXmlRes.toString().contains("http")) {
					LOGGER.info("resourceAsUrl and contains with HTTP => we need to get it via JWS if it is not cached....");
					LOGGER.debug("the data comes from JARs/URLs, adding it to resman...");
					// moved up one block
					ac.getResLoMan().addResourceLoader(
							AtlasJWSCachedResourceLoader.getInstance());

					return isAtlasDataFromJWS = true;
				} else
					return isAtlasDataFromJWS = false;

			} else {
				throw new AtlasFatalException(
						"Failed to see data! atlas.xml comes from "
								+ atlasXmlRes.toString());
			}
		}

		return isAtlasDataFromJWS;
	}

}
