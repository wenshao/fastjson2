package com.alibaba.fastjson2.v1issues.issue_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import junit.framework.TestCase;

import java.util.Date;

public class Issue1772 extends TestCase {
    public void test_0() throws Exception {
        Date date = JSON.parseObject("\"-14189155200000\"", Date.class);
        assertEquals(-14189155200000L, date.getTime());
    }

    public void test_1() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("time", "-14189155200000");

        Model m = jsonObject.toJavaObject(Model.class);
        assertEquals(-14189155200000L, m.time.getTime());
    }

    public static class Model {
        public Date time;
    }
}
