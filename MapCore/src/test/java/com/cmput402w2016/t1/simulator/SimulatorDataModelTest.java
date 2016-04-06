package com.cmput402w2016.t1.simulator;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import junit.framework.TestCase;

public class SimulatorDataModelTest extends TestCase {
    public void testLibraries() {
        SummaryStatistics statistics = new SummaryStatistics();
        assertTrue(Double.isNaN(statistics.getMean()));
    }
}