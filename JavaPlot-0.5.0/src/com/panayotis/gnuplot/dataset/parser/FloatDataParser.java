/* Copyright (c) 2007-2014 by panayotis.com
 *
 * JavaPlot is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 2.
 *
 * JavaPlot is free in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CrossMobile; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.panayotis.gnuplot.dataset.parser;

/**
 * Parser for Float data
 *
 * @author teras
 */
public class FloatDataParser extends NumericDataParser {

    /**
     * Create a new numeric data parser for Float values
     */
    public FloatDataParser() {
        super();
    }

    /**
     * Create a new Float data parser, with the information that the first
     * column is in date format.
     *
     * @param first_column_date Whether the first column is in date format
     */
    public FloatDataParser(boolean first_column_date) {
        super(first_column_date);
    }

    /**
     * Check whether this String represents a Float number
     *
     * @param format the String containing the Float number
     * @return True, if this is a representation of a Float number
     */
    protected boolean checkNumberFormat(String format) {
        try {
            Float.parseFloat(format);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
