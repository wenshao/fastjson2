package com.alibaba.json.bvt.date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import junit.framework.TestCase;
import org.junit.Assert;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateFieldTest6 extends TestCase {
    protected void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = Locale.CHINA;
    }
    
	public void test_0() throws Exception {
//		SerializeConfig mapping = new SerializeConfig();
//		mapping.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));

		Entity object = new Entity();
		object.setValue(new Date());
		String text = JSON.toJSONStringWithDateFormat(object, "yyyy-MM-dd");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", JSON.defaultLocale);
        format.setTimeZone(JSON.defaultTimeZone);
		Assert.assertEquals("{\"value\":\"" + format.format(object.getValue()) + "\"}", text);
	}

	public static class Entity {
		private Date value;

		public Date getValue() {
			return value;
		}

		public void setValue(Date value) {
			this.value = value;
		}

	}
}
