/* 
 * (C) Copyright 2015 by MSDK Development Team
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

package io.github.msdk.datamodel.featuretables;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

/**
 * Represents the name of the feature table columns.
 */
public enum ColumnName {

    ID("Id", Integer.class),
    GROUPID("Group ID", Integer.class),
    MZ("m/z", Double.class),
    PPM("ppm", Double.class),
    RT("RT", ChromatographyInfo.class),
    RTSTART("RT Start", Double.class),
    RTEND("RT End", Double.class),
    DURATION("Duration", Double.class),
    AREA("Area", Double.class),
    HEIGHT("Height", Double.class),
    CHARGE("Charge", Integer.class),
    NUMBEROFDATAPOINTS("# Data Points", Integer.class),
    FWHM("FWHM", Double.class),
    TAILINGFACTOR("Tailing Factor", Double.class),
    ASYMMETRYFACTOR("Asymmetry Factor", Double.class),
    CHROMATOGRAM("Chromatogram", Chromatogram.class);

    @Nonnull private final String name;
    @Nonnull private final Class<?> dataTypeClass;

    ColumnName(@Nonnull String name, @Nonnull Class<?> dataTypeClass) {
        this.name = name;
        this.dataTypeClass = dataTypeClass;
    }

    /**
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * <p>Getter for the field <code>dataTypeClass</code>.</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    @Nonnull
    public Class<?> getDataTypeClass() {
        return dataTypeClass;
    }

}
