
# Compatibility Report

![semver PATCH](https://img.shields.io/badge/semver-PATCH-yellow?logo=semver "semver PATCH")

## Summary

> [!IMPORTANT]
>
> Compatible bug fixes found while checking backward compatibility of version `fastjson2-v5` with the previous version `fastjson2-main`.

<details markdown="1">
<summary>Expand to see options used.</summary>

- **Report only summary**: No
- **Report only changes**: Yes
- **Report only binary-incompatible changes**: No
- **Access modifier filter**: `PROTECTED`
- **Old archives**:
  - ![fastjson2-main unknown](https://img.shields.io/badge/fastjson2_main-unknown-blue "fastjson2-main unknown")
- **New archives**:
  - ![fastjson2-v5 unknown](https://img.shields.io/badge/fastjson2_v5-unknown-blue "fastjson2-v5 unknown")
- **Evaluate annotations**: Yes
- **Include synthetic classes and class members**: No
- **Include specific elements**: Yes
  - `com.alibaba.fastjson2.*`
- **Exclude specific elements**: No
- **Ignore all missing classes**: No
- **Ignore specific missing classes**: No
- **Treat changes as errors**:
  - Any changes: No
  - Binary incompatible changes: No
  - Source incompatible changes: No
  - Incompatible changes caused by excluded classes: Yes
  - Semantically incompatible changes: No
  - Semantically incompatible changes, including development versions: No
- **Classpath mode**: `ONE_COMMON_CLASSPATH`
- **Old classpath**:
```

```
- **New classpath**:
```

```

</details>


## Results

| Status   | Type                                                | Serialization       | Compatibility Changes |
|----------|-----------------------------------------------------|---------------------|-----------------------|
| Modified | [com.alibaba.fastjson2.JSONArray]                   | ![Compatible]       | ![Method added to public class] |
| Modified | [com.alibaba.fastjson2.JSONObject]                  | ![Compatible]       | ![Method added to public class] |
| Modified | [com.alibaba.fastjson2.JSONReader]                  | ![Not serializable] | ![Method added to public class] |
| Modified | [com.alibaba.fastjson2.JSONReader$Feature]          | ![Compatible]       | ![No changes]         |
| Modified | [com.alibaba.fastjson2.JSONWriter$Feature]          | ![Compatible]       | ![No changes]         |
| Modified | [com.alibaba.fastjson2.writer.ObjectWriterProvider] | ![Not serializable] | ![Method added to public class] |

<details markdown="1">
<summary>Expand for details.</summary>

___

<a id="user-content-com.alibaba.fastjson2.jsonarray"></a>
### `com.alibaba.fastjson2.JSONArray`

- [X] Binary-compatible
- [X] Source-compatible
- [X] Serialization-compatible

| Status   | Modifiers | Type  | Name        | Extends          | JDK   | Serialization | Compatibility Changes |
|----------|-----------|-------|-------------|------------------|-------|---------------|-----------------------|
| Modified | `public`  | Class | `JSONArray` | [`ArrayList<E>`] | JDK 8 | ![Compatible] | ![No changes]         |


#### Methods

| Status | Modifiers    | Generics | Type              | Method           | Annotations | Throws | Compatibility Changes |
|--------|--------------|----------|-------------------|------------------|-------------|--------|-----------------------|
| Added  | **`public`** |          | **[`JSONArray`]** | **`deepCopy`**() |             |        | ![Method added to public class] |

___

<a id="user-content-com.alibaba.fastjson2.jsonobject"></a>
### `com.alibaba.fastjson2.JSONObject`

- [X] Binary-compatible
- [X] Source-compatible
- [X] Serialization-compatible

| Status   | Modifiers | Type  | Name         | Extends                 | JDK   | Serialization | Compatibility Changes |
|----------|-----------|-------|--------------|-------------------------|-------|---------------|-----------------------|
| Modified | `public`  | Class | `JSONObject` | [`LinkedHashMap<K, V>`] | JDK 8 | ![Compatible] | ![No changes]         |


#### Methods

| Status | Modifiers    | Generics                     | Type               | Method                                   | Annotations | Throws | Compatibility Changes |
|--------|--------------|------------------------------|--------------------|------------------------------------------|-------------|--------|-----------------------|
| Added  | **`public`** |                              | **`boolean`**      | **`canConvertToInt`**([`String`])        |             |        | ![Method added to public class] |
| Added  | **`public`** |                              | **`boolean`**      | **`canConvertToLong`**([`String`])       |             |        | ![Method added to public class] |
| Added  | **`public`** |                              | **[`JSONObject`]** | **`deepCopy`**()                         |             |        | ![Method added to public class] |
| Added  | **`public`** |                              | **[`Object`]**     | **`required`**([`String`])               |             |        | ![Method added to public class] |
| Added  | **`public`** | \<**[`T extends Object`]**\> | **[`Object`]**     | **`required`**([`String`], [`Class<T>`]) |             |        | ![Method added to public class] |

___

<a id="user-content-com.alibaba.fastjson2.jsonreader"></a>
### `com.alibaba.fastjson2.JSONReader`

- [X] Binary-compatible
- [X] Source-compatible
- [X] Serialization-compatible

| Status   | Modifiers           | Type  | Name         | Extends    | JDK   | Serialization       | Compatibility Changes |
|----------|---------------------|-------|--------------|------------|-------|---------------------|-----------------------|
| Modified | `public` `abstract` | Class | `JSONReader` | [`Object`] | JDK 8 | ![Not serializable] | ![No changes]         |


#### Methods

| Status | Modifiers    | Generics | Type                        | Method                   | Annotations | Throws | Compatibility Changes |
|--------|--------------|----------|-----------------------------|--------------------------|-------------|--------|-----------------------|
| Added  | **`public`** |          | **[`Map<String, Object>`]** | **`readObject`**(`long`) |             |        | ![Method added to public class] |

___

<a id="user-content-com.alibaba.fastjson2.jsonreader$feature"></a>
### `com.alibaba.fastjson2.JSONReader$Feature`

- [X] Binary-compatible
- [X] Source-compatible
- [X] Serialization-compatible

| Status   | Modifiers                 | Type | Name      | Extends     | JDK   | Serialization | Compatibility Changes |
|----------|---------------------------|------|-----------|-------------|-------|---------------|-----------------------|
| Modified | `final` `static` `public` | Enum | `Feature` | [`Enum<E>`] | JDK 8 | ![Compatible] | ![No changes]         |


#### Fields

| Status | Modifiers                             | Type            | Name                   | Annotations | Compatibility Changes |
|--------|---------------------------------------|-----------------|------------------------|-------------|-----------------------|
| Added  | **`public`** **`static`** **`final`** | **[`Feature`]** | `ErrorOnDuplicateKeys` |             | ![No changes]         |

___

<a id="user-content-com.alibaba.fastjson2.jsonwriter$feature"></a>
### `com.alibaba.fastjson2.JSONWriter$Feature`

- [X] Binary-compatible
- [X] Source-compatible
- [X] Serialization-compatible

| Status   | Modifiers                 | Type | Name      | Extends     | JDK   | Serialization | Compatibility Changes |
|----------|---------------------------|------|-----------|-------------|-------|---------------|-----------------------|
| Modified | `final` `static` `public` | Enum | `Feature` | [`Enum<E>`] | JDK 8 | ![Compatible] | ![No changes]         |


#### Fields

| Status | Modifiers                             | Type               | Name                           | Annotations | Compatibility Changes |
|--------|---------------------------------------|--------------------|--------------------------------|-------------|-----------------------|
| Added  | **`public`** **`static`** **`final`** | **[`Feature`][1]** | `SortFieldNamesAlphabetically` |             | ![No changes]         |

___

<a id="user-content-com.alibaba.fastjson2.writer.objectwriterprovider"></a>
### `com.alibaba.fastjson2.writer.ObjectWriterProvider`

- [X] Binary-compatible
- [X] Source-compatible
- [X] Serialization-compatible

| Status   | Modifiers | Type  | Name                   | Extends    | JDK   | Serialization       | Compatibility Changes |
|----------|-----------|-------|------------------------|------------|-------|---------------------|-----------------------|
| Modified | `public`  | Class | `ObjectWriterProvider` | [`Object`] | JDK 8 | ![Not serializable] | ![No changes]         |


#### Methods

| Status | Modifiers    | Generics | Type                 | Method                                                         | Annotations | Throws | Compatibility Changes |
|--------|--------------|----------|----------------------|----------------------------------------------------------------|-------------|--------|-----------------------|
| Added  | **`public`** |          | **[`ObjectWriter`]** | **`getObjectWriter`**([`Type`], [`Class`], [`String`], `long`) |             |        | ![Method added to public class] |
| Added  | **`public`** |          | **[`ObjectWriter`]** | **`getObjectWriter`**([`Type`], [`Class`], `long`)             |             |        | ![Method added to public class] |


</details>


___

*Generated on: 2026-10-05 22:31:59.580+0800*.

[1]: # "com.alibaba.fastjson2.JSONWriter$Feature"
[Compatible]: https://img.shields.io/badge/Compatible-green "Compatible"
[Method added to public class]: https://img.shields.io/badge/Method_added_to_public_class-yellow "Method added to public class"
[No changes]: https://img.shields.io/badge/No_changes-green "No changes"
[Not serializable]: https://img.shields.io/badge/Not_serializable-green "Not serializable"
[`ArrayList<E>`]: # "java.util.ArrayList<E extends java.lang.Object>"
[`Class<T>`]: # "java.lang.Class<T>"
[`Class`]: # "java.lang.Class"
[`Enum<E>`]: # "java.lang.Enum<E extends java.lang.Enum<E>>"
[`Feature`]: # "com.alibaba.fastjson2.JSONReader$Feature"
[`JSONArray`]: # "com.alibaba.fastjson2.JSONArray"
[`JSONObject`]: # "com.alibaba.fastjson2.JSONObject"
[`LinkedHashMap<K, V>`]: # "java.util.LinkedHashMap<K extends java.lang.Object, V extends java.lang.Object>"
[`Map<String, Object>`]: # "java.util.Map<java.lang.String, java.lang.Object>"
[`ObjectWriter`]: # "com.alibaba.fastjson2.writer.ObjectWriter"
[`Object`]: # "java.lang.Object"
[`String`]: # "java.lang.String"
[`T extends Object`]: # "T extends java.lang.Object"
[`Type`]: # "java.lang.reflect.Type"
[com.alibaba.fastjson2.JSONArray]: #user-content-com.alibaba.fastjson2.jsonarray
[com.alibaba.fastjson2.JSONObject]: #user-content-com.alibaba.fastjson2.jsonobject
[com.alibaba.fastjson2.JSONReader]: #user-content-com.alibaba.fastjson2.jsonreader
[com.alibaba.fastjson2.JSONReader$Feature]: #user-content-com.alibaba.fastjson2.jsonreader$feature
[com.alibaba.fastjson2.JSONWriter$Feature]: #user-content-com.alibaba.fastjson2.jsonwriter$feature
[com.alibaba.fastjson2.writer.ObjectWriterProvider]: #user-content-com.alibaba.fastjson2.writer.objectwriterprovider

