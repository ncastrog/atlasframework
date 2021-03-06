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
package org.geopublishing.atlasStyler.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.AtlasDialog;
import de.schmitzm.swing.CancelButton;
import de.schmitzm.swing.ColorButton;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.OkButton;
import de.schmitzm.swing.SwingUtil;

public class SymbolSelectorGUI extends AtlasDialog {
	protected Logger LOGGER = LangUtil.createLogger(this);

	public static final String PROPERTY_CANCEL_CHANGES = "CANCEL CHANGES";

	// public static final String PROPERTY_PREVIEW_UPDATE = "PREVIEW UPDATE"; //

	public static final String PROPERTY_CLOSED = "CLOSED";

	private static final String DIALOG_TITLE = ASUtil.R("SymbolSelector.Title");

	private static final Dimension DEFAULT_PREVIEW_ICON_SIZE = new Dimension(
			80, 60);

	private JPanel jContentPane = null;

	private JPanel jPanelPreview = null;

	private final JLabel jLabelPreviewIcon = new JLabel("");

	private JPanel jPanelOptions = null;

	private ColorButton jButtonColor = null;

	private JLabel jLabelColor = null;

	private JLabel jLabelSize = null;

	private JComboBox jComboBoxSize = null;

	private JLabel jLabelRotation = null;

	private JComboBox jComboBoxRotation = null;

	private JButton jButtonOK = null;

	private JButton jButtonCancel = null;

	private JButton jButtonProperties = null;

	private JButton jButtonSave = null;

	private JButton jButtonReset = null;

	private JPanel jPanel = null;

	private final SingleRuleList<?> singleSymbolRuleList;

	private JTabbedPane jTabbedPane = null;

	private JScrollPaneSymbolsLocal symbolsLocal;

	private JScrollPaneSymbolsOnline symbolsOnline;

	/**
	 * We have to remember the last loaded RulesList, so we can use the reset
	 * button. *
	 */
	private SingleRuleList<?> lastSelectedRuleList;

	private final RuleChangeListener listenToRuleChangesAndUpdatePreview = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {
			jLabelPreviewIcon.setIcon(new ImageIcon(singleSymbolRuleList
					.getImage(DEFAULT_PREVIEW_ICON_SIZE)));
		}

	};

	/**
	 * @param title
	 *            May be <code>null</code> to use default title
	 */
	public SymbolSelectorGUI(Component owner, AtlasStylerVector asv,
			String title, final SingleRuleList singleSymbolRuleList) {
		super(owner);
		if (asv == null)
			throw new IllegalStateException("asv may not be null here!");
		this.asv = asv;
		if (title != null)
			setTitle(title);
		else
			setTitle(DIALOG_TITLE);

		this.singleSymbolRuleList = singleSymbolRuleList;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});

		pack();

		SwingUtil.setRelativeFramePosition(this, getParent(),
				SwingUtil.BOUNDS_OUTER, SwingUtil.NORTHEAST);

	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new MigLayout("flowy, wrap 1"));
			jContentPane.add(getJTabbedPane(), "growy, growx 1, w 300:350:400");

			JPanel rightSide = new JPanel(new MigLayout("wrap 1, fillx"));
			rightSide.add(getJPanelPreview(), "growx, center");
			rightSide.add(getJPanelOptions(), "growx");
			rightSide.add(getJPanelButtons(), "growx");

			jContentPane.add(rightSide, "growx 2");

		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelPreview() {
		if (jPanelPreview == null) {
			jPanelPreview = new JPanel(new MigLayout("align center, w 100"),
					ASUtil.R("SymbolSelector.Preview.BorderTitle"));
			jLabelPreviewIcon.setOpaque(false);
			jPanelPreview.add(jLabelPreviewIcon, "align center");
			singleSymbolRuleList
					.addListener(listenToRuleChangesAndUpdatePreview);
			jLabelPreviewIcon.setIcon(new ImageIcon(singleSymbolRuleList
					.getImage(DEFAULT_PREVIEW_ICON_SIZE)));
		}
		return jPanelPreview;
	}

	/**
	 */
	private JPanel getJPanelOptions() {
		if (jPanelOptions == null) {
			jLabelRotation = new JLabel(ASUtil.R("RotationLabel"));
			jLabelSize = new JLabel(ASUtil.R("SizeLabel"));
			jLabelColor = new JLabel(ASUtil.R("ColorLabel"));
			jPanelOptions = new JPanel(new MigLayout("wrap 2"));
			jPanelOptions.setBorder(BorderFactory
					.createEmptyBorder(5, 15, 5, 5));
			jPanelOptions.add(jLabelColor, "sgx1");
			jPanelOptions.add(getJButtonColor(), "sgx2");
			jPanelOptions.add(jLabelSize, "sgx1");
			jPanelOptions.add(getJComboBoxSize(), "sgx2");

			if (singleSymbolRuleList.getRotation() != null) {
				jPanelOptions.add(jLabelRotation, "sgx1");
				jPanelOptions.add(getJComboBoxAngle(), "sgx2");
			}
		}
		return jPanelOptions;
	}

	/**
	 * This method initializes a {@link JButton} that allows to "Intelligently"
	 * change a color in the {@link SingleRuleList}
	 * 
	 * @return javax.swing.JComboBox
	 */
	private ColorButton getJButtonColor() {
		if (jButtonColor == null) {
			jButtonColor = new ColorButton();

			jButtonColor.setAction(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Color color = null;

					color = singleSymbolRuleList.getColor();

					Color newColor = AVSwingUtil.showColorChooser(
							SymbolSelectorGUI.this, AtlasStylerVector
									.R("SymbolSelector.Preview.ChooseColor"),
							color);

					if (newColor != null) {
						singleSymbolRuleList.setColor(newColor);

					}

				}

			});

			jButtonColor.setColor(singleSymbolRuleList.getColor());

			singleSymbolRuleList
					.addListener(colorButtonReactToStyleChangesListener);

			boolean enabled = singleSymbolRuleList.hasColor();
			jButtonColor.setEnabled(enabled);
			jLabelColor.setEnabled(enabled);
		}
		return jButtonColor;
	}

	/** Listen for RuleChanges to update the button */
	RuleChangeListener colorButtonReactToStyleChangesListener = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {
			boolean enabled = singleSymbolRuleList.hasColor();

			if (enabled) {
				// System.out.println("Should update the button");
				jButtonColor.setColor(singleSymbolRuleList.getColor());
			}

			jButtonColor.setEnabled(enabled);
			jLabelColor.setEnabled(enabled);
		}

	};

	/**
	 * This method initializes jComboBox2
	 * 
	 * @return javax.swing.JComboBox
	 */
	protected JComboBox getJComboBoxSize() {
		if (jComboBoxSize == null) {
			jComboBoxSize = new JComboBox();

			if (FeatureUtil.getGeometryForm((singleSymbolRuleList
					.getGeometryDescriptor())) == GeometryForm.LINE) {
				/***************************************************************
				 * This is about Stroke WIDTH
				 */
				jComboBoxSize.setModel(new DefaultComboBoxModel(
						AbstractStyleEditGUI.WIDTH_VALUES));
			} else {
				/***************************************************************
				 * This is about Graphic Sizes
				 */
				jComboBoxSize.setModel(new DefaultComboBoxModel(
						AbstractStyleEditGUI.SIZE_VALUES));
			}

			ASUtil.selectOrInsert(jComboBoxSize,
					singleSymbolRuleList.getSizeBiggest());

			singleSymbolRuleList
					.addListener(sizeComboBoxReactsToStyleChangesListener);

			jComboBoxSize.setEnabled(singleSymbolRuleList.hasSize());
			jLabelSize.setEnabled(singleSymbolRuleList.hasSize());

			ASUtil.selectOrInsert(jComboBoxSize,
					singleSymbolRuleList.getSizeBiggest());
			// jComboBoxSize
			// .setSelectedItem(singleSymbolRuleList.getSizeBiggest());

			/** Change in the COmboBox * */
			jComboBoxSize.addItemListener(new ItemListener() {

				AtomicBoolean busy = new AtomicBoolean(false);
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED && busy.compareAndSet(false, true)) {
						Float size = (Float) jComboBoxSize.getSelectedItem();

						// System.out.println("do set from
						// "+singleSymbolRuleList.getSizeBiggest()+ " to
						// "+size);
						singleSymbolRuleList.setSizeBiggest(size);

					}

				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxSize);

		}
		return jComboBoxSize;
	}

	/** Listen for RuleChanges to update the button */
	RuleChangeListener sizeComboBoxReactsToStyleChangesListener = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {

			boolean enabled = singleSymbolRuleList.hasSize();

			if (enabled) {
				// LOGGER.info("Should update the size to
				// "+singleSymbolRuleList
				// .getSizeBiggest());
				// if (((DefaultComboBoxModel) jComboBoxSize.getModel())
				// .getIndexOf(singleSymbolRuleList.getSizeBiggest()) < 0) {
				// ((DefaultComboBoxModel) jComboBoxSize.getModel())
				// .addElement(singleSymbolRuleList.getSizeBiggest());
				// }
				ASUtil.selectOrInsert(jComboBoxSize,
						singleSymbolRuleList.getSizeBiggest());
				// jComboBoxSize.setSelectedItem(singleSymbolRuleList
				// .getSizeBiggest());
			}

			jComboBoxSize.setEnabled(enabled);
			jLabelSize.setEnabled(enabled);

			jComboBoxSize.setEnabled(singleSymbolRuleList.hasSize());
			jLabelSize.setEnabled(singleSymbolRuleList.hasSize());
		}

	};

	/**
	 * This method initializes jComboBox3
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxAngle() {
		if (jComboBoxRotation == null) {
			jComboBoxRotation = new JComboBox(new DefaultComboBoxModel(
					AbstractStyleEditGUI.ROTATION_VALUES));
			jComboBoxRotation
					.setRenderer(AbstractStyleEditGUI.ROTATION_VALUES_RENDERER);

			ASUtil.selectOrInsert(jComboBoxRotation,
					singleSymbolRuleList.getRotation());

			jComboBoxRotation.addItemListener(new ItemListener() {

				AtomicBoolean busy = new AtomicBoolean(false);
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED && busy.compareAndSet(false, true)) {
						Double size = (Double) jComboBoxRotation
								.getSelectedItem();

						singleSymbolRuleList.setRotation(size);
						singleSymbolRuleList.fireEvents(new RuleChangedEvent(
								"rotation changed", singleSymbolRuleList));
					}

				}

			});

			singleSymbolRuleList
					.addListener(rotationComboBoxReactsToStyleChangeListener);

			SwingUtil.addMouseWheelForCombobox(jComboBoxRotation);

			jComboBoxRotation.setEnabled(singleSymbolRuleList.hasRotation());
			jLabelRotation.setEnabled(singleSymbolRuleList.hasRotation());
		}
		return jComboBoxRotation;
	}

	RuleChangeListener rotationComboBoxReactsToStyleChangeListener = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {
			jComboBoxRotation.setEnabled(singleSymbolRuleList.hasRotation());
			jLabelRotation.setEnabled(singleSymbolRuleList.hasRotation());
			jComboBoxRotation.setSelectedItem(singleSymbolRuleList
					.getRotation());
		}

	};

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new OkButton(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					SymbolSelectorGUI.this.firePropertyChange(PROPERTY_CLOSED,
							null, null);
					setVisible(false);
					dispose();
				}

			});
		}
		return jButtonOK;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new CancelButton(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					cancel();
				}

			});
		}
		return jButtonCancel;
	}

	protected void cancel() {
		// TODO Make compliant with AtlasDialog

		SymbolSelectorGUI.this.firePropertyChange(PROPERTY_CANCEL_CHANGES,
				null, null);

		SymbolSelectorGUI.this.firePropertyChange(PROPERTY_CLOSED, null, null);
		setVisible(false);
		dispose();
	}

	private final AtlasStylerVector asv;

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonProperties() {
		if (jButtonProperties == null) {
			jButtonProperties = new JButton(new AbstractAction(
					ASUtil.R("SymbolSelector.EditSymbol")) {

				@Override
				public void actionPerformed(ActionEvent e) {

					/**
					 * Opens a modal dialog. TODO replace with DialogManager
					 * with RuleListe as a KEY
					 */
					SymbolEditorGUI symbolEditorGUI = new SymbolEditorGUI(
							SymbolSelectorGUI.this, asv, singleSymbolRuleList);
					symbolEditorGUI.setModal(true);
					symbolEditorGUI.setVisible(true);
				}

			});
		}
		return jButtonProperties;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonSave() {
		if (jButtonSave == null) {
			jButtonSave = new JButton(new AbstractAction(
					ASUtil.R("SymbolSelector.SaveToFile")) {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (singleSymbolRuleList.getSymbolizers().size() == 0) {
						JOptionPane
								.showMessageDialog(
										SymbolSelectorGUI.this,
										AtlasStylerVector
												.R("SymbolSelector.SaveToFileDialog.Error.NothingToSave"));
						return;
					}

					File dir = AtlasStylerVector
							.getSymbolsDir(singleSymbolRuleList
									.getGeometryForm());
					JFileChooser chooser = new JFileChooser(dir);
					chooser.setDialogTitle(AtlasStylerVector
							.R("SymbolSelector.SaveToFileDialog.Title"));
					chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					chooser.addChoosableFileFilter(new FileNameExtensionFilter(
							"SLD", "sld"));

					String styleName = singleSymbolRuleList.getStyleName();
					if (styleName != null)
						chooser.setSelectedFile(new File(dir, styleName));

					int i = chooser.showSaveDialog(SymbolSelectorGUI.this);
					if (i == JFileChooser.APPROVE_OPTION) {
						try {
							File selectedFile = chooser.getSelectedFile();
							if (selectedFile == null)
								return;

							if ((!selectedFile.getName().endsWith("sld"))
									&& (!selectedFile.getName().endsWith("SLD"))) {
								selectedFile = new File(selectedFile
										.getParentFile(), selectedFile
										.getName() + ".sld");
							}

							/*******************************************
							 * Ask the user to enter AuthorName and Description
							 */
							String creatorName = singleSymbolRuleList
									.getStyleTitle();

							if (creatorName == null || creatorName == "") {
								LOGGER.debug("creatorName is empty");
								creatorName = System.getProperty("user.name");
							}

							String newerAuthor = ASUtil.askForString(
									SymbolSelectorGUI.this,
									creatorName,
									AtlasStylerVector
											.R("SymbolSelector.SaveToFileDialog.Author"));
							if (newerAuthor == null)
								return;
							singleSymbolRuleList.setStyleTitle(newerAuthor);

							String newerDesc = ASUtil
									.askForString(
											SymbolSelectorGUI.this,
											singleSymbolRuleList
													.getStyleAbstract(),
											AtlasStylerVector
													.R("SymbolSelector.SaveToFileDialog.Desc"));
							if (newerDesc == null)
								return;
							singleSymbolRuleList.setStyleAbstract(newerDesc);

							singleSymbolRuleList.saveSymbolToFile(selectedFile);
						} catch (Exception e1) {
							LOGGER.error("Error saving symbol " + styleName, e1);
							JOptionPane.showMessageDialog(
									SymbolSelectorGUI.this,
									AtlasStylerVector
											.R("SymbolSelector.SaveToFileDialog.Error.FailedToSave")
											+ "\n" + e1.getLocalizedMessage());
						}

						symbolsLocal.rescan(true);
					}
				}

			});
			jButtonSave.setToolTipText(AtlasStylerVector
					.R("SymbolSelector.SaveToFile.TT"));
		}
		return jButtonSave;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonReset() {
		if (jButtonReset == null) {
			jButtonReset = new JButton();
			jButtonReset.setText(AtlasStylerVector.R("SymbolSelector.Reset"));
			jButtonReset.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (lastSelectedRuleList == null
							|| singleSymbolRuleList == null)
						return;

					lastSelectedRuleList.copyTo(singleSymbolRuleList);

				}

			});

			jButtonReset.setEnabled(false);
			jButtonReset.setToolTipText(AtlasStylerVector
					.R("SymbolSelector.Reset.TT"));
		}
		return jButtonReset;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelButtons() {
		if (jPanel == null) {
			jPanel = new JPanel(new MigLayout("wrap 1, fillx"));
			jPanel.add(getJButtonProperties(), "grow");
			jPanel.add(getJButtonSave(), "split 2, align left");
			jPanel.add(getJButtonReset(), "align right");
			jPanel.add(getJButtonOK(), "gapy unrel, split 2, bottom, tag ok");
			jPanel.add(getJButtonCancel(), "gapy unrel, tag cancel");
		}
		return jPanel;
	}

	/**
	 * This method initializes jTabbedPane
	 * 
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			/**
			 * All JScrollPaneSymbols fire PropertyChangeEvents when a symbol
			 * has been selected
			 */
			PropertyChangeListener listener = new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals(
							JScrollPaneSymbols.PROPERTY_SYMBOL_SELECTED)) {

						/**
						 * Nasty bug was here: The old RuleChangeListeners have
						 * to be preserved
						 */

						lastSelectedRuleList = (SingleRuleList) evt
								.getNewValue();

						lastSelectedRuleList.copyTo(singleSymbolRuleList);

					}
				}
			};

			/**
			 * Local Symbols
			 */
			{
				symbolsLocal = new JScrollPaneSymbolsLocal(
						singleSymbolRuleList.getGeometryForm());
				symbolsLocal.addPropertyChangeListener(listener);

				JPanel symbolsLocalPanel = new JPanel(new MigLayout(
						"wrap 1, inset 0"));
				if (symbolsLocal.getToolTip() != null)
					symbolsLocalPanel.add(
							new JLabel("<html>" + symbolsLocal.getToolTip()
									+ "</html>"), "sgx");
				symbolsLocalPanel.add(symbolsLocal, "sgx, grow, h :300:");

				jTabbedPane.addTab(symbolsLocal.getDesc(),
						symbolsLocal.getIcon(), symbolsLocalPanel,
						symbolsLocal.getToolTip());
			}

			/**
			 * Online Symbols
			 */
			{
				symbolsOnline = new JScrollPaneSymbolsOnline(
						singleSymbolRuleList.getGeometryDescriptor());
				symbolsOnline.addPropertyChangeListener(listener);

				JPanel symbolsOnlinePanel = new JPanel(new MigLayout(
						"wrap 1, inset 0"));
				if (symbolsOnline.getToolTip() != null)
					symbolsOnlinePanel.add(
							new JLabel("<html>" + symbolsOnline.getToolTip()
									+ "</html>"), "sgx");
				symbolsOnlinePanel.add(symbolsOnline, "sgx, grow, h :300:");

				jTabbedPane.addTab(symbolsOnline.getDesc(),
						symbolsOnline.getIcon(), symbolsOnlinePanel,
						symbolsOnline.getToolTip());
			}

		}
		return jTabbedPane;
	}

}
