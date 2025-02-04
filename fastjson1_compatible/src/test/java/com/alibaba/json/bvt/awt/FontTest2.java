package com.alibaba.json.bvt.awt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import junit.framework.TestCase;
import org.junit.Assert;

import java.awt.*;

public class FontTest2 extends TestCase {

    public void test_color() throws Exception {
        Font[] fonts = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (Font font : fonts) {
            String text = JSON.toJSONString(font, SerializerFeature.WriteClassName);

            Font font2 = (Font) JSON.parse(text, Feature.SupportAutoType);

            Assert.assertEquals(font, font2);
        }
    }
}
