package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;

final class ObjectWriterImplEnum<E extends Enum<E>> extends ObjectWriterBaseModule.PrimitiveImpl {
    final Class defineClass;
    final Class enumType;
    final long features;

    byte[] typeNameJSONB;
    long typeNameHash;

    final Enum[] enumConstants;
    final String[] names;
    final long[] hashCodes;

    byte[][] jsonbNames;

    public ObjectWriterImplEnum(Class defineClass, Class enumType, long features) {
        this.defineClass = defineClass;
        this.enumType = enumType;
        this.features = features;

        this.enumConstants = (Enum[]) enumType.getEnumConstants();
        this.names = new String[enumConstants.length];
        this.hashCodes = new long[enumConstants.length];
        for (int i = 0; i < enumConstants.length; i++) {
            String name = enumConstants[i].name();
            this.names[i] = name;
            hashCodes[i] = Fnv.hashCode64(name);
        }
    }

    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            if (typeNameJSONB == null) {
                String typeName = TypeUtils.getTypeName(enumType);
                typeNameJSONB
                        = JSONB.toBytes(
                        typeName);
                typeNameHash = Fnv.hashCode64(typeName);
            }
            jsonWriter.writeTypeName(typeNameJSONB, typeNameHash);
        }

        Enum e = (Enum) object;
        if (jsonWriter.isEnabled(JSONWriter.Feature.WriteEnumUsingToString)) {
            jsonWriter.writeString(e.toString());
        } else {
            if (jsonbNames == null) {
                jsonbNames = new byte[this.names.length][];
            }

            int ordinal = e.ordinal();
            byte[] jsonbName = this.jsonbNames[ordinal];
            if (jsonbName == null) {
                jsonbName = JSONB.toBytes(this.names[ordinal]);
                this.jsonbNames[ordinal] = jsonbName;
            }
            jsonWriter.writeRaw(jsonbName);
        }
    }

    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        Enum e = (Enum) object;
        String str;
        if (jsonWriter.isEnabled(JSONWriter.Feature.WriteEnumUsingToString)) {
            str = e.toString();
        } else {
            str = e.name();
        }
        jsonWriter.writeString(str);
    }
}
