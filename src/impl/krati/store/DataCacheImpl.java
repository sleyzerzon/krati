package krati.store;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import krati.core.array.CheckedDataArray;
import krati.core.array.SimpleDataArray;
import krati.core.array.basic.StaticLongArray;
import krati.core.segment.MemorySegmentFactory;
import krati.core.segment.SegmentFactory;
import krati.core.segment.SegmentManager;
import krati.store.DataCache;

/**
 * DataCache: A Persistent Full Cache.
 * 
 * This class is not thread-safe by design. It is expected that the conditions below hold within one JVM.
 * <pre>
 *    1. There is one and only one instance of DataCacheImpl for a given cacheDirectory.
 *    2. There is one and only one thread calling the set methods at any given time. 
 * </pre>
 * 
 * It is expected that this class is used in the case of multiple readers and single writer.
 * 
 * @author jwu
 *
 */
public class DataCacheImpl implements DataCache
{
    private final static Logger _log = Logger.getLogger(DataCacheImpl.class);
    private SimpleDataArray _dataArray;
    private int _idCount;
    private int _idStart;
    
    /**
     * Constructs a data cache with default values below.
     * <pre>
     *    Segment File Size      : 256MB
     *    Segment Compact Trigger: 0.1
     *    Segment Compact Factor : 0.5
     *    Redo entry size        : 10000
     *    Number of Redo Entries : 5
     *    Data integrity check   : No
     *    Segment Factory        : MemorySegmentFactory
     * </pre>
     * 
     * @param memberIdStart          Start of memberId
     * @param memberIdCount          Total of memberId(s)
     * @param cacheDirectory         Cache directory where persistent data will be stored
     * @throws Exception
     */
    public DataCacheImpl(int memberIdStart,
                         int memberIdCount,
                         File cacheDirectory) throws Exception
    {
        this(memberIdStart, memberIdCount, cacheDirectory, new MemorySegmentFactory(), 256);
    }
    
    /**
     * Constructs a data cache with default values below.
     * <pre>
     *    Segment Compact Trigger: 0.1
     *    Segment Compact Factor : 0.5
     *    Redo entry size        : 10000
     *    Number of Redo Entries : 5
     *    Data integrity check   : No
     *    Segment Factory        : MemorySegmentFactory
     * </pre>
     * 
     * @param memberIdStart          Start of memberId
     * @param memberIdCount          Total of memberId(s)
     * @param cacheDirectory         Cache directory where persistent data will be stored
     * @param segmentFileSizeMB      Segment size in MB
     * @throws Exception
     */
    public DataCacheImpl(int memberIdStart,
                         int memberIdCount,
                         File cacheDirectory,
                         int segmentFileSizeMB) throws Exception
    {
        this(memberIdStart, memberIdCount, cacheDirectory, new MemorySegmentFactory(), segmentFileSizeMB);
    }
    
    /**
     * Constructs a data cache with default values below.
     * <pre>
     *    Segment File Size      : 256MB
     *    Segment Compact Trigger: 0.1
     *    Segment Compact Factor : 0.5
     *    Redo entry size        : 10000
     *    Number of Redo Entries : 5
     *    Data integrity check   : No
     * </pre>
     * 
     * @param memberIdStart          Start of memberId
     * @param memberIdCount          Total of memberId(s)
     * @param cacheDirectory         Cache directory where persistent data will be stored
     * @param segmentFactory         Factory for creating Segment(s)
     * @throws Exception
     */
    public DataCacheImpl(int memberIdStart,
                         int memberIdCount,
                         File cacheDirectory,
                         SegmentFactory segmentFactory) throws Exception
    {
        this(memberIdStart, memberIdCount, cacheDirectory, segmentFactory, 256);
    }
    
    /**
     * Constructs a data cache with default values below.
     * <pre>
     *    Segment Compact Trigger: 0.1
     *    Segment Compact Factor : 0.5
     *    Redo Entry Size        : 10000
     *    Number of redo entries : 5
     *    Data integrity check   : No
     * </pre>
     * 
     * @param memberIdStart          Start of memberId
     * @param memberIdCount          Total of memberId(s)
     * @param cacheDirectory         Cache directory where persistent data will be stored
     * @param segmentFactory         Factory for creating Segment(s)
     * @param segmentFileSizeMB      Segment size in MB
     * @throws Exception
     */
    public DataCacheImpl(int memberIdStart,
                         int memberIdCount,
                         File cacheDirectory,
                         SegmentFactory segmentFactory,
                         int segmentFileSizeMB) throws Exception
    {
        this(memberIdStart, memberIdCount, 10000, 5, cacheDirectory, segmentFactory, segmentFileSizeMB, false);
    }
    
    /**
     * Constructs a data cache with Segment Compact Trigger default to 0.1 and Segment Compact Factor default to 0.5.
     * 
     * @param memberIdStart          Start of memberId
     * @param memberIdCount          Total of memberId(s)
     * @param maxEntrySize           Redo entry size (i.e., batch size)
     * @param maxEntries             Number of redo entries required for updating the underlying address array
     * @param cacheDirectory         Cache directory where persistent data will be stored
     * @param segmentFactory         Factory for creating Segment(s)
     * @param segmentFileSizeMB      Segment size in MB
     * @param checked                whether to apply default checksum (Adler32) to ensure data integrity
     * @throws Exception
     */
    public DataCacheImpl(int memberIdStart,
                         int memberIdCount,
                         int maxEntrySize,
                         int maxEntries,
                         File cacheDirectory,
                         SegmentFactory segmentFactory,
                         int segmentFileSizeMB,
                         boolean checked) throws Exception
    {
        this._idStart = memberIdStart;
        this._idCount = memberIdCount;
        
        StaticLongArray addressArray =
            new StaticLongArray(memberIdCount,
                                maxEntrySize,
                                maxEntries,
                                cacheDirectory);
        
        if(addressArray.length() != memberIdCount)
        {
            throw new IOException("Capacity expected: " + addressArray.length() + " not " + memberIdCount);
        }
        
        String segmentHome = cacheDirectory.getCanonicalPath() + File.separator + "segs";
        SegmentManager segManager = SegmentManager.getInstance(segmentHome,
                                                               segmentFactory,
                                                               segmentFileSizeMB);
        
        if(checked)
        {
            _dataArray = new CheckedDataArray(addressArray, segManager);
        }
        else
        {
            _dataArray = new SimpleDataArray(addressArray, segManager);
        }
        
        _log.info("DataCache initiated: " + getStatus());
    }
    
    /**
     * Constructs a data cache.
     * 
     * @param memberIdStart          Start of memberId
     * @param memberIdCount          Total of memberId(s)
     * @param maxEntrySize           Redo entry size (i.e., batch size)
     * @param maxEntries             Number of redo entries required for updating the underlying address array
     * @param cacheDirectory         Cache directory where persistent data will be stored
     * @param segmentFactory         Factory for creating Segment(s)
     * @param segmentFileSizeMB      Segment size in MB
     * @param segmentCompactTrigger  Percentage of segment capacity, which triggers compaction once per segment
     * @param segmentCompactFactor   Load factor of segment, below which a segment is eligible for compaction
     * @param checked                whether to apply default checksum (Adler32) to ensure data integrity
     * @throws Exception
     */
    public DataCacheImpl(int memberIdStart,
                         int memberIdCount,
                         int maxEntrySize,
                         int maxEntries,
                         File cacheDirectory,
                         SegmentFactory segmentFactory,
                         int segmentFileSizeMB,
                         double segmentCompactTrigger,
                         double segmentCompactFactor,
                         boolean checked) throws Exception
    {
        this._idStart = memberIdStart;
        this._idCount = memberIdCount;
        
        StaticLongArray addressArray =
            new StaticLongArray(memberIdCount,
                                maxEntrySize,
                                maxEntries,
                                cacheDirectory);
        
        if(addressArray.length() != memberIdCount)
        {
            throw new IOException("Capacity expected: " + addressArray.length() + " not " + memberIdCount);
        }
        
        String segmentHome = cacheDirectory.getCanonicalPath() + File.separator + "segs";
        SegmentManager segManager = SegmentManager.getInstance(segmentHome,
                                                               segmentFactory,
                                                               segmentFileSizeMB);
        
        if (checked)
        {
            _dataArray = new CheckedDataArray(addressArray, segManager, segmentCompactTrigger, segmentCompactFactor);
        }
        else
        {
            _dataArray = new SimpleDataArray(addressArray, segManager, segmentCompactTrigger, segmentCompactFactor);
        }
        
        _log.info("DataCache initiated: " + getStatus());
    }
    
    protected String getStatus()
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("idStart");
        buffer.append("=");
        buffer.append(getIdStart());
        buffer.append(" ");
        
        buffer.append("idCount");
        buffer.append("=");
        buffer.append(getIdCount());
        buffer.append(" ");
        
        buffer.append("lwMark");
        buffer.append("=");
        buffer.append(getLWMark());
        buffer.append(" ");

        buffer.append("hwMark");
        buffer.append("=");
        buffer.append(getHWMark());
        
        return buffer.toString();
    }
    
    @Override
    public int getIdCount()
    {
        return _idCount;
    }
    
    @Override
    public int getIdStart()
    {
        return _idStart;
    }
    
    @Override
    public byte[] get(int memberId)
    {
        return _dataArray.getData(memberId - _idStart);
    }
    
    @Override
    public int get(int memberId, byte[] dst)
    {
        return _dataArray.getData(memberId - _idStart, dst);
    }
    
    @Override
    public int get(int memberId, byte[] dst, int offset)
    {
        return _dataArray.getData(memberId - _idStart, dst, offset);
    }
    
    @Override
    public void set(int memberId, byte[] data, long scn) throws Exception
    {
        _dataArray.setData(memberId - _idStart, data, scn);
    }
    
    @Override
    public void set(int memberId, byte[] data, int offset, int length, long scn) throws Exception
    {
        _dataArray.setData(memberId - _idStart, data, offset, length, scn);
    }
    
    @Override
    public void delete(int memberId, long scn) throws Exception
    {
        _dataArray.setData(memberId - _idStart, null, scn);
    }
    
    @Override
    public void sync() throws IOException
    {
        _log.info("DataCache prior-sync: " + getStatus());
        
        _dataArray.sync();
        
        _log.info("DataCache after-sync: " + getStatus());
    }
    
    @Override
    public void persist() throws IOException
    {
        _log.info("DataCache prior-persist: " + getStatus());
        
        _dataArray.persist();
        
        _log.info("DataCache after-persist: " + getStatus());
    }
    
    @Override
    public long getHWMark()
    {
        return _dataArray.getHWMark();
    }
    
    @Override
    public long getLWMark()
    {
        return _dataArray.getLWMark();
    }
    
    @Override
    public void saveHWMark(long endOfPeriod) throws Exception
    {
        _dataArray.saveHWMark(endOfPeriod);
    }
    
    @Override
    public void clear() throws IOException
    {
        _dataArray.clear();
    }
}