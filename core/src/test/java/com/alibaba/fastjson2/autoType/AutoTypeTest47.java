package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2_vo.IntField1;
import com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase;
import org.junit.jupiter.api.Test;

import static junit.framework.TestCase.*;

public class AutoTypeTest47 {
    @Test
    public void test_0() throws Exception {
        String text = "{\"@type\":\"com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase\"}";
        JSONFactory.getDefaultObjectReaderProvider().setAutoTypeBeforeHandler(
                (String typeName, Class<?> expectClass, long features)
                        -> typeName.equals("com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase")
                        ? TypeUtils.loadClass(typeName)
                        : null
        );
        PoolBackedDataSourceBase dataSource = (PoolBackedDataSourceBase) JSON.parseObject(text, Object.class, JSONReader.Feature.SupportAutoType);
        assertNotNull(dataSource);
    }
}
