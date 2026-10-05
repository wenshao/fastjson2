
# Compatibility Report

![semver MAJOR](https://img.shields.io/badge/semver-MAJOR-red?logo=semver "semver MAJOR")

## Summary

> [!CAUTION]
>
> Incompatible changes found while checking backward compatibility of version `fastjson2-v5` with the previous version `fastjson2-v4`.

<details markdown="1">
<summary>Expand to see options used.</summary>

- **Report only summary**: No
- **Report only changes**: Yes
- **Report only binary-incompatible changes**: No
- **Access modifier filter**: `PROTECTED`
- **Old archives**:
  - ![fastjson2-v4 unknown](https://img.shields.io/badge/fastjson2_v4-unknown-blue "fastjson2-v4 unknown")
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

| Status   | Type                         | Serialization       | Compatibility Changes |
|----------|------------------------------|---------------------|-----------------------|
| Modified | [com.alibaba.fastjson2.JSON] | ![Not serializable] | ![Method removed]     |

<details markdown="1">
<summary>Expand for details.</summary>

___

<a id="user-content-com.alibaba.fastjson2.json"></a>
### `com.alibaba.fastjson2.JSON`

- [ ] Binary-compatible
- [ ] Source-compatible
- [X] Serialization-compatible

| Status   | Modifiers           | Type      | Name   | Extends    | JDK   | Serialization       | Compatibility Changes |
|----------|---------------------|-----------|--------|------------|-------|---------------------|-----------------------|
| Modified | `public` `abstract` | Interface | `JSON` | [`Object`] | JDK 8 | ![Not serializable] | ![No changes]         |


#### Methods

| Status  | Modifiers                 | Generics | Type           | Method                           | Annotations | Throws | Compatibility Changes |
|---------|---------------------------|----------|----------------|----------------------------------|-------------|--------|-----------------------|
| Removed | ~~`static`~~ ~~`public`~~ |          | ~~[`Object`]~~ | ~~`toJSON`~~([`Object`], `long`) |             |        | ![Method removed]     |


</details>


___

*Generated on: 2026-10-05 22:32:00.286+0800*.

[Method removed]: https://img.shields.io/badge/Method_removed-red "Method removed"
[No changes]: https://img.shields.io/badge/No_changes-green "No changes"
[Not serializable]: https://img.shields.io/badge/Not_serializable-green "Not serializable"
[`Object`]: # "java.lang.Object"
[com.alibaba.fastjson2.JSON]: #user-content-com.alibaba.fastjson2.json

