import sys, re
root, name = sys.argv[1], sys.argv[2]
W = root + '/core/src/main/java/com/alibaba/fastjson2/'
def rep(rel, old, new, count=1, regex=False):
    p = W + rel
    s = open(p).read()
    if regex:
        n = len(re.findall(old, s, flags=re.S))
        assert n == count, (rel, old, n)
        s = re.sub(old, new, s, flags=re.S)
    else:
        assert s.count(old) == count, (rel, old, s.count(old))
        s = s.replace(old, new)
    open(p, 'w').write(s)
if name == 'none':
    pass
elif name == 'nolock':
    rep('writer/ObjectWriterProvider.java', 'synchronized (createLock) {', '{')
elif name == 'readarray':
    rep('JSONReader.java', 'value = readArray(contextFeatures);', 'value = readArray();')
    rep('JSONReader.java', 'val = readArray(features);', 'val = readArray();', 2)
    rep('reader/ObjectReaderImplObject.java', 'jsonReader.readArray(features)', 'jsonReader.readArray()', 2)
elif name == 'dup-type':
    rep('reader/ObjectReaderImplObject.java', r'\n\s*if \(seenKeys != null\) \{\n\s*// register the discriminator[^\n]*\n\s*seenKeys\.add\("@type"\);\n\s*\}', '', 1, True)
    rep('reader/ObjectReaderImplMapTyped.java', r'\n\s*if \(seenKeys != null && !seenKeys\.add\(getTypeKey\(\)\)\) \{\n\s*throw duplicateKeyError\(jsonReader, name\);\n\s*\}', '', 3, True)
elif name == 'map-typed':
    rep('writer/FieldWriterObject.java', '                && !Map.class.isAssignableFrom(fieldClass)\n', '')
    rep('writer/FieldWriterObject.java', 'if (format == null && !Map.class.isAssignableFrom(fieldClass)) {', 'if (format == null) {')
elif name == 'mixin':
    rep('writer/ObjectWriterProvider.java', '        cacheFieldBased.remove(target);\n        cacheFieldNamesSorted.remove(target);\n        cacheFieldNamesSortedFieldBased.remove(target);\n', '        cacheFieldNamesSorted.remove(target);\n')
elif name == 'proxy':
    rep('writer/ObjectWriterProvider.java', 'if (objectWriter == null && !fieldNamesSorted) {', 'if (objectWriter == null) {')
elif name == 'tree-reresolve':
    rep('writer/ObjectWriterAdapter.java', r'boolean reResolve = objectFieldWriter\.initValueClass != null\n\s*\? !objectFieldWriter\.isTypeMatch\(fieldValueClass\)\n.*?isAssignableFrom\(fieldValueClass\);', 'boolean reResolve = objectFieldWriter.initValueClass != null && !objectFieldWriter.isTypeMatch(fieldValueClass);', 1, True)
elif name == 'unwrapped':
    rep('writer/ObjectWriterAdapter.java', 'fieldObjectWriter = JSONFactory.getObjectWriter(fieldClass, this.features | features);', 'fieldObjectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(fieldClass);')
elif name == 'contentas':
    rep('writer/FieldWriterList.java', 'ObjectWriter resolved = jsonWriter.getContext().getProvider()\n                    .getObjectWriter(this.contentAs, contentAs, resolvedFeatures);', 'ObjectWriter resolved = jsonWriter.getObjectWriter(this.contentAs, contentAs);')
elif name == 'canconvert':
    rep('JSONObject.java', r'\n\s*if \(value instanceof Number\) \{\n\s*// any other integral Number.*?\n\s*\}\n', '\n', 2, True)
elif name == 'keycontext':
    rep('writer/ObjectWriterImplMap.java', r'\} else if \(\(\(jsonWriter\.getFeatures\(\) \| this\.features \| features\)\n.*?jsonWriter\.writeName\(strKey = mapKeyToString\(key, jsonWriter, features\)\);\n\s*\}', '} else {\n                    jsonWriter.writeNameAny(key);\n                }', 1, True)
elif name == 'collection-merge':
    rep('writer/ObjectWriterImplCollection.java', 'itemObjectWriter = jsonWriter.getContext().getProvider()\n                        .getObjectWriter(itemClass, itemClass, jsonWriter.getFeatures(features) | this.features);', 'itemObjectWriter = jsonWriter.getObjectWriter(itemClass);', 2)
else:
    raise SystemExit('unknown ' + name)
print('applied', name)
