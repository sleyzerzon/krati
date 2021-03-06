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

package krati.core.array.basic;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import krati.Mode;
import krati.array.Array;
import krati.array.LongArray;
import krati.core.array.AddressArray;
import krati.core.array.entry.EntryLongFactory;
import krati.core.array.entry.EntryPersistListener;
import krati.core.array.entry.EntryValueLong;

/**
 * StaticLongArray: Fixed-Size Persistent LongArray Implementation.
 * 
 * This class is not thread-safe by design. It is expected that the conditions below hold within one JVM.
 * <pre>
 *    1. There is one and only one instance of StaticLongArray for a given home directory.
 *    2. There is one and only one thread is calling the setData method at any given time. 
 * </pre>
 * 
 * It is expected that this class is used in the case of multiple readers and single writer.
 * 
 * @author jwu
 * 
 * <p>
 * 05/09, 2011 - added support for Closeable
 * 
 */
public class StaticLongArray extends AbstractRecoverableArray<EntryValueLong> implements AddressArray {
    private static final Logger _log = Logger.getLogger(StaticLongArray.class);
    private long[] _internalArray;
    
    /**
     * The mode can only be <code>Mode.INIT</code>, <code>Mode.OPEN</code> and <code>Mode.CLOSED</code>.
     */
    private volatile Mode _mode = Mode.INIT;
    
    /**
     * Create a fixed-length persistent long array.
     * 
     * @param length
     *            the length of this array
     * @param entrySize
     *            the size of redo entry (i.e., batch size)
     * @param maxEntries
     *            the number of redo entries required for updating the
     *            underlying array file
     * @param homeDirectory
     *            the home directory of this array
     * @throws Exception
     *             if this array cannot be created.
     */
    public StaticLongArray(int length, int entrySize, int maxEntries, File homeDirectory) throws Exception {
        super(length, 8 /* elementSize */, entrySize, maxEntries, homeDirectory, new EntryLongFactory());
        this._mode = Mode.OPEN;
    }
    
    @Override
    protected Logger getLogger() {
        return _log;
    }
    
    @Override
    protected void loadArrayFileData() throws IOException {
        long maxScn = _arrayFile.getLwmScn();
        if(_arrayFile.getArrayLength() != _length) {
            throw new IOException("Invalid array length: " + _length);
        }
        
        _internalArray = _arrayFile.loadLongArray();
        _entryManager.setWaterMarks(maxScn, maxScn);
        _log.info("Data loaded successfully from file " + _arrayFile.getName());
    }
    
    /**
     * Sync-up the high water mark to a given value.
     * 
     * @param endOfPeriod
     */
    @Override
    public void saveHWMark(long endOfPeriod) {
        if (getHWMark() < endOfPeriod) {
            try {
                set(0, get(0), endOfPeriod);
            } catch (Exception e) {
                _log.error("Failed to saveHWMark " + endOfPeriod, e);
            }
        } else if(0 < endOfPeriod && endOfPeriod < getLWMark()) {
            try {
                _entryManager.sync();
            } catch(Exception e) {
                _log.error("Failed to saveHWMark" + endOfPeriod, e);
            }
            _entryManager.setWaterMarks(endOfPeriod, endOfPeriod);
        }
    }
    
    @Override
    public void clear() {
        // Clear in-memory array
        if (_internalArray != null) {
            for (int i = 0; i < _internalArray.length; i++) {
                _internalArray[i] = 0;
            }
        }
        
        // Clear the entry manager
        _entryManager.clear();
        
        // Clear the underlying array file
        try {
            _arrayFile.reset(_internalArray, _entryManager.getLWMark());
        } catch (IOException e) {
            _log.error(e.getMessage(), e);
        }
    }
    
    @Override
    public long get(int index) {
        return _internalArray[index];
    }
    
    @Override
    public void set(int index, long value, long scn) throws Exception {
        _internalArray[index] = value;
        _entryManager.addToPreFillEntryLong(index, value, scn);
    }
    
    @Override
    public void setCompactionAddress(int index, long address, long scn) throws Exception {
        _internalArray[index] = address;
        _entryManager.addToPreFillEntryLongCompaction(index, address, scn);
    }
    
    @Override
    public void expandCapacity(int index) throws Exception {
        // Do nothing
    }
    
    @Override
    public long[] getInternalArray() {
        return _internalArray;
    }
    
    public void wrap(LongArray newArray) throws Exception {
        if (length() != newArray.length()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        
        _internalArray = newArray.getInternalArray();
        _arrayFile.reset(_internalArray);
        _entryManager.clear();
    }
    
    @Override
    public EntryPersistListener getPersistListener() {
        return getEntryManager().getEntryPersistListener();
    }
    
    @Override
    public void setPersistListener(EntryPersistListener persistListener) {
        getEntryManager().setEntryPersistListener(persistListener);
    }

    @Override
    public synchronized void close() throws IOException {
        if(_mode == Mode.CLOSED) {
            return;
        }
        
        try {
            sync();
            _entryManager.clear();
            _arrayFile.close();
        } catch(Exception e) {
            throw (e instanceof IOException) ? (IOException)e : new IOException(e);
        } finally {
            _internalArray = null;
            _arrayFile = null;
            _length = 0;
            
            _mode = Mode.CLOSED;
        }
    }
    
    @Override
    public synchronized void open() throws IOException {
        if(_mode == Mode.OPEN) {
            return;
        }
        
        File file = new File(_directory, "indexes.dat");
        _arrayFile = openArrayFile(file, _length /* initial length */, 8);
        _length = _arrayFile.getArrayLength();
        
        this.init();
        this._mode = Mode.OPEN;
        
        getLogger().info("length:" + _length +
                        " entrySize:" + _entryManager.getMaxEntrySize() +
                        " maxEntries:" + _entryManager.getMaxEntries() + 
                        " directory:" + _directory.getAbsolutePath() +
                        " arrayFile:" + _arrayFile.getName());
    }
    
    @Override
    public boolean isOpen() {
        return _mode == Mode.OPEN;
    }
    
    @Override
    public final Array.Type getType() {
        return Array.Type.STATIC;
    }
}
