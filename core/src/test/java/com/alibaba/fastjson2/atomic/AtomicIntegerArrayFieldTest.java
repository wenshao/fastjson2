package com.alibaba.fastjson2.atomic;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicIntegerArrayFieldTest extends TestCase {

    public void test_codec_null() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls);
        Assert.assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        Assert.assertEquals(v1.getValue(), v.getValue());
    }

    public void test_codec_null_1() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue);
        Assert.assertEquals("{\"value\":[]}", text);
    }
    
    public void test_codec_null_2() throws Exception {
        V0 v = JSON.parseObject("{\"value\":[1,2]}", V0.class);

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue);
        Assert.assertEquals("{\"value\":[1,2]}", text);
    }

    public static class V0 {

        private AtomicIntegerArray value;

        public AtomicIntegerArray getValue() {
            return value;
        }

        public void setValue(AtomicIntegerArray value) {
            this.value = value;
        }

    }
}