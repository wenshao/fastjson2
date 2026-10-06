# Single-change mutations of the U sketch (61e081039 + fix-r8 + sort word through nested containers): revert one call site each.
import sys
root, name = sys.argv[1], sys.argv[2]
W = root + '/core/src/main/java/com/alibaba/fastjson2/writer/'
S = 'JSONWriter.Feature.SortFieldNamesAlphabetically.mask'
def rep(rel, old, new, count=1):
    p = W + rel
    s = open(p).read()
    assert s.count(old) == count, (rel, old, s.count(old))
    open(p, 'w').write(s.replace(old, new))
if name == 'none':
    pass
elif name == 'list-text':
    rep('ObjectWriterImplList.java', 'itemObjectWriter.write(jsonWriter, item, i, this.itemType, this.features | (features & %s));' % S,
        'itemObjectWriter.write(jsonWriter, item, i, this.itemType, this.features);')
elif name == 'list-jsonb':
    rep('ObjectWriterImplList.java', 'itemObjectWriter.writeJSONB(jsonWriter, item, i, this.itemType, this.features | (features & %s));' % S,
        'itemObjectWriter.writeJSONB(jsonWriter, item, i, this.itemType, this.features);')
elif name == 'optional':
    rep('ObjectWriterImplOptional.java', 'valueWriter.write(jsonWriter, value, fieldName, valueType, this.features | (features & %s));' % S,
        'valueWriter.write(jsonWriter, value, fieldName, valueType, this.features);')
elif name == 'atomic-text':
    rep('ObjectWriterImplAtomicReference.java', '        if (ref != null && (features & %s) != 0) {\n            Class<?> refClass = ref.getClass();\n            jsonWriter.getContext().getProvider().getObjectWriter(refClass, refClass, jsonWriter.getFeatures() | features)\n                    .write(' % S,
        '        if (false) {\n            Class<?> refClass = ref.getClass();\n            jsonWriter.getContext().getProvider().getObjectWriter(refClass, refClass, jsonWriter.getFeatures() | features)\n                    .write(')
elif name == 'atomic-jsonb':
    rep('ObjectWriterImplAtomicReference.java', '        if (ref != null && (features & %s) != 0) {\n            Class<?> refClass = ref.getClass();\n            jsonWriter.getContext().getProvider().getObjectWriter(refClass, refClass, jsonWriter.getFeatures() | features)\n                    .writeJSONB(' % S,
        '        if (false) {\n            Class<?> refClass = ref.getClass();\n            jsonWriter.getContext().getProvider().getObjectWriter(refClass, refClass, jsonWriter.getFeatures() | features)\n                    .writeJSONB(')
elif name == 'array-jsonb':
    rep('ObjectWriterArray.java', 'itemObjectWriter.writeJSONB(jsonWriter, item, i, this.itemType, features & %s);' % S,
        'itemObjectWriter.writeJSONB(jsonWriter, item, i, this.itemType, 0);')
else:
    raise SystemExit('unknown ' + name)
print('applied', name)
