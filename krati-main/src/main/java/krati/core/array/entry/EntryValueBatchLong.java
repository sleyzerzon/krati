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

package krati.core.array.entry;

/**
 * EntryValueBatchLong
 * 
 * @author jwu
 * 
 */
public class EntryValueBatchLong extends EntryValueBatch {
    
    public EntryValueBatchLong() {
        this(1000);
    }
    
    public EntryValueBatchLong(int capacity) {
        /*
         * EntryValueLong int position; long value; long scn;
         */
        super(20, capacity);
    }
    
    public void add(int pos, long val, long scn) {
        _buffer.putInt(pos);  /* array position */
        _buffer.putLong(val); /* data value */
        _buffer.putLong(scn); /* SCN value */
    }
}
