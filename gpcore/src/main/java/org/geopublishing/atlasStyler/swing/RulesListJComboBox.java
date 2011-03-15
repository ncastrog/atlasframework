package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.geopublishing.atlasStyler.AbstractRulesList.RulesListType;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.opengis.feature.type.FeatureType;

import de.schmitzm.swing.SwingUtil;

/**
 * A {@link JComboBox} which offers a selection of RulesList. Only RLs that
 * "make sense" for the given {@link FeatureType} are presented.
 * 
 */
public class RulesListJComboBox extends JComboBox {

	private final DefaultComboBoxModel model;

	public RulesListJComboBox(AtlasStyler as) {

		SwingUtil.addMouseWheelForCombobox(this, false);
		RulesListType[] valuesFor = RulesListType.valuesFor(as);
		Arrays.sort(valuesFor);
		model = new DefaultComboBoxModel(valuesFor);
		setModel(model);

		setRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				JLabel proto = (JLabel) super.getListCellRendererComponent(
						list, value, index, isSelected, cellHasFocus);

				RulesListType rlt = (RulesListType) value;

				proto.setText("<html>" + rlt.getTitle() + "</html>");

				return proto;
			}
		});

	}

}
