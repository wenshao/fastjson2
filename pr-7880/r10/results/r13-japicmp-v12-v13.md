
# Compatibility Report

![semver MAJOR](https://img.shields.io/badge/semver-MAJOR-red?logo=semver "semver MAJOR")

## Summary

> [!CAUTION]
>
> Incompatible changes found while checking backward compatibility of version `fastjson2-v13` with the previous version `fastjson2-v12`.

<details markdown="1">
<summary>Expand to see options used.</summary>

- **Report only summary**: No
- **Report only changes**: Yes
- **Report only binary-incompatible changes**: No
- **Access modifier filter**: `PROTECTED`
- **Old archives**:
  - ![fastjson2-v12 unknown](https://img.shields.io/badge/fastjson2_v12-unknown-blue "fastjson2-v12 unknown")
- **New archives**:
  - ![fastjson2-v13 unknown](https://img.shields.io/badge/fastjson2_v13-unknown-blue "fastjson2-v13 unknown")
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
| Modified | [com.alibaba.fastjson2.writer.ObjectWriterProvider] | ![Not serializable] | ![Method less accessible] |

<details markdown="1">
<summary>Expand for details.</summary>

___

<a id="user-content-com.alibaba.fastjson2.writer.objectwriterprovider"></a>
### `com.alibaba.fastjson2.writer.ObjectWriterProvider`

- [ ] Binary-compatible
- [ ] Source-compatible
- [X] Serialization-compatible

| Status   | Modifiers | Type  | Name                   | Extends    | JDK   | Serialization       | Compatibility Changes |
|----------|-----------|-------|------------------------|------------|-------|---------------------|-----------------------|
| Modified | `public`  | Class | `ObjectWriterProvider` | [`Object`] | JDK 8 | ![Not serializable] | ![No changes]         |


#### Methods

| Status   | Modifiers    | Generics | Type             | Method                                                  | Annotations | Throws | Compatibility Changes |
|----------|--------------|----------|------------------|---------------------------------------------------------|-------------|--------|-----------------------|
| Modified | ~~`public`~~ |          | [`ObjectWriter`] | `getObjectWriterFromCache`([`Type`], [`Class`], `long`) |             |        | ![Method less accessible] |


</details>


___

*Generated on: 2026-10-07 03:43:06.070+0800*.

[Method less accessible]: https://img.shields.io/badge/Method_less_accessible-red "Method less accessible"
[No changes]: https://img.shields.io/badge/No_changes-green "No changes"
[Not serializable]: https://img.shields.io/badge/Not_serializable-green "Not serializable"
[`Class`]: # "java.lang.Class"
[`ObjectWriter`]: # "com.alibaba.fastjson2.writer.ObjectWriter"
[`Object`]: # "java.lang.Object"
[`Type`]: # "java.lang.reflect.Type"
[com.alibaba.fastjson2.writer.ObjectWriterProvider]: #user-content-com.alibaba.fastjson2.writer.objectwriterprovider

