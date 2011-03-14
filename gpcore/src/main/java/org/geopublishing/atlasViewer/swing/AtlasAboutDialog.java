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
/** 
 Copyright 2009 Stefan Alfons Tzeggai 

 atlas-framework - This file is part of the Atlas Framework

 This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA

 Diese Bibliothek ist freie Software; Sie dürfen sie unter den Bedingungen der GNU Lesser General Public License, wie von der Free Software Foundation veröffentlicht, weiterverteilen und/oder modifizieren; entweder gemäß Version 2.1 der Lizenz oder (nach Ihrer Option) jeder späteren Version.
 Diese Bibliothek wird in der Hoffnung weiterverbreitet, daß sie nützlich sein wird, jedoch OHNE IRGENDEINE GARANTIE, auch ohne die implizierte Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Mehr Details finden Sie in der GNU Lesser General Public License.
 Sie sollten eine Kopie der GNU Lesser General Public License zusammen mit dieser Bibliothek erhalten haben; falls nicht, schreiben Sie an die Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA.
 **/

package org.geopublishing.atlasViewer.swing;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;

import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.AtlasConfig;

import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.versionnumber.ReleaseUtil;

/**
 * A Dialog showing an about message that can be editoed for an atlas.
 * */
public class AtlasAboutDialog extends JDialog {

	private final AtlasConfig atlasConfig;

	/** Creates new form AtlasAboutDialog2 */
	public AtlasAboutDialog(java.awt.Frame parent, boolean modal,
			final AtlasConfig atlasConfig) {
		super(parent, modal);
		this.atlasConfig = atlasConfig;
		try {
			initComponents();
			if (atlasConfig.getIconURL() != null)
				jLabel2.setIcon(new ImageIcon(atlasConfig.getIconURL()));
			jLabel2.setText(null);
			jLabel3.setText("<html><h1>" + atlasConfig.getTitle()
					+ "</h1><br/><font size='-2' color='gray'>AtlasViewer "
					+ ReleaseUtil.getVersionInfo(AVUtil.class)
					+ "</font></html>");
			jButton1.setText(AtlasViewerGUI.R("HtmlBrowserWindow.button.close"));
			setTitle(AtlasViewerGUI.R("AtlasViewer.HelpMenu.About",
					atlasConfig.getTitle()));
//MS: now done in HTMLInfoJPane
//			html.setContentType("text/html");
//          html.setEditable(false);
			html.getComponent().setPreferredSize(new Dimension(450, 300));

			Thread.sleep(300);

			pack();

			SwingUtil.centerFrameOnScreen(this);
		} catch (Exception e) {
			ExceptionDialog.show(this, e);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		top = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jPanel1 = new javax.swing.JPanel();
		jButton1 = new javax.swing.JButton();
		html = AVUtil.createHTMLInfoPane(atlasConfig.getAboutHTMLURL(), atlasConfig);

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		top.setLayout(new java.awt.BorderLayout());

		jLabel2.setText("logo");
		jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5,
				5));
		jLabel2.setIconTextGap(0);
		top.add(jLabel2, java.awt.BorderLayout.LINE_END);

		jLabel3.setText("title");
		top.add(jLabel3, java.awt.BorderLayout.CENTER);

		getContentPane().add(top, java.awt.BorderLayout.PAGE_START);

		jButton1.setText("jButton1");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
				jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												194, Short.MAX_VALUE)
										.addComponent(jButton1)
										.addContainerGap()));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jButton1))
										.addContainerGap()));

		getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

		JComponent htmlComponent = html.getComponent();
		if ( !html.hasScrollPane() ) {
		  javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
		  jScrollPane1.setViewportView(html.getComponent());
		  htmlComponent = jScrollPane1;
		}
		getContentPane().add(htmlComponent, java.awt.BorderLayout.CENTER);

		pack();
	}// </editor-fold>

	// GEN-END:initComponents

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				AtlasAboutDialog dialog = new AtlasAboutDialog(
						new javax.swing.JFrame(), true, null);
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private HTMLInfoPaneInterface html;
	private javax.swing.JButton jButton1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel top;
	// End of variables declaration//GEN-END:variables

}
