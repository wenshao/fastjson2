package com.alibaba.json.bvt.atomic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.concurrent.atomic.AtomicLongArray;

public class AtomicLongArrayFieldTest extends TestCase {

    public void test_codec_null() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, mapping, SerializerFeature.WriteMapNullValue);
        Assert.assertEquals("{\"value\":null}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        Assert.assertEquals(v1.getValue(), v.getValue());
    }

    public void test_codec_null_1() throws Exception {
        V0 v = new V0();

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, mapping, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty);
        Assert.assertEquals("{\"value\":[]}", text);
    }
    
    public void test_codec_null_2() throws Exception {
        V0 v = JSON.parseObject("{\"value\":[1,2]}", V0.class);

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);

        String text = JSON.toJSONString(v, mapping, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty);
        Assert.assertEquals("{\"value\":[1,2]}", text);
    }

    public static class V0 {

        private AtomicLongArray value;

        public AtomicLongArray getValue() {
            return value;
        }

        public void setValue(AtomicLongArray value) {
            this.value = value;
        }

    }
}
