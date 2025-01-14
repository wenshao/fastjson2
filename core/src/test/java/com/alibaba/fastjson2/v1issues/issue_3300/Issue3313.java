package com.alibaba.fastjson2.v1issues.issue_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import junit.framework.TestCase;
import lombok.Data;

/**
 * @Author ：Nanqi
 * @Date ：Created in 21:54 2020/6/30
 */
public class Issue3313 extends TestCase {
    public void test_for_issue() throws Exception {
        String jsonStr = "{\"NAME\":\"nanqi\",\"age\":18}";
        Model model = JSON.parseObject(jsonStr, Model.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(model.getAGe());
        assertNotNull(model.getName());
    }

    @Data
    static class Model {
        @JSONField(name = "NaMe")
        private String name;

        @JSONField(name = "age")
        private Integer aGe;
    }
}
