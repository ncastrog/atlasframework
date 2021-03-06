/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.datapool;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.gui.internal.GPDialogManager;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geopublishing.geopublisher.swing.GpSwingUtil;

import de.schmitzm.i18n.I18NUtil;
import de.schmitzm.jfree.chart.style.ChartStyle;

public class DataPoolEditHTMLAction extends AbstractAction {

	static final Logger LOGGER = Logger.getLogger(DataPoolDeleteAction.class);

	private final DataPoolJTable dpTable;

	public DataPoolEditHTMLAction(DataPoolJTable dpTable) {
		super(GeopublisherGUI.R("DataPoolWindow_Action_EditDPEHTML_label"));

		this.dpTable = dpTable;
	}

	@Override
	/*
	 * Opens a dialog to edit the HTML for this layer
	 */
	public void actionPerformed(ActionEvent e) {

		// Determine which DPEntry is selected
		if (dpTable.getSelectedRow() == -1)
			return;
		DataPool dataPool = dpTable.getDataPool();
		DpEntry dpe = dataPool.get(dpTable.convertRowIndexToModel(dpTable
				.getSelectedRow()));

		if (!(dpe instanceof DpLayer))
			return;

		final DpLayer<?, ChartStyle> dpl = (DpLayer<?, ChartStyle>) dpe;
		java.util.List<File> infoFiles = GpSwingUtil.getHTMLFilesFor(dpl);

		java.util.List<String> tabTitles = new ArrayList<String>();
		AtlasConfigEditable ace = dpTable.getAce();
		for (String l : ace.getLanguages()) {
			tabTitles.add(GeopublisherGUI.R(
					"DPLayer.HTMLInfo.LanguageTabTitle", I18NUtil
							.getFirstLocaleForLang(l).getDisplayLanguage()));
		}

		// GpSwingUtil.openHTMLEditors(dpTable, ace, infoFiles, tabTitles,
		// GeopublisherGUI.R("EditLayerHTML.Dialog.Title", dpl.getTitle()
		// .toString()));

		String key = GpSwingUtil.openHTMLEditorsKey(infoFiles);
		Window instanceFor = GPDialogManager.dm_HtmlEditor.getInstanceFor(key,
				dpTable, ace, infoFiles, tabTitles, dpl.getTitle().toString());

		// TODO In case this HTML editor is beeing tried to be opened twice,
		// these listeneras are added twice.!
		instanceFor.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				/**
				 * Try to reset a few cached values for the TODO nicer! //
				 * Expect that the number of HTML files, and with it the QM have
				 * changed. // TODO replace this events?! Should uncache fire
				 * the events?
				 */
				dpl.uncache();
				((AbstractTableModel) dpTable.getModel())
						.fireTableDataChanged();
				dpTable.setTableCellRenderers();
			}
		});

	}

}
