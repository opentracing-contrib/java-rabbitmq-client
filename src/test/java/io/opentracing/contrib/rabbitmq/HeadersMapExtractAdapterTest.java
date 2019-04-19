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
