/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.spectra.splash;

import javax.annotation.Nonnull;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;

import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.util.MsSpectrumUtil;

/**
 * the reference implementation of the Spectral Hash Key
 */
public class SplashCalculationAlgorithm {

    private static final int BINS = 10;
    private static final int BIN_SIZE = 100;

    private static final char[] INTENSITY_MAP = new char[] { '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z' };

    private static final int FINAL_SCALE_FACTOR = 35;

    /**
     * how to scale the spectrum
     */
    private static final @Nonnull Float scalingOfRelativeIntensity = 100f;

    /**
     * how should ions in the string representation be separated
     */
    private static final String ION_SEPERATOR = " ";

    /**
     * how many character should be in the spectrum block. Basically this
     * reduces the SHA256 code down to a fixed length of N characater
     */
    private static final int maxCharactersForSpectrumBlockTruncation = 20;

    /**
     * Fixed precission of masses
     */
    private static final int fixedPrecissionOfMasses = 6;

    /**
     * factor to scale m/z floating point values
     */
    private static final long MZ_PRECISION_FACTOR = (long) Math.pow(10,
            fixedPrecissionOfMasses);

    /**
     * Fixed precission of intensites
     */
    private static final int fixedPrecissionOfIntensities = 0;

    /**
     * factor to scale m/z floating point values
     */
    private static final long INTENSITY_PRECISION_FACTOR = (long) Math.pow(10,
            fixedPrecissionOfIntensities);

    /**
     * Correction factor to avoid floating point issues between implementations
     * and processor architectures
     */
    private static final double EPS_CORRECTION = 1.0e-7;

    /**
     * formats a m/z value to our defined fixedPrecissionOfMasses
     *
     * @param value
     * @return
     */
    private static String formatMZ(double value) {
        return String.format("%d",
                (long) ((value + EPS_CORRECTION) * MZ_PRECISION_FACTOR));
    }

    /**
     * formats an intensity value to our defined fixedPrecissionOfIntensites
     *
     * @param value
     * @return
     */
    private static String formatIntensity(double value) {
        return String.format("%d",
                (long) ((value + EPS_CORRECTION) * INTENSITY_PRECISION_FACTOR));
    }

    /**
     * encodes the actual spectrum
     *
     * @param spectrum
     * @return
     */
    private static String encodeSpectrum(double mzValues[], float intValues[],
            int size) {

        StringBuilder buffer = new StringBuilder();

        // build the first string
        for (int i = 0; i < size; i++) {
            buffer.append(formatMZ(mzValues[i]));
            buffer.append(":");
            buffer.append(formatIntensity(intValues[i]));

            // add our separator
            if (i < size - 1) {
                buffer.append(ION_SEPERATOR);
            }
        }

        // notify observers in case they want to know about progress of the
        // hashing
        String block = buffer.toString();
        String hash = DigestUtils.sha256Hex(block).substring(0,
                maxCharactersForSpectrumBlockTruncation);
        return hash;
    }

    /**
     * calculates our spectral hash
     *
     * @param spectrum
     *            a {@link io.github.msdk.datamodel.msspectra.MsSpectrum}
     *            object.
     * @return a {@link java.lang.String} object.
     */
    public static @Nonnull String calculateSplash(
            @Nonnull MsSpectrum spectrum) {

        return calculateSplash(spectrum.getMzValues(),
                spectrum.getIntensityValues(),
                spectrum.getNumberOfDataPoints());
    }

    /**
     * <p>
     * calculateSplash.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     * @param mzValues
     *            an array of double.
     * @param intValues
     *            an array of float.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    public static @Nonnull String calculateSplash(@Nonnull double mzValues[],
            @Nonnull float intValues[], @Nonnull Integer size) {

        // Parameter check
        Preconditions.checkNotNull(mzValues);
        Preconditions.checkNotNull(intValues);
        Preconditions.checkNotNull(size);

        // convert the spectrum to relative values
        float relativeIntensities[] = intValues.clone();

        MsSpectrumUtil.normalizeIntensity(relativeIntensities, size,
                scalingOfRelativeIntensity);

        StringBuffer buffer = new StringBuffer();

        // first block
        buffer.append(buildFirstBlock());
        buffer.append("-");

        // second block
        buffer.append(
                calculateHistogramBlock(mzValues, relativeIntensities, size));
        buffer.append("-");

        // third block
        buffer.append(encodeSpectrum(mzValues, relativeIntensities, size));

        return buffer.toString();
    }

    private static String buildFirstBlock() {
        String splash = "splash" + '1' + getVersion();
        return splash;
    }

    /**
     * @return
     */
    private static char getVersion() {
        return '0';
    }

    /**
     * Calculates a spectral histogram using the following steps: 1. Bin
     * spectrum into a histogram based on BIN_SIZE, extending the histogram size
     * as needed to accommodate large m/z values 2. Normalize the histogram,
     * scaling to INITIAL_SCALE_FACTOR 3. Wrap the histogram by summing
     * normalized intensities to reduce the histogram to BINS bins 4. Normalize
     * the reduced histogram, scaling to FINAL_SCALE_FACTOR 5. Convert/truncate
     * each intensity value to a 2-digit integer value, concatenated into the
     * final string histogram representation
     *
     * @param spectrum
     * @return histogram
     */
    private static String calculateHistogramBlock(double mzValues[],
            float intValues[], int size) {

        double binnedIons[] = new double[BINS];

        // Bin ions
        for (int i = 0; i < size; i++) {
            int index = ((int) (mzValues[i] / BIN_SIZE)) % BINS;
            binnedIons[index] += intValues[i];
        }

        // Normalize the histogram
        final double maxIntensity = Doubles.max(binnedIons);

        for (int i = 0; i < BINS; i++) {
            binnedIons[i] = EPS_CORRECTION
                    + FINAL_SCALE_FACTOR * binnedIons[i] / maxIntensity;
        }

        // Build histogram string
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < BINS; i++) {
            int bin = (int) (EPS_CORRECTION + binnedIons[i]);
            result.append(INTENSITY_MAP[bin]);
        }

        return result.toString();
    }
}
