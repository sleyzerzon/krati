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

package test.core.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;
import krati.core.array.basic.MemoryLongArray;

/**
 * TestMemoryLongArray
 * 
 * @author jwu
 * 06/09, 2011
 * 
 */
public class TestMemoryLongArray extends TestCase {
    final Random _rand = new Random();
    
    public void testApiBasics() {
        MemoryLongArray array = new MemoryLongArray();
        int length = array.length();
        int anyIndex = _rand.nextInt(length);
        onArray(array, anyIndex);
        
        anyIndex = length + _rand.nextInt(length);
        onArray(array, anyIndex);
        assertTrue(length < array.length());
        
        length = array.length();
        anyIndex = length + _rand.nextInt(length);
        onArray(array, anyIndex);
        assertTrue(length < array.length());
        
        length = array.length();
        anyIndex = length + _rand.nextInt(length << 5);
        onArray(array, anyIndex);
        assertTrue(length < array.length());
    }
    
    private void onArray(MemoryLongArray array, int anyIndex) {
        array.expandCapacity(anyIndex);
        int length = array.length();
        assertTrue(anyIndex < length);
        
        Map<Integer, Long> map = new HashMap<Integer, Long>();
        for(int i = 0; i < 10; i++) {
            int index = _rand.nextInt(length);
            long value = _rand.nextLong();
            map.put(index, value);
            array.set(index, value);
            assertEquals(value, array.get(index));
        }
        
        long[] internalArray = array.getInternalArray();
        assertEquals(length, internalArray.length);
        for(Map.Entry<Integer, Long> e : map.entrySet()) {
            assertEquals(e.getValue().longValue(), internalArray[e.getKey()]);
        }
        
        array.clear();
        for(Integer index: map.keySet()) {
            assertEquals(0, array.get(index));
        }
    }
}
