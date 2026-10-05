
# Compatibility Report

![semver MINOR](https://img.shields.io/badge/semver-MINOR-orange?logo=semver "semver MINOR")

## Summary

> [!WARNING]
>
> Compatible changes found while checking backward compatibility of version `fastjson2-v3` with the previous version `fastjson2-v2`.

<details markdown="1">
<summary>Expand to see options used.</summary>

- **Report only summary**: No
- **Report only changes**: Yes
- **Report only binary-incompatible changes**: No
- **Access modifier filter**: `PROTECTED`
- **Old archives**:
  - ![fastjson2-v2 unknown](https://img.shields.io/badge/fastjson2_v2-unknown-blue "fastjson2-v2 unknown")
- **New archives**:
  - ![fastjson2-v3 unknown](https://img.shields.io/badge/fastjson2_v3-unknown-blue "fastjson2-v3 unknown")
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

| Status   | Type                               | Serialization       | Compatibility Changes |
|----------|------------------------------------|---------------------|-----------------------|
| Modified | [com.alibaba.fastjson2.JSON]       | ![Not serializable] | ![Method new static added to interface] |
| Modified | [com.alibaba.fastjson2.JSONReader] | ![Not serializable] | ![Method added to public class] |

<details markdown="1">
<summary>Expand for details.</summary>

___

<a id="user-content-com.alibaba.fastjson2.json"></a>
### `com.alibaba.fastjson2.JSON`

- [X] Binary-compatible
- [X] Source-compatible
- [X] Serialization-compatible

| Status   | Modifiers           | Type      | Name   | Extends    | JDK   | Serialization       | Compatibility Changes |
|----------|---------------------|-----------|--------|------------|-------|---------------------|-----------------------|
| Modified | `public` `abstract` | Interface | `JSON` | [`Object`] | JDK 8 | ![Not serializable] | ![No changes]         |


#### Methods

| Status | Modifiers                 | Generics | Type           | Method                           | Annotations | Throws | Compatibility Changes |
|--------|---------------------------|----------|----------------|----------------------------------|-------------|--------|-----------------------|
| Added  | **`static`** **`public`** |          | **[`Object`]** | **`toJSON`**([`Object`], `long`) |             |        | ![Method new static added to interface] |

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


</details>


___

*Generated on: 2026-10-05 16:04:40.985+0800*.

[Method added to public class]: https://img.shields.io/badge/Method_added_to_public_class-yellow "Method added to public class"
[Method new static added to interface]: https://img.shields.io/badge/Method_new_static_added_to_interface-orange "Method new static added to interface"
[No changes]: https://img.shields.io/badge/No_changes-green "No changes"
[Not serializable]: https://img.shields.io/badge/Not_serializable-green "Not serializable"
[`Map<String, Object>`]: # "java.util.Map<java.lang.String, java.lang.Object>"
[`Object`]: # "java.lang.Object"
[com.alibaba.fastjson2.JSON]: #user-content-com.alibaba.fastjson2.json
[com.alibaba.fastjson2.JSONReader]: #user-content-com.alibaba.fastjson2.jsonreader

