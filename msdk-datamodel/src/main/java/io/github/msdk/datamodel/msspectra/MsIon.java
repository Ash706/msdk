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

package io.github.msdk.datamodel.msspectra;

/**
 * A single ion in a mass spectrum.
 */
public interface MsIon {

    /**
     * Returns the ion's m/z value
     */
    double getMz();

    /**
     * Returns the ion's intensity value
     */
    float getIntensity();

}
