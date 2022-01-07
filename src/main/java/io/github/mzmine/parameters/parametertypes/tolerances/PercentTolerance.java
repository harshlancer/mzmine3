/*
 *  Copyright 2006-2020 The MZmine Development Team
 *
 *  This file is part of MZmine.
 *
 *  MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 *  General Public License as published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version.
 *
 *  MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 *  Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with MZmine; if not,
 *  write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 *  USA
 */

package io.github.mzmine.parameters.parametertypes.tolerances;

import com.google.common.collect.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class to determine if two values lie within a specified tolerance region.
 *
 * @author https://github.com/steffenheu
 */
public class PercentTolerance {

  private final double tolerance;

  /**
   * @param percentTolerance The tolerance in percent. 0.1 = 10%, 1.5 = 150 %
   */
  public PercentTolerance(final double percentTolerance) {
    tolerance = percentTolerance;
  }

  public boolean matches(int base, int value) {
    return Double.compare(Math.abs(value - base) / (double) base, tolerance) <= 0;
  }

  public boolean matches(long base, long value) {
    return Double.compare(Math.abs(value - base) / (double) base, tolerance) <= 0;
  }

  public boolean matches(float base, float value) {
    return Double.compare(Math.abs(value - base) / base, tolerance) <= 0;
  }

  public boolean matches(double base, double value) {
    return Double.compare(Math.abs(value - base) / base, tolerance) <= 0;
  }

  /**
   * @param base  The base value of this range ("what it is supposed to be")
   * @param value The value to be checked.
   * @return true or false. false if any of the input type is null
   */
  public <T extends Number, V extends Number> boolean matches(@Nullable T base, @Nullable V value) {
    if (base == null || value == null) {
      return false;
    }
    return Double.compare(Math.abs(value.doubleValue() - base.doubleValue()) / base.doubleValue(),
        tolerance) <= 0;
  }

  public <T extends Number> Range<Double> getToleranceRange(@NotNull T value) {
    final double tol = value.doubleValue() * tolerance;
    return Range.closed(value.doubleValue() - tol, value.doubleValue() + tol);
  }

  public double getTolerance() {
    return tolerance;
  }
}
