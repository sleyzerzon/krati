/*
 * Copyright (c) 2010-2012 LinkedIn, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package krati.core;

import java.io.File;

import krati.core.segment.SegmentFactory;
import krati.store.ArrayStore;
import krati.store.ArrayStorePartition;
import krati.store.DynamicDataArray;
import krati.store.DynamicDataSet;
import krati.store.DynamicDataStore;
import krati.store.IndexedDataStore;
import krati.store.StaticArrayStorePartition;
import krati.store.StaticDataArray;
import krati.store.StaticDataSet;
import krati.store.StaticDataStore;
import krati.util.FnvHashFunction;

/**
 * StoreFactory offers a standard static API for creating different data stores including
 * {@link krati.store.ArrayStore ArrayStore} and {@link krati.store.DataStore DataStore},
 * which are handling byte array based keys and values.
 * 
 * @author jwu
 * @since 06/09, 2011
 * 
 * <p>
 * 06/11, 2011 - Added methods for creating static and dynamic DataSet <br/>
 * 06/12, 2011 - Added JavaDoc comment <br/>
 * 06/25, 2011 - Added factory methods using StoreConfig <br/>
 */
public class StoreFactory {
    
    /**
     * Creates a fixed-length {@link krati.store.ArrayStorePartition ArrayStorePartition}.
     * A configuration file <code>config.properties</code> is created automatically in the store home directory. 
     * 
     * @param config - ArrayStorePartition configuration
     * @return A range-based ArrayStorePartition.
     * @throws Exception if the store cannot be created.
     */
    public static ArrayStorePartition createArrayStorePartition(StorePartitionConfig config) throws Exception {
        return new StaticArrayStorePartition(config);
    }
    
    /**
     * Creates a {@link krati.store.ArrayStorePartition ArrayStorePartition} with the default parameters below.
     * 
     * <pre>
     *   batchSize            : 10000
     *   numSyncBatches       : 5
     *   segmentCompactFactor : 0.5
     * </pre>
     * 
     * @param homeDir              - the store home directory
     * @param idStart              - the partition idStart (i.e. the first index)
     * @param idCount              - the partition idCount (i.e. capacity) which cannot be changed after the store is created
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return A range-based ArrayStore.
     * @throws Exception if the store cannot be created.
     */
    public static ArrayStorePartition createArrayStorePartition(
            File homeDir,
            int idStart,
            int idCount,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        int batchSize = StoreParams.BATCH_SIZE_DEFAULT;
        int numSyncBatches = StoreParams.NUM_SYNC_BATCHES_DEFAULT;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return StoreFactory.createArrayStorePartition(
                homeDir,
                idStart,
                idCount,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
    }
    
    /**
     * Creates a {@link krati.store.ArrayStorePartition ArrayStorePartition} with the default parameters below.
     * 
     * <pre>
     *   segmentCompactFactor : 0.5
     * </pre>
     * 
     * @param homeDir              - the store home directory
     * @param idStart              - the partition idStart (i.e. the first index)
     * @param idCount              - the partition idCount (i.e. capacity) which cannot be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return A range-based ArrayStore.
     * @throws Exception if the store cannot be created.
     */
    public static ArrayStorePartition createArrayStorePartition(
            File homeDir,
            int idStart,
            int idCount,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return StoreFactory.createArrayStorePartition(
                homeDir,
                idStart,
                idCount,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
    }
    
    /**
     * Creates a {@link krati.store.ArrayStorePartition ArrayStorePartition}.
     * 
     * @param homeDir              - the store home directory
     * @param idStart              - the partition idStart (i.e. the first index)
     * @param idCount              - the partition idCount (i.e. capacity) which cannot be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @param segmentCompactFactor - the segment load threshold, below which a segment is eligible for compaction
     * @return A range-based ArrayStore.
     * @throws Exception if the store cannot be created.
     */
    public static ArrayStorePartition createArrayStorePartition(
            File homeDir,
            int idStart,
            int idCount,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory,
            double segmentCompactFactor) throws Exception {
        return new StaticArrayStorePartition(
                idStart,
                idCount,
                batchSize,
                numSyncBatches,
                homeDir,
                segmentFactory,
                segmentFileSizeMB,
                segmentCompactFactor,
                false);
    }
    
    /**
     * Creates a fixed-length {@link krati.store.ArrayStore ArrayStore}.
     * A configuration file <code>config.properties</code> is created automatically in the store home directory. 
     * 
     * @param config - ArrayStore configuration
     * @return A fixed-length ArrayStore.
     * @throws Exception if the store cannot be created.
     */
    public static ArrayStore createStaticArrayStore(StoreConfig config) throws Exception {
        return new StaticDataArray(config);
    }
    
    /**
     * Creates a fixed-length {@link krati.store.ArrayStore ArrayStore} with the default parameters below.
     * 
     * <pre>
     *   batchSize            : 10000
     *   numSyncBatches       : 5
     *   segmentCompactFactor : 0.5
     * </pre>
     * 
     * @param homeDir              - the store home directory
     * @param length               - the store length (i.e. capacity) which cannot be changed after the store is created
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return A fixed-length ArrayStore.
     * @throws Exception if the store cannot be created.
     */
    public static ArrayStore createStaticArrayStore(
            File homeDir,
            int length,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        int batchSize = StoreParams.BATCH_SIZE_DEFAULT;
        int numSyncBatches = StoreParams.NUM_SYNC_BATCHES_DEFAULT;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return createStaticArrayStore(
                homeDir,
                length,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
    }
    
    /**
     * Creates a fixed-length {@link krati.store.ArrayStore ArrayStore} with the default parameters below.
     * 
     * <pre>
     *   segmentCompactFactor : 0.5
     * </pre>
     * 
     * @param homeDir              - the store home directory
     * @param length               - the store length (i.e. capacity) which cannot be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return A fixed-length ArrayStore.
     * @throws Exception if the store cannot be created.
     */
    public static ArrayStore createStaticArrayStore(
            File homeDir,
            int length,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return createStaticArrayStore(
                homeDir,
                length,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
    }
    
    /**
     * Creates a fixed-length {@link krati.store.ArrayStore ArrayStore}.
     * 
     * @param homeDir              - the store home directory
     * @param length               - the store length (i.e. capacity) which cannot be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @param segmentCompactFactor - the segment load threshold, below which a segment is eligible for compaction
     * @return A fixed-length ArrayStore.
     * @throws Exception if the store cannot be created.
     */
    public static ArrayStore createStaticArrayStore(
            File homeDir,
            int length,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory,
            double segmentCompactFactor) throws Exception {
        return new StaticDataArray(
                length,
                batchSize,
                numSyncBatches,
                homeDir,
                segmentFactory,
                segmentFileSizeMB,
                segmentCompactFactor);
    }
    
    /**
     * Creates a dynamic {@link krati.store.ArrayStore ArrayStore} which grows its capacity as needed.
     * A configuration file <code>config.properties</code> is created automatically in the store home directory. 
     * 
     * @param config - ArrayStore configuration
     * @return a dynamic ArrayStore with growing capacity as needed.
     * @throws Exception if the store cannot be created.
     */
    public static ArrayStore createDynamicArrayStore(StoreConfig config) throws Exception {
        return new DynamicDataArray(config);
    }
    
    /**
     * Creates a dynamic {@link krati.store.ArrayStore ArrayStore} which grows its capacity as needed.
     * The store created has the default parameters below.
     * 
     * <pre>
     *   batchSize            : 10000
     *   numSyncBatches       : 5
     *   segmentCompactFactor : 0.5
     * </pre>
     * 
     * @param homeDir              - the store home directory
     * @param initialLength        - the initial length (i.e. capacity) which should not be changed after the store is created
     * @param segmentFileSizeMB    - the segment size in MB 
     * @param segmentFactory       - the segment factory
     * @return a dynamic ArrayStore with growing capacity as needed.
     * @throws Exception if the store cannot be created.
     */
    public static ArrayStore createDynamicArrayStore(
            File homeDir,
            int initialLength,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        int batchSize = StoreParams.BATCH_SIZE_DEFAULT;
        int numSyncBatches = StoreParams.NUM_SYNC_BATCHES_DEFAULT;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return createDynamicArrayStore(
                homeDir,
                initialLength,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
    }
    
    /**
     * Creates a dynamic {@link krati.store.ArrayStore ArrayStore} which grows its capacity as needed.
     * The store created has the default parameters below.
     * 
     * <pre>
     *   segmentCompactFactor : 0.5
     * </pre>
     * 
     * @param homeDir              - the store home directory
     * @param initialLength        - the initial length (i.e. capacity) which should not be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return a dynamic ArrayStore with growing capacity as needed.
     * @throws Exception if the store cannot be created.
     */
    public static ArrayStore createDynamicArrayStore(
            File homeDir,
            int initialLength,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return createDynamicArrayStore(
                homeDir,
                initialLength,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
    }
    
    /**
     * Creates a dynamic {@link krati.store.ArrayStore ArrayStore} which grows its capacity as needed.
     * 
     * @param homeDir              - the store home directory
     * @param initialLength        - the initial length (i.e. capacity) which should not be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @param segmentCompactFactor - the segment load threshold, below which a segment is eligible for compaction
     * @return a dynamic ArrayStore with growing capacity as needed.
     * @throws Exception if the store cannot be created.
     */
    public static ArrayStore createDynamicArrayStore(
            File homeDir,
            int initialLength,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory,
            double segmentCompactFactor) throws Exception {
        return new DynamicDataArray(
                initialLength,
                batchSize,
                numSyncBatches,
                homeDir,
                segmentFactory,
                segmentFileSizeMB,
                segmentCompactFactor);
    }
    
    /**
     * Creates a static {@link krati.store.DataStore DataStore} with a fixed-capacity.
     * A configuration file <code>config.properties</code> is created automatically in the store home directory. 
     * 
     * @param config - DataStore configuration
     * @return A fixed-capacity DataStore.
     * @throws Exception if the store cannot be created.
     */
    public static StaticDataStore createStaticDataStore(StoreConfig config) throws Exception {
        return new StaticDataStore(config);
    }
    
    /**
     * Creates a fixed-capacity {@link krati.store.DataStore DataStore} with the default parameters below.
     * 
     * <pre>
     *   batchSize            : 10000
     *   numSyncBatches       : 5
     *   segmentCompactFactor : 0.5
     * </pre>
     * 
     * @param homeDir              - the store home directory
     * @param capacity             - the store capacity which cannot be changed after the store is created
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return A fixed-capacity DataStore.
     * @throws Exception if the store cannot be created.
     */
    public static StaticDataStore createStaticDataStore(
            File homeDir,
            int capacity,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        int batchSize = StoreParams.BATCH_SIZE_DEFAULT;
        int numSyncBatches = StoreParams.NUM_SYNC_BATCHES_DEFAULT;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return createStaticDataStore(
                homeDir,
                capacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
    }
    
    /**
     * Creates a fixed-capacity {@link krati.store.DataStore DataStore} with the default parameters below.
     * 
     * <pre>
     *   segmentCompactFactor : 0.5
     * </pre>
     * 
     * @param homeDir              - the store home directory
     * @param capacity             - the store capacity which cannot be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return A fixed-capacity DataStore.
     * @throws Exception if the store cannot be created.
     */
    public static StaticDataStore createStaticDataStore(
            File homeDir,
            int capacity,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return createStaticDataStore(
                homeDir,
                capacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
    }
    
    /**
     * Creates a fixed-capacity {@link krati.store.DataStore DataStore}.
     * 
     * @param homeDir              - the store home directory
     * @param capacity             - the store capacity which cannot be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @param segmentCompactFactor - the segment load threshold, below which a segment is eligible for compaction
     * @return A fixed-capacity DataStore.
     * @throws Exception if the store cannot be created.
     */
    public static StaticDataStore createStaticDataStore(
            File homeDir,
            int capacity,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory,
            double segmentCompactFactor) throws Exception {
        return new StaticDataStore(
                homeDir,
                capacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor,
                new FnvHashFunction());
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataStore DataStore} which grows its capacity as needed.
     * A configuration file <code>config.properties</code> is created automatically in the store home directory. 
     * 
     * @param config - DataStore configuration
     * @return A dynamic DataStore with growing capacity as needed.
     * @throws Exception if the store cannot be created.
     */
    public static DynamicDataStore createDynamicDataStore(StoreConfig config) throws Exception {
        return new DynamicDataStore(config);
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataStore DataStore} which grows its capacity as needed.
     * The store created has the default parameters below:
     * 
     * <pre>
     *   batchSize            : 10000
     *   numSyncBatches       : 5
     *   segmentCompactFactor : 0.5
     *   hashLoadFactor       : 0.75
     * </pre>
     * 
     * <p>
     * It is recommended to have an <code>initialCapacity</code> which is large enough to hold the total number of keys
     * eventually added to this store. This can reduce hash conflicts and yield better performance.
     * 
     * @param homeDir              - the store home directory
     * @param initialCapacity      - the initial capacity which should not be changed after the store is created
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return A dynamic DataStore with growing capacity as needed.
     * @throws Exception if the store cannot be created.
     */
    public static DynamicDataStore createDynamicDataStore(
            File homeDir,
            int initialCapacity,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        int batchSize = StoreParams.BATCH_SIZE_DEFAULT;
        int numSyncBatches = StoreParams.NUM_SYNC_BATCHES_DEFAULT;
        double hashLoadFactor = StoreParams.HASH_LOAD_FACTOR_DEFAULT;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return createDynamicDataStore(
                homeDir,
                initialCapacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor,
                hashLoadFactor);
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataStore DataStore} which grows its capacity as needed.
     * The store created has the default parameters below:
     * 
     * <pre>
     *   segmentCompactFactor : 0.5
     *   hashLoadFactor       : 0.75
     * </pre>
     * 
     * <p>
     * It is recommended to have an <code>initialCapacity</code> which is large enough to hold the total number of keys
     * eventually added to this store. This can reduce hash conflicts and yield better performance.
     * 
     * @param homeDir              - the store home directory
     * @param initialCapacity      - the initial capacity which should not be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return A dynamic DataStore with growing capacity as needed.
     * @throws Exception if the store cannot be created.
     */
    public static DynamicDataStore createDynamicDataStore(
            File homeDir,
            int initialCapacity,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        double hashLoadFactor = StoreParams.HASH_LOAD_FACTOR_DEFAULT;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return createDynamicDataStore(
                homeDir,
                initialCapacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor,
                hashLoadFactor);
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataStore DataStore} which grows its capacity as needed.
     * The store created has the default parameters below:
     * 
     * <pre>
     *   hashLoadFactor : 0.75
     * </pre>
     * 
     * <p>
     * It is recommended to have an <code>initialCapacity</code> which is large enough to hold the total number of keys
     * eventually added to this store. This can reduce hash conflicts and yield better performance.
     * 
     * @param homeDir              - the store home directory
     * @param initialCapacity      - the initial capacity which should not be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @param segmentCompactFactor - the segment load threshold, below which a segment is eligible for compaction
     * @return A dynamic DataStore with growing capacity as needed.
     * @throws Exception if the store cannot be created.
     */
    public static DynamicDataStore createDynamicDataStore(
            File homeDir,
            int initialCapacity,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory,
            double segmentCompactFactor) throws Exception {
        double hashLoadFactor = StoreParams.HASH_LOAD_FACTOR_DEFAULT;
        
        return createDynamicDataStore(
                homeDir,
                initialCapacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor,
                hashLoadFactor);
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataStore DataStore} which grows its capacity as needed.
     * 
     * <p>
     * It is recommended to have an <code>initialCapacity</code> which is large enough to hold the total number of keys
     * eventually added to this store. This can reduce hash conflicts and yield better performance.
     * 
     * @param homeDir              - the store home directory
     * @param initialCapacity      - the initial capacity which should not be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @param segmentCompactFactor - the segment load threshold, below which a segment is eligible for compaction
     * @param hashLoadFactor       - the load factor of the underlying hash table
     * @return A dynamic DataStore with growing capacity as needed.
     * @throws Exception if the store cannot be created.
     */
    public static DynamicDataStore createDynamicDataStore(
            File homeDir,
            int initialCapacity,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory,
            double segmentCompactFactor,
            double hashLoadFactor) throws Exception {
        return new DynamicDataStore(
                homeDir,
                StoreParams.getDynamicStoreInitialLevel(initialCapacity),
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor,
                hashLoadFactor,
                new FnvHashFunction());
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataStore DataStore} which grows its capacity as needed.
     * A configuration file <code>config.properties</code> is created automatically in the store home directory. 
     * 
     * @param config - DataStore configuration
     * @return A dynamic DataStore with growing capacity as needed.
     * @throws Exception if the store cannot be created.
     */
    public static IndexedDataStore createIndexedDataStore(StoreConfig config) throws Exception {
        return new IndexedDataStore(config);
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataStore DataStore} which grows its capacity as needed.
     * The store created has the default parameters below.
     * 
     * <pre>
     *   batchSize            : 10000
     *   numSyncBatches       : 5
     *   segmentCompactFactor : 0.5
     *   hashLoadFactor       : 0.75
     * </pre>
     * 
     * <p>
     * It is recommended to have an <code>initialCapacity</code> which is large enough to hold the total number of keys
     * eventually added to this store. This can reduce hash conflicts and yield better performance.
     * 
     * <p>
     * {@link krati.store.IndexedDataStore IndexedDataStore} is suitable for very large data sets
     * in which the number of keys can fit into memory and the total bytes of all values is significantly
     * larger than the available memory.  
     * 
     * @param homeDir                - the store home directory
     * @param initialCapacity        - the initial capacity which should not be changed after the store is created
     * @param indexSegmentFileSizeMB - the index segment size in MB with a recommended range from 8 to 32
     * @param indexSegmentFactory    - the index segment factory, {@link krati.core.segment.MemorySegmentFactory MemorySegmentFactory} recommended
     * @param storeSegmentFileSizeMB - the store segment size in MB with a recommended range from 8 to 256
     * @param storeSegmentFactory    - the store segment factory, {@link krati.core.segment.WriteBufferSegmentFactory WriteBufferSegmentFactory} recommended
     * @return A dynamic DataStore with growing capacity as needed.
     * @throws Exception if the store cannot be created.
     */
    public static IndexedDataStore createIndexedDataStore(
            File homeDir,
            int initialCapacity,
            int indexSegmentFileSizeMB,
            SegmentFactory indexSegmentFactory,
            int storeSegmentFileSizeMB,
            SegmentFactory storeSegmentFactory) throws Exception {
        int batchSize = StoreParams.BATCH_SIZE_DEFAULT;
        int numSyncBatches = StoreParams.NUM_SYNC_BATCHES_DEFAULT;
        
        return createIndexedDataStore(
                homeDir,
                initialCapacity,
                batchSize,
                numSyncBatches,
                indexSegmentFileSizeMB,
                indexSegmentFactory,
                storeSegmentFileSizeMB,
                storeSegmentFactory);
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataStore DataStore} which grows its capacity as needed.
     * The store created has the default parameters below.
     * 
     * <pre>
     *   segmentCompactFactor : 0.5
     *   hashLoadFactor       : 0.75
     * </pre>
     * 
     * <p>
     * It is recommended to have an <code>initialCapacity</code> which is large enough to hold the total number of keys
     * eventually added to this store. This can reduce hash conflicts and yield better performance.
     * 
     * <p>
     * {@link krati.store.IndexedDataStore IndexedDataStore} is suitable for very large data sets
     * in which the number of keys can fit into memory and the total bytes of all values is significantly
     * larger than the available memory.  
     * 
     * @param homeDir                - the store home directory
     * @param initialCapacity        - the initial capacity which should not be changed after the store is created
     * @param batchSize              - the number of updates per update batch
     * @param numSyncBatches         - the number of update batches required for updating the underlying indexes
     * @param indexSegmentFileSizeMB - the index segment size in MB with a recommended range from 8 to 32
     * @param indexSegmentFactory    - the index segment factory, {@link krati.core.segment.MemorySegmentFactory MemorySegmentFactory} recommended
     * @param storeSegmentFileSizeMB - the store segment size in MB with a recommended range from 8 to 256
     * @param storeSegmentFactory    - the store segment factory, {@link krati.core.segment.WriteBufferSegmentFactory WriteBufferSegmentFactory} recommended
     * @return A dynamic DataStore with growing capacity as needed.
     * @throws Exception if the store cannot be created.
     */
    public static IndexedDataStore createIndexedDataStore(
            File homeDir,
            int initialCapacity,
            int batchSize,
            int numSyncBatches,
            int indexSegmentFileSizeMB,
            SegmentFactory indexSegmentFactory,
            int storeSegmentFileSizeMB,
            SegmentFactory storeSegmentFactory) throws Exception {
        return new IndexedDataStore(
                homeDir,
                initialCapacity,
                batchSize,
                numSyncBatches,
                indexSegmentFileSizeMB,
                indexSegmentFactory,
                storeSegmentFileSizeMB,
                storeSegmentFactory);
    }
    
    /**
     * Creates a static {@link krati.store.DataSet DataSet} with a fixed-capacity.
     * A configuration file <code>config.properties</code> is created automatically in the store home directory. 
     * 
     * @param config - DataSet configuration
     * @return A fixed-capacity DataSet.
     * @throws Exception if the set cannot be created.
     */
    public static StaticDataSet createStaticDataSet(StoreConfig config) throws Exception {
        return new StaticDataSet(config);
    }
    
    /**
     * Creates a fixed-capacity {@link krati.store.DataSet DataSet} with the default parameters below.
     * 
     * <pre>
     *   batchSize            : 10000
     *   numSyncBatches       : 5
     *   segmentCompactFactor : 0.5
     * </pre>
     * 
     * @param homeDir              - the set home directory
     * @param capacity             - the set capacity which cannot be changed after the store is created
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return A fixed-capacity DataSet.
     * @throws Exception if the set cannot be created.
     */
    public static StaticDataSet createStaticDataSet(
            File homeDir,
            int capacity,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        int batchSize = StoreParams.BATCH_SIZE_DEFAULT;
        int numSyncBatches = StoreParams.NUM_SYNC_BATCHES_DEFAULT;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return createStaticDataSet(
                homeDir,
                capacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
    }
    
    /**
     * Creates a fixed-capacity {@link krati.store.DataSet DataSet} with the default parameters below.
     * 
     * <pre>
     *   segmentCompactFactor : 0.5
     * </pre>
     * 
     * @param homeDir              - the set home directory
     * @param capacity             - the set capacity which cannot be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return A fixed-capacity DataSet.
     * @throws Exception if the set cannot be created.
     */
    public static StaticDataSet createStaticDataSet(
            File homeDir,
            int capacity,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return createStaticDataSet(
                homeDir,
                capacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor);
    }
    
    /**
     * Creates a fixed-capacity {@link krati.store.DataSet DataSet}.
     * 
     * @param homeDir              - the set home directory
     * @param capacity             - the set capacity which cannot be changed after the store is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @param segmentCompactFactor - the segment load threshold, below which a segment is eligible for compaction
     * @return A fixed-capacity DataSet.
     * @throws Exception if the set cannot be created.
     */
    public static StaticDataSet createStaticDataSet(
            File homeDir,
            int capacity,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory,
            double segmentCompactFactor) throws Exception {
        return new StaticDataSet(
                homeDir,
                capacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor,
                new FnvHashFunction());
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataSet DataSet} which grows its capacity as needed.
     * A configuration file <code>config.properties</code> is created automatically in the store home directory. 
     * 
     * @param config - DataSet configuration
     * @return A dynamic DataSet with growing capacity as needed.
     * @throws Exception if the set cannot be created.
     */
    public static DynamicDataSet createDynamicDataSet(StoreConfig config) throws Exception {
        return new DynamicDataSet(config);
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataSet DataSet} which grows its capacity as needed.
     * The set created has the default parameters below:
     * 
     * <pre>
     *   batchSize            : 10000
     *   numSyncBatches       : 5
     *   segmentCompactFactor : 0.5
     *   hashLoadFactor       : 0.75
     * </pre>
     * 
     * <p>
     * It is recommended to have an <code>initialCapacity</code> which is large enough to hold the total number of keys
     * eventually added to this set. This can reduce hash conflicts and yield better performance.
     * 
     * @param homeDir              - the set home directory
     * @param initialCapacity      - the initial capacity which should not be changed after the set is created
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return A dynamic DataSet with growing capacity as needed.
     * @throws Exception if the set cannot be created.
     */
    public static DynamicDataSet createDynamicDataSet(
            File homeDir,
            int initialCapacity,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        int batchSize = StoreParams.BATCH_SIZE_DEFAULT;
        int numSyncBatches = StoreParams.NUM_SYNC_BATCHES_DEFAULT;
        double hashLoadFactor = StoreParams.HASH_LOAD_FACTOR_DEFAULT;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return createDynamicDataSet(
                homeDir,
                initialCapacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor,
                hashLoadFactor);
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataSet DataSet} which grows its capacity as needed.
     * The set created has the default parameters below:
     * 
     * <pre>
     *   segmentCompactFactor : 0.5
     *   hashLoadFactor       : 0.75
     * </pre>
     * 
     * <p>
     * It is recommended to have an <code>initialCapacity</code> which is large enough to hold the total number of keys
     * eventually added to this set. This can reduce hash conflicts and yield better performance.
     * 
     * @param homeDir              - the set home directory
     * @param initialCapacity      - the initial capacity which should not be changed after the set is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @return A dynamic DataSet with growing capacity as needed.
     * @throws Exception if the set cannot be created.
     */
    public static DynamicDataSet createDynamicDataSet(
            File homeDir,
            int initialCapacity,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory) throws Exception {
        double hashLoadFactor = StoreParams.HASH_LOAD_FACTOR_DEFAULT;
        double segmentCompactFactor = StoreParams.SEGMENT_COMPACT_FACTOR_DEFAULT;
        
        return createDynamicDataSet(
                homeDir,
                initialCapacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor,
                hashLoadFactor);
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataSet DataSet} which grows its capacity as needed.
     * The set created has the default parameters below:
     * 
     * <pre>
     *   hashLoadFactor : 0.75
     * </pre>
     * 
     * <p>
     * It is recommended to have an <code>initialCapacity</code> which is large enough to hold the total number of keys
     * eventually added to this set. This can reduce hash conflicts and yield better performance.
     * 
     * @param homeDir              - the set home directory
     * @param initialCapacity      - the initial capacity which should not be changed after the set is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @param segmentCompactFactor - the segment load threshold, below which a segment is eligible for compaction
     * @return A dynamic DataSet with growing capacity as needed.
     * @throws Exception if the set cannot be created.
     */
    public static DynamicDataSet createDynamicDataSet(
            File homeDir,
            int initialCapacity,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory,
            double segmentCompactFactor) throws Exception {
        double hashLoadFactor = StoreParams.HASH_LOAD_FACTOR_DEFAULT;
        
        return createDynamicDataSet(
                homeDir,
                initialCapacity,
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor,
                hashLoadFactor);
    }
    
    /**
     * Creates a dynamic {@link krati.store.DataSet DataSet} which grows its capacity as needed.
     *
     * <p>
     * It is recommended to have an <code>initialCapacity</code> which is large enough to hold the total number of keys
     * eventually added to this set. This can reduce hash conflicts and yield better performance.
     * 
     * @param homeDir              - the set home directory
     * @param initialCapacity      - the initial capacity which should not be changed after the set is created
     * @param batchSize            - the number of updates per update batch
     * @param numSyncBatches       - the number of update batches required for updating the underlying indexes
     * @param segmentFileSizeMB    - the segment size in MB
     * @param segmentFactory       - the segment factory
     * @param segmentCompactFactor - the segment load threshold, below which a segment is eligible for compaction
     * @param hashLoadFactor       - the load factor of the underlying hash table
     * @return A dynamic DataSet with growing capacity as needed.
     * @throws Exception if the set cannot be created.
     */
    public static DynamicDataSet createDynamicDataSet(
            File homeDir,
            int initialCapacity,
            int batchSize,
            int numSyncBatches,
            int segmentFileSizeMB,
            SegmentFactory segmentFactory,
            double segmentCompactFactor,
            double hashLoadFactor) throws Exception {
        return new DynamicDataSet(
                homeDir,
                StoreParams.getDynamicStoreInitialLevel(initialCapacity),
                batchSize,
                numSyncBatches,
                segmentFileSizeMB,
                segmentFactory,
                segmentCompactFactor,
                hashLoadFactor,
                new FnvHashFunction());
    }
    
}
