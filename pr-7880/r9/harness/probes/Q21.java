package p21;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.lang.reflect.Array;
import java.util.*;

public class Q21 {
    public enum E { A, B }
    public static class B { public int id; public String name; public Object o; public E e; public Integer n; public byte[] bytes; public boolean flag; }
    public static class Sub extends B { public String extra; }
    public static class PL { public List<B> f; }
    public static class PA { public B[] f; }
    public static class PS { public Set<B> f; }
    public static class PM { public Map<String, B> f; }
    public static class PO { public B f; }
    public static class L0 { @JSONField(deserializeFeatures = JSONReader.Feature.FieldBased) public List<B> f; }
    public static class A0 { @JSONField(deserializeFeatures = JSONReader.Feature.FieldBased) public B[] f; }
    public static class S0 { @JSONField(deserializeFeatures = JSONReader.Feature.FieldBased) public Set<B> f; }
    public static class M0 { @JSONField(deserializeFeatures = JSONReader.Feature.FieldBased) public Map<String, B> f; }
    public static class O0 { @JSONField(deserializeFeatures = JSONReader.Feature.FieldBased) public B f; }
    public static class L1 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreNoneSerializable) public List<B> f; }
    public static class A1 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreNoneSerializable) public B[] f; }
    public static class S1 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreNoneSerializable) public Set<B> f; }
    public static class M1 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreNoneSerializable) public Map<String, B> f; }
    public static class O1 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreNoneSerializable) public B f; }
    public static class L2 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNoneSerializable) public List<B> f; }
    public static class A2 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNoneSerializable) public B[] f; }
    public static class S2 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNoneSerializable) public Set<B> f; }
    public static class M2 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNoneSerializable) public Map<String, B> f; }
    public static class O2 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNoneSerializable) public B f; }
    public static class L3 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportArrayToBean) public List<B> f; }
    public static class A3 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportArrayToBean) public B[] f; }
    public static class S3 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportArrayToBean) public Set<B> f; }
    public static class M3 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportArrayToBean) public Map<String, B> f; }
    public static class O3 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportArrayToBean) public B f; }
    public static class L4 { @JSONField(deserializeFeatures = JSONReader.Feature.InitStringFieldAsEmpty) public List<B> f; }
    public static class A4 { @JSONField(deserializeFeatures = JSONReader.Feature.InitStringFieldAsEmpty) public B[] f; }
    public static class S4 { @JSONField(deserializeFeatures = JSONReader.Feature.InitStringFieldAsEmpty) public Set<B> f; }
    public static class M4 { @JSONField(deserializeFeatures = JSONReader.Feature.InitStringFieldAsEmpty) public Map<String, B> f; }
    public static class O4 { @JSONField(deserializeFeatures = JSONReader.Feature.InitStringFieldAsEmpty) public B f; }
    public static class L5 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportAutoType) public List<B> f; }
    public static class A5 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportAutoType) public B[] f; }
    public static class S5 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportAutoType) public Set<B> f; }
    public static class M5 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportAutoType) public Map<String, B> f; }
    public static class O5 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportAutoType) public B f; }
    public static class L6 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportSmartMatch) public List<B> f; }
    public static class A6 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportSmartMatch) public B[] f; }
    public static class S6 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportSmartMatch) public Set<B> f; }
    public static class M6 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportSmartMatch) public Map<String, B> f; }
    public static class O6 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportSmartMatch) public B f; }
    public static class L7 { @JSONField(deserializeFeatures = JSONReader.Feature.UseNativeObject) public List<B> f; }
    public static class A7 { @JSONField(deserializeFeatures = JSONReader.Feature.UseNativeObject) public B[] f; }
    public static class S7 { @JSONField(deserializeFeatures = JSONReader.Feature.UseNativeObject) public Set<B> f; }
    public static class M7 { @JSONField(deserializeFeatures = JSONReader.Feature.UseNativeObject) public Map<String, B> f; }
    public static class O7 { @JSONField(deserializeFeatures = JSONReader.Feature.UseNativeObject) public B f; }
    public static class L8 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportClassForName) public List<B> f; }
    public static class A8 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportClassForName) public B[] f; }
    public static class S8 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportClassForName) public Set<B> f; }
    public static class M8 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportClassForName) public Map<String, B> f; }
    public static class O8 { @JSONField(deserializeFeatures = JSONReader.Feature.SupportClassForName) public B f; }
    public static class L9 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreSetNullValue) public List<B> f; }
    public static class A9 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreSetNullValue) public B[] f; }
    public static class S9 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreSetNullValue) public Set<B> f; }
    public static class M9 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreSetNullValue) public Map<String, B> f; }
    public static class O9 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreSetNullValue) public B f; }
    public static class L10 { @JSONField(deserializeFeatures = JSONReader.Feature.UseDefaultConstructorAsPossible) public List<B> f; }
    public static class A10 { @JSONField(deserializeFeatures = JSONReader.Feature.UseDefaultConstructorAsPossible) public B[] f; }
    public static class S10 { @JSONField(deserializeFeatures = JSONReader.Feature.UseDefaultConstructorAsPossible) public Set<B> f; }
    public static class M10 { @JSONField(deserializeFeatures = JSONReader.Feature.UseDefaultConstructorAsPossible) public Map<String, B> f; }
    public static class O10 { @JSONField(deserializeFeatures = JSONReader.Feature.UseDefaultConstructorAsPossible) public B f; }
    public static class L11 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigDecimalForFloats) public List<B> f; }
    public static class A11 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigDecimalForFloats) public B[] f; }
    public static class S11 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigDecimalForFloats) public Set<B> f; }
    public static class M11 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigDecimalForFloats) public Map<String, B> f; }
    public static class O11 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigDecimalForFloats) public B f; }
    public static class L12 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigDecimalForDoubles) public List<B> f; }
    public static class A12 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigDecimalForDoubles) public B[] f; }
    public static class S12 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigDecimalForDoubles) public Set<B> f; }
    public static class M12 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigDecimalForDoubles) public Map<String, B> f; }
    public static class O12 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigDecimalForDoubles) public B f; }
    public static class L13 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnEnumNotMatch) public List<B> f; }
    public static class A13 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnEnumNotMatch) public B[] f; }
    public static class S13 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnEnumNotMatch) public Set<B> f; }
    public static class M13 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnEnumNotMatch) public Map<String, B> f; }
    public static class O13 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnEnumNotMatch) public B f; }
    public static class L14 { @JSONField(deserializeFeatures = JSONReader.Feature.TrimString) public List<B> f; }
    public static class A14 { @JSONField(deserializeFeatures = JSONReader.Feature.TrimString) public B[] f; }
    public static class S14 { @JSONField(deserializeFeatures = JSONReader.Feature.TrimString) public Set<B> f; }
    public static class M14 { @JSONField(deserializeFeatures = JSONReader.Feature.TrimString) public Map<String, B> f; }
    public static class O14 { @JSONField(deserializeFeatures = JSONReader.Feature.TrimString) public B f; }
    public static class L15 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNotSupportAutoType) public List<B> f; }
    public static class A15 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNotSupportAutoType) public B[] f; }
    public static class S15 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNotSupportAutoType) public Set<B> f; }
    public static class M15 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNotSupportAutoType) public Map<String, B> f; }
    public static class O15 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNotSupportAutoType) public B f; }
    public static class L16 { @JSONField(deserializeFeatures = JSONReader.Feature.DuplicateKeyValueAsArray) public List<B> f; }
    public static class A16 { @JSONField(deserializeFeatures = JSONReader.Feature.DuplicateKeyValueAsArray) public B[] f; }
    public static class S16 { @JSONField(deserializeFeatures = JSONReader.Feature.DuplicateKeyValueAsArray) public Set<B> f; }
    public static class M16 { @JSONField(deserializeFeatures = JSONReader.Feature.DuplicateKeyValueAsArray) public Map<String, B> f; }
    public static class O16 { @JSONField(deserializeFeatures = JSONReader.Feature.DuplicateKeyValueAsArray) public B f; }
    public static class L17 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public List<B> f; }
    public static class A17 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public B[] f; }
    public static class S17 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Set<B> f; }
    public static class M17 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<String, B> f; }
    public static class O17 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public B f; }
    public static class L18 { @JSONField(deserializeFeatures = JSONReader.Feature.AllowUnQuotedFieldNames) public List<B> f; }
    public static class A18 { @JSONField(deserializeFeatures = JSONReader.Feature.AllowUnQuotedFieldNames) public B[] f; }
    public static class S18 { @JSONField(deserializeFeatures = JSONReader.Feature.AllowUnQuotedFieldNames) public Set<B> f; }
    public static class M18 { @JSONField(deserializeFeatures = JSONReader.Feature.AllowUnQuotedFieldNames) public Map<String, B> f; }
    public static class O18 { @JSONField(deserializeFeatures = JSONReader.Feature.AllowUnQuotedFieldNames) public B f; }
    public static class L19 { @JSONField(deserializeFeatures = JSONReader.Feature.NonStringKeyAsString) public List<B> f; }
    public static class A19 { @JSONField(deserializeFeatures = JSONReader.Feature.NonStringKeyAsString) public B[] f; }
    public static class S19 { @JSONField(deserializeFeatures = JSONReader.Feature.NonStringKeyAsString) public Set<B> f; }
    public static class M19 { @JSONField(deserializeFeatures = JSONReader.Feature.NonStringKeyAsString) public Map<String, B> f; }
    public static class O19 { @JSONField(deserializeFeatures = JSONReader.Feature.NonStringKeyAsString) public B f; }
    public static class L20 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreCheckClose) public List<B> f; }
    public static class A20 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreCheckClose) public B[] f; }
    public static class S20 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreCheckClose) public Set<B> f; }
    public static class M20 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreCheckClose) public Map<String, B> f; }
    public static class O20 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreCheckClose) public B f; }
    public static class L21 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNullForPrimitives) public List<B> f; }
    public static class A21 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNullForPrimitives) public B[] f; }
    public static class S21 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNullForPrimitives) public Set<B> f; }
    public static class M21 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNullForPrimitives) public Map<String, B> f; }
    public static class O21 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnNullForPrimitives) public B f; }
    public static class L22 { @JSONField(deserializeFeatures = JSONReader.Feature.NullOnError) public List<B> f; }
    public static class A22 { @JSONField(deserializeFeatures = JSONReader.Feature.NullOnError) public B[] f; }
    public static class S22 { @JSONField(deserializeFeatures = JSONReader.Feature.NullOnError) public Set<B> f; }
    public static class M22 { @JSONField(deserializeFeatures = JSONReader.Feature.NullOnError) public Map<String, B> f; }
    public static class O22 { @JSONField(deserializeFeatures = JSONReader.Feature.NullOnError) public B f; }
    public static class L23 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreAutoTypeNotMatch) public List<B> f; }
    public static class A23 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreAutoTypeNotMatch) public B[] f; }
    public static class S23 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreAutoTypeNotMatch) public Set<B> f; }
    public static class M23 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreAutoTypeNotMatch) public Map<String, B> f; }
    public static class O23 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreAutoTypeNotMatch) public B f; }
    public static class L24 { @JSONField(deserializeFeatures = JSONReader.Feature.NonZeroNumberCastToBooleanAsTrue) public List<B> f; }
    public static class A24 { @JSONField(deserializeFeatures = JSONReader.Feature.NonZeroNumberCastToBooleanAsTrue) public B[] f; }
    public static class S24 { @JSONField(deserializeFeatures = JSONReader.Feature.NonZeroNumberCastToBooleanAsTrue) public Set<B> f; }
    public static class M24 { @JSONField(deserializeFeatures = JSONReader.Feature.NonZeroNumberCastToBooleanAsTrue) public Map<String, B> f; }
    public static class O24 { @JSONField(deserializeFeatures = JSONReader.Feature.NonZeroNumberCastToBooleanAsTrue) public B f; }
    public static class L25 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreNullPropertyValue) public List<B> f; }
    public static class A25 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreNullPropertyValue) public B[] f; }
    public static class S25 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreNullPropertyValue) public Set<B> f; }
    public static class M25 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreNullPropertyValue) public Map<String, B> f; }
    public static class O25 { @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreNullPropertyValue) public B f; }
    public static class L26 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnUnknownProperties) public List<B> f; }
    public static class A26 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnUnknownProperties) public B[] f; }
    public static class S26 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnUnknownProperties) public Set<B> f; }
    public static class M26 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnUnknownProperties) public Map<String, B> f; }
    public static class O26 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnUnknownProperties) public B f; }
    public static class L27 { @JSONField(deserializeFeatures = JSONReader.Feature.EmptyStringAsNull) public List<B> f; }
    public static class A27 { @JSONField(deserializeFeatures = JSONReader.Feature.EmptyStringAsNull) public B[] f; }
    public static class S27 { @JSONField(deserializeFeatures = JSONReader.Feature.EmptyStringAsNull) public Set<B> f; }
    public static class M27 { @JSONField(deserializeFeatures = JSONReader.Feature.EmptyStringAsNull) public Map<String, B> f; }
    public static class O27 { @JSONField(deserializeFeatures = JSONReader.Feature.EmptyStringAsNull) public B f; }
    public static class L28 { @JSONField(deserializeFeatures = JSONReader.Feature.NonErrorOnNumberOverflow) public List<B> f; }
    public static class A28 { @JSONField(deserializeFeatures = JSONReader.Feature.NonErrorOnNumberOverflow) public B[] f; }
    public static class S28 { @JSONField(deserializeFeatures = JSONReader.Feature.NonErrorOnNumberOverflow) public Set<B> f; }
    public static class M28 { @JSONField(deserializeFeatures = JSONReader.Feature.NonErrorOnNumberOverflow) public Map<String, B> f; }
    public static class O28 { @JSONField(deserializeFeatures = JSONReader.Feature.NonErrorOnNumberOverflow) public B f; }
    public static class L29 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigIntegerForInts) public List<B> f; }
    public static class A29 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigIntegerForInts) public B[] f; }
    public static class S29 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigIntegerForInts) public Set<B> f; }
    public static class M29 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigIntegerForInts) public Map<String, B> f; }
    public static class O29 { @JSONField(deserializeFeatures = JSONReader.Feature.UseBigIntegerForInts) public B f; }
    public static class L30 { @JSONField(deserializeFeatures = JSONReader.Feature.UseLongForInts) public List<B> f; }
    public static class A30 { @JSONField(deserializeFeatures = JSONReader.Feature.UseLongForInts) public B[] f; }
    public static class S30 { @JSONField(deserializeFeatures = JSONReader.Feature.UseLongForInts) public Set<B> f; }
    public static class M30 { @JSONField(deserializeFeatures = JSONReader.Feature.UseLongForInts) public Map<String, B> f; }
    public static class O30 { @JSONField(deserializeFeatures = JSONReader.Feature.UseLongForInts) public B f; }
    public static class L31 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableSingleQuote) public List<B> f; }
    public static class A31 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableSingleQuote) public B[] f; }
    public static class S31 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableSingleQuote) public Set<B> f; }
    public static class M31 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableSingleQuote) public Map<String, B> f; }
    public static class O31 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableSingleQuote) public B f; }
    public static class L32 { @JSONField(deserializeFeatures = JSONReader.Feature.UseDoubleForDecimals) public List<B> f; }
    public static class A32 { @JSONField(deserializeFeatures = JSONReader.Feature.UseDoubleForDecimals) public B[] f; }
    public static class S32 { @JSONField(deserializeFeatures = JSONReader.Feature.UseDoubleForDecimals) public Set<B> f; }
    public static class M32 { @JSONField(deserializeFeatures = JSONReader.Feature.UseDoubleForDecimals) public Map<String, B> f; }
    public static class O32 { @JSONField(deserializeFeatures = JSONReader.Feature.UseDoubleForDecimals) public B f; }
    public static class L33 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableReferenceDetect) public List<B> f; }
    public static class A33 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableReferenceDetect) public B[] f; }
    public static class S33 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableReferenceDetect) public Set<B> f; }
    public static class M33 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableReferenceDetect) public Map<String, B> f; }
    public static class O33 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableReferenceDetect) public B f; }
    public static class L34 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableStringArrayUnwrapping) public List<B> f; }
    public static class A34 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableStringArrayUnwrapping) public B[] f; }
    public static class S34 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableStringArrayUnwrapping) public Set<B> f; }
    public static class M34 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableStringArrayUnwrapping) public Map<String, B> f; }
    public static class O34 { @JSONField(deserializeFeatures = JSONReader.Feature.DisableStringArrayUnwrapping) public B f; }
    static final String[] FEATURES = {"FieldBased", "IgnoreNoneSerializable", "ErrorOnNoneSerializable", "SupportArrayToBean", "InitStringFieldAsEmpty", "SupportAutoType", "SupportSmartMatch", "UseNativeObject", "SupportClassForName", "IgnoreSetNullValue", "UseDefaultConstructorAsPossible", "UseBigDecimalForFloats", "UseBigDecimalForDoubles", "ErrorOnEnumNotMatch", "TrimString", "ErrorOnNotSupportAutoType", "DuplicateKeyValueAsArray", "ErrorOnDuplicateKeys", "AllowUnQuotedFieldNames", "NonStringKeyAsString", "IgnoreCheckClose", "ErrorOnNullForPrimitives", "NullOnError", "IgnoreAutoTypeNotMatch", "NonZeroNumberCastToBooleanAsTrue", "IgnoreNullPropertyValue", "ErrorOnUnknownProperties", "EmptyStringAsNull", "NonErrorOnNumberOverflow", "UseBigIntegerForInts", "UseLongForInts", "DisableSingleQuote", "UseDoubleForDecimals", "DisableReferenceDetect", "DisableStringArrayUnwrapping"};
    static final String[][] SHAPES = {{"L", "[%s]"}, {"A", "[%s]"}, {"S", "[%s]"}, {"M", "{\"k\":%s}"}, {"O", "%s"}};
    static final String[][] ITEMS = {{"plain", "{\"id\":1,\"name\":\"a\"}"}, {"array", "[1,\"a\"]"}, {"case", "{\"ID\":1,\"Name\":\"a\"}"}, {"snake", "{\"id\":1,\"na_me\":\"a\"}"}, {"autotype-sub", "{\"@type\":\"p21.Q21$Sub\",\"id\":1,\"extra\":\"x\"}"}, {"autotype-other", "{\"@type\":\"java.util.HashMap\",\"id\":1}"}, {"unknown", "{\"id\":1,\"zzz\":2}"}, {"badint", "{\"id\":\"x\"}"}, {"trim", "{\"id\":1,\"name\":\" a \"}"}, {"nostring", "{\"id\":1}"}, {"double", "{\"id\":1,\"o\":1.5}"}, {"bigint", "{\"id\":1,\"o\":12345678901234567890}"}, {"int", "{\"id\":1,\"o\":7}"}, {"enum", "{\"id\":1,\"e\":\"NOPE\"}"}, {"dup", "{\"id\":1,\"id\":2}"}, {"nullprim", "{\"id\":null}"}, {"nested", "{\"id\":1,\"o\":{\"k\":[1]}}"}, {"base64", "{\"id\":1,\"bytes\":\"AQI=\"}"}, {"emptystr", "{\"id\":1,\"n\":\"\"}"}, {"ref", "{\"id\":1,\"o\":{\"$ref\":\"$\"}}"}, {"nullname", "{\"id\":1,\"name\":null}"}, {"numname", "{\"id\":1,\"name\":5}"}, {"bool", "{\"id\":1,\"o\":true,\"flag\":2}"}, {"overflow", "{\"id\":99999999999}"}};

    static String desc(Object v) {
        if (v == null) return "null";
        if (v instanceof B) {
            B b = (B) v;
            String s = v.getClass().getSimpleName() + "(id=" + b.id + ",name=" + q(b.name) + ",o=" + desc(b.o) + ",e=" + b.e + ",n=" + b.n
                    + ",bytes=" + (b.bytes == null ? "null" : Arrays.toString(b.bytes)) + ",flag=" + b.flag;
            if (v instanceof Sub) s += ",extra=" + ((Sub) v).extra;
            return s + ")";
        }
        if (v instanceof Map) {
            StringBuilder sb = new StringBuilder(v.getClass().getSimpleName()).append('{');
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) sb.append(desc(e.getKey())).append('=').append(e.getValue() == v ? "<self>" : desc(e.getValue())).append(',');
            return sb.append('}').toString();
        }
        if (v instanceof Collection) {
            StringBuilder sb = new StringBuilder(v.getClass().getSimpleName()).append('[');
            for (Object o : (Collection<?>) v) sb.append(desc(o)).append(',');
            return sb.append(']').toString();
        }
        if (v.getClass().isArray()) {
            StringBuilder sb = new StringBuilder(v.getClass().getComponentType().getSimpleName()).append("[]{");
            for (int i = 0; i < Array.getLength(v); i++) sb.append(desc(Array.get(v, i))).append(',');
            return sb.append('}').toString();
        }
        if (v instanceof String) return q((String) v);
        return v.getClass().getSimpleName() + ":" + v;
    }

    static String q(String s) {
        return s == null ? "null" : "'" + s + "'";
    }

    static String parse(ObjectReaderProvider p, Class<?> c, String json, JSONReader.Feature... f) {
        try (JSONReader r = JSONReader.of(json, new JSONReader.Context(p, f))) {
            Object o = r.read(c);
            return desc(o == null ? null : c.getField("f").get(o));
        } catch (Throwable e) {
            String m = String.valueOf(e.getMessage());
            int k = m.indexOf(", offset");
            if (k > 0) m = m.substring(0, k);
            if (m.length() > 70) m = m.substring(0, 70);
            return "EXC " + e.getClass().getSimpleName() + ": " + m;
        }
    }

    public static void main(String[] args) throws Exception {
        String only = args.length > 0 ? args[0] : null;
        for (String cn : new String[]{"asm", "reflect"}) {
            for (int i = 0; i < FEATURES.length; i++) {
                if (only != null && !only.equals(FEATURES[i])) continue;
                JSONReader.Feature x;
                try {
                    x = JSONReader.Feature.valueOf(FEATURES[i]);
                } catch (IllegalArgumentException e) {
                    x = null;
                }
                for (String[] shape : SHAPES) {
                    ObjectReaderProvider p = new ObjectReaderProvider("asm".equals(cn) ? ObjectReaderCreatorASM.INSTANCE : ObjectReaderCreator.INSTANCE);
                    Class<?> annotated = Class.forName("p21.Q21$" + shape[0] + i);
                    Class<?> plain = Class.forName("p21.Q21$P" + shape[0]);
                    for (String[] item : ITEMS) {
                        String json = "{\"f\":" + String.format(shape[1], item[1]) + "}";
                        String field = x == null ? "n/a" : parse(p, annotated, json);
                        String ctx = x == null ? "n/a" : parse(p, plain, json, x);
                        String base = parse(p, plain, json);
                        System.out.println(cn + "\t" + FEATURES[i] + "\t" + shape[0] + "\t" + item[0] + "\tfield=" + field + "\tcontext=" + ctx + "\tbase=" + base);
                    }
                }
            }
        }
    }
}
