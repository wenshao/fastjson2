package p;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.ValueFilter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/** Context with a date format and any filter (as FastJsonConfig sets up) and maps with non-String keys. */
public class Q7e {
    public static class Resp {
        public Map<Date, String> byDate = new LinkedHashMap<>(Collections.singletonMap(new Date(86_400_000L), "a"));
        public Map<LocalDate, String> byLocalDate = new LinkedHashMap<>(Collections.singletonMap(LocalDate.of(2026, 10, 6), "b"));
        public Map<LocalDateTime, String> byLocalDateTime = new LinkedHashMap<>(Collections.singletonMap(LocalDateTime.of(2026, 10, 6, 1, 2, 3), "c"));
        public Date when = new Date(86_400_000L);
    }

    public static void main(String[] args) {
        System.out.println("jar " + JSON.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll(".*/", ""));
        JSONWriter.Context plain = new JSONWriter.Context();
        plain.setDateFormat("yyyy/MM/dd");
        System.out.println("dateFormat only           : " + JSON.toJSONString(new Resp(), plain));
        JSONWriter.Context withFilter = new JSONWriter.Context();
        withFilter.setDateFormat("yyyy/MM/dd");
        withFilter.configFilter((PropertyFilter) (o, n, v) -> true);
        System.out.println("dateFormat + no-op filter : " + JSON.toJSONString(new Resp(), withFilter));
        JSONWriter.Context nonString = new JSONWriter.Context(JSONWriter.Feature.WriteNonStringKeyAsString);
        nonString.setDateFormat("yyyy/MM/dd");
        nonString.configFilter((ValueFilter) (o, n, v) -> v);
        System.out.println("+ WriteNonStringKeyAsString: " + JSON.toJSONString(new Resp(), nonString));
        System.out.println("utf8 dateFormat + filter  : " + new String(JSON.toJSONBytes(new Resp(), java.nio.charset.StandardCharsets.UTF_8, withFilter), java.nio.charset.StandardCharsets.UTF_8));
    }
}
