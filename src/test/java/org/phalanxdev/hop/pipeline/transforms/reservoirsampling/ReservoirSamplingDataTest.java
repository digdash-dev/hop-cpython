package org.phalanxdev.hop.pipeline.transforms.reservoirsampling;

import org.junit.Test;
import org.phalanxdev.hop.pipeline.transforms.cpython.CPythonScriptExecutorData;

import static org.junit.Assert.*;
import java.util.List;

public class ReservoirSamplingDataTest {

    @Test
    public void testInitializationModes() {
        ReservoirSamplingData data = new ReservoirSamplingData(); //

        // Test Passthrough mode (k = 0)
        data.initialize(0, 123); //
        assertEquals(ReservoirSamplingData.PROC_MODE.PASSTHROUGH, data.getProcessingMode()); //[cite: 1]

        // Test Disabled mode (k < 0)
        data.initialize(-1, 123); //[cite: 1]
        assertEquals(ReservoirSamplingData.PROC_MODE.DISABLED, data.getProcessingMode()); //[cite: 1]

        // Test Sampling mode (k > 0)
        data.initialize(100, 123); //[cite: 1]
        assertEquals(ReservoirSamplingData.PROC_MODE.SAMPLING, data.getProcessingMode()); //[cite: 1]
    }

    @Test
    public void testProcessRowCapacity() {
        ReservoirSamplingData data = new ReservoirSamplingData(); //
        data.initialize(2, 42); // Sample size of 2 //[cite: 1]
        
        data.processRow(new Object[]{"Row 1"}); //[cite: 1]
        data.processRow(new Object[]{"Row 2"}); //[cite: 1]
        data.processRow(new Object[]{"Row 3"}); // Triggers Vitter's replacement logic[cite: 1]
        
        List<Object[]> sample = data.getSample(); //[cite: 1]
        
        // Prune the trailing nulls inserted by setElement's buffering logic
        CPythonScriptExecutorData.pruneNullRowsFromSample(sample); //[cite: 1]
        
        // The pruned sample size should now correctly be exactly k (2)
        assertNotNull(sample);
        assertEquals(2, sample.size()); 
    }
    
    @Test
    public void testSameSeedProducesSameSample() {
        int k = 5;
        int seed = 999;
        
        // Initialize two separate samplers with the same parameters
        ReservoirSamplingData sampler1 = new ReservoirSamplingData();
        sampler1.initialize(k, seed);
        
        ReservoirSamplingData sampler2 = new ReservoirSamplingData();
        sampler2.initialize(k, seed);
        
        // Feed both samplers the exact same 100 rows
        for (int i = 0; i < 100; i++) {
            Object[] row = new Object[]{"RowValue_" + i};
            sampler1.processRow(row);
            sampler2.processRow(row);
        }
        
        List<Object[]> sample1 = sampler1.getSample();
        List<Object[]> sample2 = sampler2.getSample();
        
        CPythonScriptExecutorData.pruneNullRowsFromSample(sample1);
        CPythonScriptExecutorData.pruneNullRowsFromSample(sample2);
        
        // Verify both arrays ended up with the exact same randomly selected rows
        assertEquals(k, sample1.size());
        assertEquals(k, sample2.size());
        
        for (int i = 0; i < k; i++) {
            assertEquals(sample1.get(i)[0], sample2.get(i)[0]);
        }
    }
    
    @Test
    public void testDisabledAndPassthroughModesDoNotSample() {
        ReservoirSamplingData passthroughData = new ReservoirSamplingData();
        passthroughData.initialize(0, 1); // k = 0
        
        ReservoirSamplingData disabledData = new ReservoirSamplingData();
        disabledData.initialize(-1, 1); // k = -1[cite: 1]
        
        for (int i = 0; i < 10; i++) {
            Object[] row = new Object[]{"Data"};
            passthroughData.processRow(row);
            disabledData.processRow(row);
        }
        
        // For Passthrough (k=0), currentRow is never < k, so sample remains empty
        List<Object[]> ptSample = passthroughData.getSample();
        CPythonScriptExecutorData.pruneNullRowsFromSample(ptSample);
        assertEquals(0, ptSample.size());
        
        // For Disabled (k=-1), currentRow is never < k, so sample remains empty
        List<Object[]> disabledSample = disabledData.getSample();
        CPythonScriptExecutorData.pruneNullRowsFromSample(disabledSample);
        assertEquals(0, disabledSample.size());
    }
    
    @Test
    public void testCleanUpReleasesMemory() {
        ReservoirSamplingData data = new ReservoirSamplingData();
        data.initialize(10, 42);
        
        data.processRow(new Object[]{"Row 1"});
        assertNotNull(data.getSample());
        
        // Execute cleanup phase
        data.cleanUp();
        
        // Verify the sample pointer is nullified to prevent memory leaks
        assertNull(data.getSample());
    }
}