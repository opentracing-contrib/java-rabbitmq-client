/*
 * Copyright 2017-2019 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.contrib.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)
public class HeadersMapExtractAdapterTest {

    @Test
    @Parameters(method = "emptyValues")
    public void emptyHeadersMapExtract(Map<String, Object> inputMap) {
        //given params

        //when
        HeadersMapExtractAdapter adapter = new HeadersMapExtractAdapter(inputMap);

        //then
        assertFalse(adapter.iterator().hasNext());
    }

    @Test
    @Parameters(method = "nonEmptyValues")
    public void nonEmptyHeadersMapExtract(String header, Object value, String expectedKey, String expectedValue) {
        //given
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put(header, value);

        //when
        HeadersMapExtractAdapter adapter = new HeadersMapExtractAdapter(inputMap);

        //then
        assertTrue(adapter.iterator().hasNext());
        Map.Entry<String, String> entry = adapter.iterator().next();
        assertEquals(expectedValue, entry.getValue());
        assertEquals(expectedKey, entry.getKey());
    }

    @Parameters
    private Object[] emptyValues() {
        return new Object[]{
                new Object[]{null},
                new Object[]{new HashMap<>()},
        };
    }

    @Parameters
    private Object[] nonEmptyValues() {
        return new Object[]{
                new Object[]{"header1", "value1",  "header1", "value1"},
                new Object[]{"header2", "",        "header2", ""},
                new Object[]{"header3", null,      "header3", "null"},
                new Object[]{"header4", 1L,        "header4", "1"},
                new Object[]{"header5", false,     "header5", "false"},
        };
    }
}
