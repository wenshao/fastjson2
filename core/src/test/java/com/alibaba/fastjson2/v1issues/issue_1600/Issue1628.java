package com.alibaba.fastjson2.v1issues.issue_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class Issue1628 extends TestCase {
    public void test_toJSONBytes() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", 1001);
        map.put("b", 2002);
        byte[] bytes = JSON.toJSONBytes(map, new SimplePropertyPreFilter("a"));
        assertEquals("{\"a\":1001}", new String(bytes));
    }

    public void test_toJSONBytes_1() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", 1001);
        map.put("b", 2002);
        byte[] bytes = JSON.toJSONBytes(map, new Filter[] {new SimplePropertyPreFilter("a")});
        assertEquals("{\"a\":1001}", new String(bytes));
    }

    public void test_toJSONBytes_2() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", 1001);
        map.put("b", 2002);
        byte[] bytes = JSON.toJSONBytes(map, new SimplePropertyPreFilter("a"));
        assertEquals("{\"a\":1001}", new String(bytes));
    }
}
