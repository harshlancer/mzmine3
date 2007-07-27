/*
 * Copyright 2006-2007 The MZmine Development Team
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.io;

import java.io.IOException;

import net.sf.mzmine.data.Scan;

/**
 * 
 */
public interface RawDataFileWriter {

    /**
     * Adds a new scan to the file.
     * 
     * @param newScan Scan to add
     */
    public void addScan(Scan newScan) throws IOException;

    /**
     * Finishes writing of the file
     * 
     * @return newly written file as RawDataFile
     */
    public void finishWriting() throws IOException;

}
