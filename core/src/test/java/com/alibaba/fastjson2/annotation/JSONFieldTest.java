package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorLambda;
import org.junit.jupiter.api.*;

import java.util.TimeZone;

import static junit.framework.TestCase.assertEquals;

public class JSONFieldTest {
    @Test
    public void test_format_iso8601() {
        ObjectWriterCreator[] creators = new ObjectWriterCreator[]{
                ObjectWriterCreator.INSTANCE,
                ObjectWriterCreatorLambda.INSTANCE,
                ObjectWriterCreatorASM.INSTANCE
        };

        VO vo = new VO();
        vo.date = 0;

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<VO> objectWriter = creator
                    .createObjectWriter(
                            VO.class, 0, JSONFactory.getDefaultObjectWriterProvider().getModules());
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            objectWriter.write(jsonWriter, vo, null, null, 0);

            int rawOffset = TimeZone.getDefault().getRawOffset();
            if (rawOffset == 0) {
                assertEquals("{\"date\":\"1970-01-01T00:00:00Z\"}", jsonWriter.toString());
            } else {
                assertEquals("{\"date\":\"1970-01-01T08:00:00+08:00\"}", jsonWriter.toString());
            }
        }
    }

    @Test
    public void test_format_iso8601_method() {
        ObjectWriterCreator[] creators = new ObjectWriterCreator[]{
                ObjectWriterCreator.INSTANCE,
                ObjectWriterCreatorLambda.INSTANCE,
                ObjectWriterCreatorASM.INSTANCE
        };

        VO_Method vo = new VO_Method();
        vo.date = 0;

        for (ObjectWriterCreator creator : creators) {
            ObjectWriter<VO_Method> objectWriter = creator
                    .createObjectWriter(
                            VO_Method.class, 0, JSONFactory.getDefaultObjectWriterProvider().getModules());
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            objectWriter.write(jsonWriter, vo, null, null, 0);

            int rawOffset = TimeZone.getDefault().getRawOffset();
            if (rawOffset == 0) {
                assertEquals("{\"date\":\"1970-01-01T00:00:00Z\"}", jsonWriter.toString());
            } else {
                assertEquals("{\"date\":\"1970-01-01T08:00:00+08:00\"}", jsonWriter.toString());
            }
        }
    }

    public static class VO {
        @JSONField(format = "iso8601")
        public long date;
    }


    public static class VO_Method {
        private long date;

        @JSONField(format = "iso8601")
        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }
    }
}
