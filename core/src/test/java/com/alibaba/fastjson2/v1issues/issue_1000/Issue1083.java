package com.alibaba.fastjson2.v1issues.issue_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wenshao on 11/06/2017.
 */
public class Issue1083 extends TestCase {
    public void test_for_issue() throws Exception {
        Map map = new HashMap();
        map.put("userId", 456);
        String json = JSON.toJSONString(map, JSONWriter.Feature.WriteNonStringValueAsString);
        assertEquals("{\"userId\":\"456\"}", json);
    }
}
