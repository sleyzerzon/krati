package krati.core.segment;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Date;

/**
 * AbstractSegment
 * 
 * @author jwu
 *
 */
public abstract class AbstractSegment implements Segment
{
    protected final int _segId;
    protected final File _segFile;
    protected final int _initSizeMB;
    protected final long _initSizeBytes;
    protected volatile int _loadSizeBytes;
    protected volatile long _lastForcedTime;
    protected volatile Segment.Mode _segMode;
    protected RandomAccessFile _raf = null;
    protected FileChannel _channel = null;
    
    protected long _storageVersion;
    
    protected AbstractSegment(int segmentId, File segmentFile, int initialSizeMB, Segment.Mode mode) throws IOException
    {
        this._segId = segmentId;
        this._segFile = segmentFile;
        this._initSizeMB = initialSizeMB;
        this._initSizeBytes = initialSizeMB * 1024L * 1024L;
        this._segMode = mode;
        this.init();
    }
    
    protected abstract void init() throws IOException;
    
    protected void initHeader() throws IOException
    {
        // update the time stamp of segment
        _lastForcedTime = System.currentTimeMillis();
        _storageVersion = Segment.STORAGE_VERSION;
        
        setAppendPosition(0);
        appendLong(getLastForcedTime());
        decrLoadSize(8);
        appendLong(getStorageVersion());
        decrLoadSize(8);
        force();
        
        _channel.position(Segment.dataStartPosition);
        setAppendPosition(Segment.dataStartPosition);
    }
    
    protected void loadHeader() throws IOException
    {
        _lastForcedTime = readLong(posLastForcedTime);
        _storageVersion = readLong(posStorageVersion);
    }
    
    protected String getHeader()
    {
        StringBuffer b = new StringBuffer();
        
        b.append("lastForcedTime");
        b.append('=');
        b.append(new Date(getLastForcedTime()));
        
        b.append(' ');
        
        b.append("storageVersion");
        b.append('=');
        b.append(getStorageVersion());
        
        return b.toString();
    }

    protected long getChannelPosition() throws IOException
    {
        return _channel.position();
    }
    
    @Override
    public String getStatus()
    {
        StringBuffer b = new StringBuffer();
        
        b.append("loadSize");
        b.append('=');
        b.append(getLoadSize());
        
        b.append(' ');
        
        b.append("appendPosition");
        b.append('=');
        try
        {
            b.append(getAppendPosition());
        }
        catch(IOException ioe)
        {
            b.append('?');
        }
        
        b.append(' ');
        
        b.append("channelPosition");
        b.append('=');
        try
        {
            b.append(getChannelPosition());
        }
        catch(IOException ioe)
        {
            b.append('?');
        }
        
        b.append(' ');
        
        b.append("lastForcedTime");
        b.append('=');
        b.append(new Date(getLastForcedTime()));
        
        return b.toString();
    }
    
    @Override
    public final Mode getMode()
    {
        return _segMode;
    }
    
    @Override
    public final int getSegmentId()
    {
        return _segId;
    }
    
    @Override
    public final File getSegmentFile()
    {
        return _segFile;
    }
    
    @Override
    public final int getInitialSizeMB()
    {
        return _initSizeMB;
    }
    
    @Override
    public final long getInitialSize() {
        return _initSizeBytes;
    }
    
    @Override
    public final long getLastForcedTime()
    {
        return _lastForcedTime;
    }
    
    @Override
    public final long getStorageVersion()
    {
        return _storageVersion;
    }
    
    @Override
    public final double getLoadFactor()
    {
       return ((double)_loadSizeBytes) / _initSizeBytes;
    }

    @Override
    public final int getLoadSize()
    {
        return _loadSizeBytes;
    }

    @Override
    public final synchronized void incrLoadSize(int byteCnt)
    {
        _loadSizeBytes += byteCnt;
    }
    
    @Override
    public final synchronized void decrLoadSize(int byteCnt)
    {
        _loadSizeBytes -= byteCnt;
    }
    
    @Override
    public final boolean isReadOnly()
    {
        return (_segMode == Segment.Mode.READ_ONLY);
    }
}
