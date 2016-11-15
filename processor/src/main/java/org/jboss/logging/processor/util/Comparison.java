/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.logging.processor.util;

import java.util.Comparator;

/**
 * Date: 30.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class Comparison {

    public static final int EQUAL = 0;

    public static final int LESS = -1;

    public static final int GREATER = 1;

    private static final Comparison LESS_COMPARISON = new DeadComparison(-1);

    private static final Comparison GREATER_COMPARISON = new DeadComparison(1);

    private static final Comparison ALLOW_NULL_INSTANCE = new Comparison() {

        @Override
        @SuppressWarnings("unchecked")
        public Comparison compare(final Comparable left, final Comparable right) {
            final int result;
            if (left == null && right == null) {
                result = EQUAL;
            } else if (left == null) {
                result = LESS;
            } else if (right == null) {
                result = GREATER;
            } else {
                result = left.compareTo(right);
            }
            return super.checkResult(result);
        }

        @Override
        public <T> Comparison compare(final T left, final T right, final Comparator<T> comparator) {
            final int result;
            if (left == null && right == null) {
                result = EQUAL;
            } else if (left == null) {
                result = LESS;
            } else if (right == null) {
                result = GREATER;
            } else {
                result = comparator.compare(left, right);
            }
            return super.checkResult(result);
        }

        @Override
        public Comparison getInstance() {
            return ALLOW_NULL_INSTANCE;
        }
    };

    private static final Comparison INSTANCE = new Comparison() {

        @Override
        @SuppressWarnings("unchecked")
        public Comparison compare(final Comparable left, final Comparable right) {
            return super.checkResult(left.compareTo(right));
        }

        @Override
        public <T> Comparison compare(final T left, final T right, final Comparator<T> comparator) {
            return super.checkResult(comparator.compare(left, right));
        }

        @Override
        public Comparison getInstance() {
            return INSTANCE;
        }
    };

    private static final class DeadComparison extends Comparison {

        private final int result;

        public DeadComparison(final int result) {
            this.result = result;
        }

        @Override
        public Comparison compare(final Comparable<?> left, final Comparable<?> right) {
            return this;
        }

        @Override
        public <T> Comparison compare(final T left, final T right, final Comparator<T> comparator) {
            return this;
        }

        @Override
        public Comparison compare(final int left, final int right) {
            return this;
        }

        @Override
        public Comparison compare(final long left, final long right) {
            return this;
        }

        @Override
        public Comparison compare(final float left, final float right) {
            return this;
        }

        @Override
        public Comparison compare(final double left, final double right) {
            return this;
        }

        @Override
        public Comparison compare(final boolean left, final boolean right) {
            return this;
        }

        @Override
        public int result() {
            return result;
        }

        @Override
        public Comparison getInstance() {
            return this;
        }
    }

    /**
     * Private constructor for singleton pattern.
     */
    private Comparison() {
    }

    /**
     * Begins a new comparison.
     *
     * @return the comparison.
     */
    public static Comparison begin() {
        return INSTANCE;
    }

    /**
     * Begins a new comparison, but allows for {@code null} values to be passed.
     * <p/>
     * <p>
     * If the first value is {@code null} and the second value is
     * {@code non-null}, the comparison will return -1. If the first value is
     * {@code non-null} and the second value is {@code null}, the comparison
     * will return 1. If both values are {@code null} 0 is returned.
     * </p>
     *
     * @return the comparison.
     */
    public static Comparison beginAllowNull() {
        return ALLOW_NULL_INSTANCE;
    }

    /**
     * Compares the left comparable to the right as specified by the {@link
     * Comparable#compareTo(Object)} interface.
     *
     * @param left  the object to compare to the right.
     * @param right the object compared to the left.
     *
     * @return the the same instance if the objects are equal, otherwise a
     *         comparison that will return a defined value.
     */
    public abstract Comparison compare(Comparable<?> left, Comparable<?> right);

    /**
     * Compares the left object to the right object as specified by the {@link
     * java.util.Comparator#compare(Object, Object)}
     * interface.
     *
     * @param <T>        the type of the object to the compared.
     * @param left       the object to compare to the right.
     * @param right      the object compared to the left.
     * @param comparator the comparator used to compare the objects.
     *
     * @return the the same instance if the objects are equal, otherwise a
     *         comparison that will return a defined value.
     */
    public abstract <T> Comparison compare(T left, T right, Comparator<T> comparator);

    /**
     * Compares the left integer to the right integer.
     *
     * @param left  the integer to compare to the right.
     * @param right the integer compared to the left.
     *
     * @return the the same instance if the integers are equal, otherwise a
     *         comparison that will return a defined value.
     */
    public Comparison compare(int left, int right) {
        int result = EQUAL;
        if (left < right) {
            result = LESS;
        } else if (left > right) {
            result = GREATER;
        }
        return checkResult(result);
    }

    /**
     * Compares the left long to the right long.
     *
     * @param left  the long to compare to the right.
     * @param right the long compared to the left.
     *
     * @return the the same instance if the longs are equal, otherwise a
     *         comparison that will return a defined value.
     */
    public Comparison compare(long left, long right) {
        int result = EQUAL;
        if (left < right) {
            result = LESS;
        } else if (left > right) {
            result = GREATER;
        }
        return checkResult(result);
    }

    /**
     * Compares the left float to the float integer.
     *
     * @param left  the float to compare to the right.
     * @param right the float compared to the left.
     *
     * @return the the same instance if the floats are equal, otherwise a
     *         comparison that will return a defined value.
     */
    public Comparison compare(float left, float right) {
        return checkResult(Float.compare(left, right));
    }

    /**
     * Compares the left double to the double integer.
     *
     * @param left  the double to compare to the right.
     * @param right the double compared to the left.
     *
     * @return the the same instance if the doubles are equal, otherwise a
     *         comparison that will return a defined value.
     */
    public Comparison compare(double left, double right) {
        return checkResult(Double.compare(left, right));
    }

    /**
     * Compares the left boolean to the double boolean.
     *
     * @param left  the boolean to compare to the right.
     * @param right the boolean compared to the left.
     *
     * @return the the same instance if the booleans are equal, otherwise a
     *         comparison that will return a defined value.
     */
    public Comparison compare(boolean left, boolean right) {
        return checkResult((left == right) ? EQUAL : (left ? GREATER : LESS));
    }

    /**
     * Ends the comparison and returns 0 if all comparisons were equal, -1 if
     * the any of the left comparisons were less than the right comparisons or
     * 1 if any of the right comparisons were less than the left.
     *
     * @return zero if equal, otherwise the a value with the same sign as the first
     *         non-equal comparison.
     */
    public int result() {
        return EQUAL;
    }

    /**
     * Returns the comparison instance being used.
     *
     * @return the comparison instance being used.
     */
    protected abstract Comparison getInstance();

    /**
     * Checks to see which comparison to return.
     *
     * @param result the result of the comparison.
     *
     * @return the comparison that should be used.
     */
    private Comparison checkResult(final int result) {
        return (result < 0) ? LESS_COMPARISON : (result > 0) ? GREATER_COMPARISON : getInstance();
    }
}
