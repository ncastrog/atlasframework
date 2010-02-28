/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.sld.classification;

import hep.aida.bin.DynamicBin1D;

import java.awt.Component;
import java.io.IOException;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.apache.log4j.Logger;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.lang.LangUtil;
import skrueger.AttributeMetadata;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.atlas.swing.AtlasSwingWorker;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.sld.classification.ClassificationChangeEvent.CHANGETYPES;

/**
 * A quantitative classification. The inveralls are defined by upper and lower
 * limits
 * 
 * 
 * @param <T>
 *            The type of the value field
 * 
 * @author stefan
 */
public class QuantitiesClassification extends FeatureClassification {

	protected Logger LOGGER = ASUtil.createLogger(this);

//	static DefaultComboBoxModel nClassesComboBoxModel;

	/**
	 * This CONSTANT is only used in the JCombobox. NORMALIZER_FIELD String is
	 * null, and in the SLD a "null"
	 */
	public static final String NORMALIZE_NULL_VALUE_IN_COMBOBOX = "-";

	@Override
	public void fireEvent(final ClassificationChangeEvent e) {

		if (isQuite()) {
			lastOpressedEvent = e;
			return;
		} else {
			lastOpressedEvent = null;
		}

		if (e.getType() == CHANGETYPES.START_NEW_STAT_CALCULATION
				&& getMethod() == METHOD.MANUAL)
			return;

		super.fireEvent(e);

		boolean calcNewStats = true;

		if (getMethod() == METHOD.MANUAL) {
			// Never calculate statistics (including new class breaks?!) when we
			// are manual. TODO Maybe that should be moved to the stuff after
			// getStats...
			calcNewStats = false;
		} else if (e.getType() == CHANGETYPES.START_NEW_STAT_CALCULATION) {
			calcNewStats = false;
		} else if (e.getType() == CHANGETYPES.CLASSES_CHG) {
			calcNewStats = false;
		}

		if ((calcNewStats) && (recalcAutomatically)) {
			LOGGER
					.debug("Starting to calculate new class-limits on another thread due to "
							+ e.getType().toString());
			calculateClassLimitsWithWorker();
		}
	}

	private String value_field_name;

	private String normalizer_field_name;

	private DefaultComboBoxModel valueAttribsComboBoxModel;

	private DefaultComboBoxModel normlizationAttribsComboBoxModel;

	/**
	 * Count of digits the (quantile) classes are rounded to. A negative value
	 * means round to digits BEFORE comma! If {@code null} (this is the
	 * default!) no round is performed.
	 */
	private Integer limitsDigits = null;

	/**
	 * If the classification contains 5 classes, then we have to save 5+1
	 * breaks.
	 */
	protected volatile TreeSet<Double> breaks = null;

	private DynamicBin1D stats = null;

	final String handle = "statisticsQuery";

	static final public int MAX_FEATURES_DEFAULT = 10000;

	public static final METHOD DEFAULT_METHOD = METHOD.QUANTILES;

	/**
	 * Different Methods to classify
	 */
	public enum METHOD {
		EI, MANUAL, QUANTILES;

		public String getDesc() {
			return AtlasStyler
					.R("QuantitiesClassifiction.Method.ComboboxEntry."
							+ toString());
		}

		public String getToolTip() {
			return AtlasStyler
					.R("QuantitiesClassifiction.Method.ComboboxEntry."
							+ toString() + ".TT");
		}

	}

	/** The type of classification that is used. Quantiles by default * */
	public METHOD classificationMethod = DEFAULT_METHOD;

	private int numClasses = 5;

	volatile private boolean cancelCalculation;

	volatile private AtlasSwingWorker<TreeSet<Double>> calculateStatisticsWorker;

	protected volatile TreeSet<Double> classLimits;

	private boolean recalcAutomatically = true;

	private Component owner;

	/**
	 * @param featureSource
	 *            The featuresource to use for the statistics
	 * @param layerFilter
	 *            The {@link Filter} that shall be applied whenever asking for
	 *            the {@link FeatureCollection}. <code>null</code> is not
	 *            allowed, use Filter.INCLUDE
	 * @param value_field_name
	 *            The column that is used for the classification
	 * @param normalizer_field_name
	 *            If null, no normalization will be used
	 */
	public QuantitiesClassification(Component owner,
			StyledFeaturesInterface<?> styledFeatures,
			final String value_field_name, final String normalizer_field_name) {
		super(styledFeatures);
		this.owner = owner;
		this.value_field_name = value_field_name;
		this.normalizer_field_name = normalizer_field_name;
	}

	/**
	 * @param featureSource
	 *            The featuresource to use for the statistics
	 * @param value_field_name
	 *            The column that is used for the classification
	 */
	public QuantitiesClassification(Component owner,
			final StyledFeaturesInterface<?> styledFeatures,
			final String value_field_name) {
		this(owner, styledFeatures, value_field_name, null);
	}

	/**
	 * @param featureSource
	 *            The featuresource to use for the statistics
	 */
	public QuantitiesClassification(Component owner,
			final StyledFeaturesInterface<?> styledFeatures) {
		this(owner, styledFeatures, null, null);
	}

	@Override
	public int getNumClasses() {
		// if (numClasses <= 0 && breaks != null) {
		// LOGGER
		// .debug("getNumCLasses() sets numClasses to ( breaks.size()-1 = "
		// + (breaks.size() - 1)
		// + " ) because would return "
		// + numClasses + " otherwise");
		// numClasses = breaks.size() - 1;
		// }

		// return breaks.size() - 1;
		return numClasses;
	}

	/**
	 * @return A {@link ComboBoxModel} that contains a list of class numbers.<br/>
	 *         When we supported SD as a classification METHOD long ago, this
	 *         retured something dependent on the {@link #classificationMethod}.
	 *         Not it always returns a list of numbers.
	 */
	public ComboBoxModel getClassificationParameterComboBoxModel() {
		
		DefaultComboBoxModel nClassesComboBoxModel = new DefaultComboBoxModel(
					new Integer[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });

		switch (classificationMethod) {
		case EI:
		case QUANTILES:
		default:
			nClassesComboBoxModel.setSelectedItem(numClasses);
			return nClassesComboBoxModel;

		}
	}

	/**
	 * Equal Interval Classification method divides a set of attribute values
	 * into groups that contain an equal range of values. This method better
	 * communicates with continuous set of data. The map designed by using equal
	 * interval classification is easy to accomplish and read . It however is
	 * not good for clustered data because you might get the map with many
	 * features in one or two classes and some classes with no features because
	 * of clustered data.
	 * 
	 * @return nClasses + 1 breaks
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public TreeSet<Double> getEqualIntervalLimits() throws IOException,
			InterruptedException {

		getStatistics();

		breaks = new TreeSet<Double>();
		final Double step = 100. / numClasses;
		final double max = stats.max();
		for (double i = 0; i < 100;) {
			final double percent = (i) * 0.01;
			final double equal = percent * max;
			breaks.add(equal);
			i = i + step;
		}
		breaks.add(max);
		breaks = roundLimits(breaks);

		return breaks;
	}

	/**
	 * Quantiles classification method distributes a set of values into groups
	 * that contain an equal number of values. This method places the same
	 * number of data values in each class and will never have empty classes or
	 * classes with too few or too many values. It is attractive in that this
	 * method always produces distinct map patterns.
	 * 
	 * @return nClasses + 1 breaks
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public TreeSet<Double> getQuantileLimits() throws IOException,
			InterruptedException {

		getStatistics();

		LOGGER.debug("getQuantileLimits numClasses ziel variable ist : "
				+ numClasses);

		breaks = new TreeSet<Double>();
		final Double step = 100. / new Double(numClasses);
		for (double i = 0; i < 100;) {
			final double percent = (i) * 0.01;
			final double quantile = stats.quantile(percent);
			breaks.add(quantile);
			i = i + step;
		}
		breaks.add(stats.max());
		breaks = roundLimits(breaks);

		// LOGGER.debug(breaks.size() + "  " + breaks);

		return breaks;
	}

	// ms-01.sn
	/**
	 * Rounds all elements of the {@link TreeSet} to the number of digits
	 * specified by {@link #limitsDigits}. The first break (start of the first
	 * interval) is rounded down and the last break (end of last interval) is
	 * rounded up, so that every possible value is still included in one
	 * interval.
	 * 
	 * @param breaksList
	 *            interval breaks
	 * @return a new {@link TreeSet}
	 */
	private TreeSet<Double> roundLimits(final TreeSet<Double> breaksList) {
		// No round -> use the original values
		if (limitsDigits == null)
			return breaksList;

		final TreeSet<Double> roundedBreaks = new TreeSet<Double>();
		for (final double value : breaksList) {
			int roundMode = 0; // normal round
			// begin of first interval must be rounded DOWN, so that all
			// values are included
			if (value == breaksList.first())
				roundMode = -1;
			// end of last interval must be rounded UP, so that all
			// values are included
			if (value == breaksList.last())
				roundMode = 1;

			// round value and put it into the new TreeSet
			roundedBreaks.add(LangUtil.round(value, limitsDigits, roundMode));
		}
		return roundedBreaks;
	}

	// ms-01.en

	/**
	 * This is where the magic happens. Here the attributes of the features are
	 * summarized in a {@link DynamicBin1D} class.
	 */
	synchronized public DynamicBin1D getStatistics()
			throws InterruptedException {

		cancelCalculation = false;

		if (value_field_name == null)
			throw new IllegalArgumentException("value field has to be set");
		if (normalizer_field_name == value_field_name)
			throw new RuntimeException(
					"value field and the normalizer field may not be equal.");

		if (stats == null) {
			//
			// /**
			// * Fires a START_CALCULATIONS event to inform listening JTables
			// etc.
			// * about the change *
			// */
			// SwingUtilities.invokeLater(new Runnable() {
			// public void run() {
			// // System.out.println("Fire STart Calculations");
			// fireEvent(new ClassificationChangeEvent(
			// CHANGETYPES.START_NEW_STAT_CALCULATION));
			// }
			// });

			FeatureCollection<SimpleFeatureType, SimpleFeature> features = getStyledFeatures()
					.getFeatureCollectionFiltered();
			
			// Forget about the count of NODATA values
			resetNoDataCount();

			final DynamicBin1D stats_local = new DynamicBin1D();

			// get the AttributeMetaData for the given attribute to filter
			// NODATA values
			final AttributeMetadata amd = getStyledFeatures()
					.getAttributeMetaDataMap().get(value_field_name);
			final AttributeMetadata amdNorm = getStyledFeatures()
					.getAttributeMetaDataMap().get(normalizer_field_name);

			// // Simulate a slow calculation
			// try {
			// Thread.sleep(40);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }

			/**
			 * Iterating over the values and inserting them into the statistics
			 */
			final FeatureIterator<SimpleFeature> iterator = features.features();
			try {
				Double numValue, valueNormDivider;
				while (iterator.hasNext()) {

					/**
					 * The calculation process has been stopped from external.
					 */
					if (cancelCalculation) {
						stats = null;
						throw new InterruptedException(
								"The statistics calculation has been externally interrupted by setting the 'cancelCalculation' flag.");
					}

					final SimpleFeature f = iterator.next();

					// Filter VALUE for NODATA
					final Object filtered = amd.fiterNodata(f
							.getAttribute(value_field_name));
					if (filtered == null) {
						increaseNoDataValue();
						continue;
					}

					numValue = ((Number) filtered).doubleValue();

					if (normalizer_field_name != null) {

						// Filter NORMALIZATION DIVIDER for NODATA
						Object filteredNorm = amdNorm.fiterNodata(f
								.getAttribute(normalizer_field_name));
						if (filteredNorm == null) {
							increaseNoDataValue();
							continue;
						}

						valueNormDivider = ((Number) filteredNorm)
								.doubleValue();
						if (valueNormDivider == 0) {
							// Even if it is not defined as a NODATA value,
							// division by null is not definied.
							increaseNoDataValue();
							continue;
						}

						numValue = numValue / valueNormDivider;
					}

					stats_local.add(numValue);

				}

				stats = stats_local;

			} finally {
				features.close(iterator);
			}
		}

		return stats;
	}

	/**
	 * Change the LocalName of the {@link Attribute} that shall be used for the
	 * values. <code>null</code> is not allowed.
	 * 
	 * @param value_field_name
	 *            {@link Double}.
	 */
	public void setValue_field_name(final String value_field_name) {
		// IllegalArgumentException("null is not a valid value field name");
		if ((value_field_name != null)
				&& (this.value_field_name != value_field_name)) {
			this.value_field_name = value_field_name;
			stats = null;

			if (normalizer_field_name == value_field_name) {
				normalizer_field_name = null;
			}

			fireEvent(new ClassificationChangeEvent(CHANGETYPES.VALUE_CHG));
		}
	}

	/**
	 * Change the LocalName of the {@link Attribute} that shall be used as a
	 * normalizer for the value {@link Attribute}. If <code>null</code> is
	 * passed, the value will not be normalized.
	 * 
	 * @param normalizer_field_name
	 *            {@link Double}.
	 */
	public void setNormalizer_field_name(String normalizer_field_name) {
		// This max actually be set to null!!
		if (this.normalizer_field_name != normalizer_field_name) {
			this.normalizer_field_name = normalizer_field_name;
			stats = null;

			// Das durfte sowieso nie passieren
			if (normalizer_field_name == value_field_name) {
				normalizer_field_name = null;
				throw new IllegalStateException(
						"Die GUI sollte nicht erlauben, dass VALUE und NORMALIZATION field gleich sind.");
			}

			fireEvent(new ClassificationChangeEvent(CHANGETYPES.NORM_CHG));
		}
	}

	/**
	 * Help the GC to clean up this object.
	 */
	public void dispose() {
		super.dispose();
		stats = null;
	}

	public METHOD getMethod() {
		return classificationMethod;
	}

	// ms-01.sn
	/**
	 * Setzs the number of digits the (quantile) class limits are rounded to. If
	 * set to {@code null} no round is performed.
	 * 
	 * @param digits
	 *            positive values means round to digits AFTER comma, negative
	 *            values means round to digits BEFORE comma
	 */
	public void setLimitsDigits(final Integer digits) {
		this.limitsDigits = digits;
	}

	/**
	 * Returns the number of digits the (quantile) class limits are rounded to.
	 * Positive values means round to digits AFTER comma, Negative values means
	 * round to digits BEFORE comma.
	 * 
	 * @return {@code null} if no round is performed
	 */
	public Integer getClassValueDigits() {
		return this.limitsDigits;
	}

	// ms-01.en

	public void setMethod(final METHOD newMethod) {
		if ((classificationMethod != null)
				&& (classificationMethod != newMethod)) {
			classificationMethod = newMethod;

			fireEvent(new ClassificationChangeEvent(CHANGETYPES.METHODS_CHG));
		}
	}

	public void setNumClasses(final Integer numClasses2) {
		if (numClasses2 != null && !numClasses2.equals(numClasses)) {
			numClasses = numClasses2;
			LOGGER.debug("QuanClassification set NumClasses to " + numClasses2
					+ " and fires event");
			fireEvent(new ClassificationChangeEvent(CHANGETYPES.NUM_CLASSES_CHG));
		}

	}

	/**
	 * Calculates the {@link TreeSet} of classLimits, blocking the thread.
	 */
	public TreeSet<Double> calculateClassLimitsBlocking() throws IOException,
			InterruptedException {
		/**
		 * Do we have all necessary information to calculate ClassLimits?
		 */
		if (getMethod() == METHOD.MANUAL) {
			LOGGER
					.warn("calculateClassLimitsBlocking has been called but METHOD == MANUAL");
			// getStatistics();
			return getClassLimits();
		}
		if (value_field_name == null)
			throw new IllegalStateException("valueFieldName has to be set");
		if (getMethod() == null)
			throw new IllegalStateException("method has to be set");

		switch (classificationMethod) {
		case EI:
			return classLimits = getEqualIntervalLimits();

		case QUANTILES:
		default:
			return classLimits = getQuantileLimits();
		}
	}

	public void calculateClassLimitsWithWorker() {
		classLimits = new TreeSet<Double>();

		/**
		 * Do we have all necessary information to calculate ClassLimits?
		 */
		if (value_field_name == null)
			return;

		/**
		 * If there is another thread running, cancel it first. But remember,
		 * that swing-workers may not be reused!
		 */
		if (calculateStatisticsWorker != null
				&& !calculateStatisticsWorker.isDone()) {
			LOGGER.debug("Cancelling calculation on another thread");
			setCancelCalculation(true);
			calculateStatisticsWorker.cancel(true);
		}

		AtlasStatusDialog statusDialog = new AtlasStatusDialog(owner);

		calculateStatisticsWorker = new AtlasSwingWorker<TreeSet<Double>>(
				statusDialog) {

			@Override
			protected TreeSet<Double> doInBackground() throws IOException,
					InterruptedException {
				return calculateClassLimitsBlocking();
			}

		};

		pushQuite();
		TreeSet<Double> newLimits;
		try {
			newLimits = calculateStatisticsWorker.executeModal();
			setClassLimits(newLimits);
			popQuite();
		} catch (InterruptedException e) {
			setQuite(stackQuites.pop());
		} catch (CancellationException e) {
			setQuite(stackQuites.pop());
		} catch (ExecutionException exception) {
//			ExceptionMonitor.show(owner, exception);
			setQuite(stackQuites.pop());
		} finally {
		}

	}

	public TreeSet<Double> getClassLimits() {
		return classLimits;
	}

	public void setCancelCalculation(final boolean cancelCalculation) {
		this.cancelCalculation = cancelCalculation;

	}

	/**
	 * Return a cached {@link ComboBoxModel} that present all available
	 * attributes. Its connected to the
	 * {@link #createNormalizationFieldsComboBoxModel()}
	 */
	public ComboBoxModel getValueFieldsComboBoxModel() {
		if (valueAttribsComboBoxModel == null)
			valueAttribsComboBoxModel = new DefaultComboBoxModel(FeatureUtil
					.getNumericalFieldNames(getStyledFeatures().getSchema(),
							false).toArray());
		return valueAttribsComboBoxModel;
	}

	/**
	 * Return a {@link ComboBoxModel} that present all available attributes.
	 * That excludes the attribute selected in
	 * {@link #getValueFieldsComboBoxModel()}.
	 */
	public ComboBoxModel createNormalizationFieldsComboBoxModel() {
		normlizationAttribsComboBoxModel = new DefaultComboBoxModel();
		normlizationAttribsComboBoxModel
				.addElement(NORMALIZE_NULL_VALUE_IN_COMBOBOX);
		normlizationAttribsComboBoxModel
				.setSelectedItem(NORMALIZE_NULL_VALUE_IN_COMBOBOX);
		for (final String fn : FeatureUtil.getNumericalFieldNames(
				getStyledFeatures().getSchema(), false)) {
			if (fn != valueAttribsComboBoxModel.getSelectedItem())
				normlizationAttribsComboBoxModel.addElement(fn);
			else {
				// System.out.println("Omittet field" + fn);
			}
		}
		return normlizationAttribsComboBoxModel;
	}

	/**
	 * @return the name of the {@link Attribute} used for the value. It may
	 *         additionally be normalized if #
	 */
	public String getValue_field_name() {
		return value_field_name;
	}

	/**
	 * @return the name of the {@link Attribute} used for the normalization of
	 *         the value. e.g. value = value field / normalization field
	 */
	public String getNormalizer_field_name() {
		return normalizer_field_name;
	}

	/**
	 * Determine if you want the classification to be recalculated whenever if
	 * makes sense automatically. Events for START calculation and
	 * NEW_STATS_AVAIL will be fired if set to <code>true</code> Default is
	 * <code>true</code>. Switching it to false can be usefull for tests. If set
	 * to <false> you have to call {@link #calculateClassLimitsBlocking()} to
	 * update the statistics.
	 */
	public void setRecalcAutomatically(final boolean b) {
		recalcAutomatically = b;
	}

	/**
	 * Determine if you want the classification to be recalculated whenever if
	 * makes sense automatically. Events for START calculation and
	 * NEW_STATS_AVAIL will be fired.
	 */
	public boolean getRecalcAutomatically() {
		return recalcAutomatically;
	}

	public void setClassLimits(final TreeSet<Double> classLimits2) {
		this.classLimits = classLimits2;
		this.numClasses = classLimits2.size() - 1;
		fireEvent(new ClassificationChangeEvent(CHANGETYPES.CLASSES_CHG));
	}

	/**
	 * Will trigger recalculating the statistics including firing events
	 */
	public void onFilterChanged() {
		stats = null;
		if (getMethod() == METHOD.MANUAL) {
			fireEvent(new ClassificationChangeEvent(CHANGETYPES.CLASSES_CHG));
		} else
			calculateClassLimitsWithWorker();
	}

}
