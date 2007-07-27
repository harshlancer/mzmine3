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

package net.sf.mzmine.modules.filtering.crop;

import java.io.IOException;

import net.sf.mzmine.data.Scan;
import net.sf.mzmine.data.impl.SimpleParameterSet;
import net.sf.mzmine.data.impl.SimpleScan;
import net.sf.mzmine.io.RawDataFile;
import net.sf.mzmine.io.RawDataFileWriter;
import net.sf.mzmine.taskcontrol.Task;

/**
 * 
 */
class CropFilterTask implements Task {

    private RawDataFile dataFile;

    private TaskStatus status = TaskStatus.WAITING;
    private String errorMessage;

    private int filteredScans;
    private int totalScans;

    private int msLevel;
    private double minMZ;
    private double maxMZ;
    private double minRT;
    private double maxRT;

    private RawDataFile filteredRawDataFile;

    /**
     * @param rawDataFile
     * @param parameters
     */
    CropFilterTask(RawDataFile dataFile, SimpleParameterSet parameters) {
        this.dataFile = dataFile;
        msLevel = (Integer) parameters.getParameterValue(CropFilter.parameterMSlevel);
        minMZ = (Float) parameters.getParameterValue(CropFilter.parameterMinMZ);
        minRT = (Float) parameters.getParameterValue(CropFilter.parameterMinRT);
        maxMZ = (Float) parameters.getParameterValue(CropFilter.parameterMaxMZ);
        maxRT = (Float) parameters.getParameterValue(CropFilter.parameterMaxRT);
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getTaskDescription()
     */
    public String getTaskDescription() {
        return "Crop filtering " + dataFile;
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getFinishedPercentage()
     */
    public float getFinishedPercentage() {
        if (totalScans == 0)
            return 0.0f;
        return (float) filteredScans / totalScans;
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getStatus()
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getErrorMessage()
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getResult()
     */
    public Object getResult() {
        return filteredRawDataFile;
    }

    public RawDataFile getDataFile() {
        return dataFile;
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#cancel()
     */
    public void cancel() {
        status = TaskStatus.CANCELED;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        status = TaskStatus.PROCESSING;

        // Create new temporary copy
        RawDataFileWriter rawDataFileWriter;
        try {
            rawDataFileWriter = dataFile.updateFile();
        } catch (IOException e) {
            status = TaskStatus.ERROR;
            errorMessage = e.toString();
            return;
        }

        Scan oldScan, newScan;

        // Get all scans
        int[] scanNumbers = dataFile.getScanNumbers();
        totalScans = scanNumbers.length;

        // Loop through all scans
        for (int scani = 0; scani < totalScans; scani++) {

            // Check if we are not canceled
            if (status == TaskStatus.CANCELED)
                return;

            // Get scan
            oldScan = dataFile.getScan(scanNumbers[scani]);

            // if the scan is our target MS level, do the filtering
            if (oldScan.getMSLevel() == msLevel) {

                // Is this scan within the RT range?
                if ((oldScan.getRetentionTime() >= minRT)
                        && (oldScan.getRetentionTime() <= maxRT)) {

                    // Pickup datapoints inside the M/Z range
                    float originalMassValues[] = oldScan.getMZValues();
                    float originalIntensityValues[] = oldScan.getIntensityValues();

                    int numSmallerThanMin = 0;
                    for (int ind = 0; ind < originalMassValues.length; ind++) {
                        if (originalMassValues[ind] >= minMZ) {
                            break;
                        }
                        numSmallerThanMin++;
                    }

                    int numBiggerThanMax = 0;
                    for (int ind = (originalMassValues.length - 1); ind >= 0; ind--) {
                        if (originalMassValues[ind] <= maxMZ) {
                            break;
                        }
                        numBiggerThanMax++;
                    }

                    float newMassValues[] = new float[originalMassValues.length
                            - numSmallerThanMin - numBiggerThanMax];
                    float newIntensityValues[] = new float[originalMassValues.length
                            - numSmallerThanMin - numBiggerThanMax];

                    int newInd = 0;
                    for (int ind = numSmallerThanMin; ind < (originalMassValues.length - numBiggerThanMax); ind++) {
                        newMassValues[newInd] = originalMassValues[ind];
                        newIntensityValues[newInd] = originalIntensityValues[ind];
                        newInd++;
                    }

                    SimpleScan tmpScan = new SimpleScan(oldScan);
                    tmpScan.setData(newMassValues, newIntensityValues);
                    newScan = tmpScan;

                } else {
                    // ignore this scan
                    filteredScans++;
                    continue;
                }
            } else {
                // TODO: check if the parent scan is included into new file
                newScan = oldScan;
            }

            // Write the modified scan to file
            try {

                rawDataFileWriter.addScan(newScan);

            } catch (IOException e) {
                status = TaskStatus.ERROR;
                errorMessage = e.toString();
                return;
            }

            filteredScans++;

        }

        // Finalize writing
        try {
            rawDataFileWriter.finishWriting();
        } catch (IOException e) {
            status = TaskStatus.ERROR;
            errorMessage = e.toString();
            return;
        }

        status = TaskStatus.FINISHED;

    }

}
