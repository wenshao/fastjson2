# Single-change mutations of 6bf404c01 (+ the setRootObject line of the round-7 patch), applied to a worktree.
import sys
root, name = sys.argv[1], sys.argv[2]
W = root + '/core/src/main/java/com/alibaba/fastjson2/writer/'
def rep(rel, old, new, count=1):
    p = W + rel
    s = open(p).read()
    assert s.count(old) == count, (rel, old, s.count(old))
    open(p, 'w').write(s.replace(old, new))
P, M = 'ObjectWriterProvider.java', 'ObjectWriterImplMap.java'
if name == 'none':
    pass
elif name == 'creator-in-lock':      # Q: run the creator under createLock again (as in 82514ab10 / 07faf7789)
    rep(P, """            ObjectWriterCreator creator = getCreator();
            objectWriter = creator.createObjectWriter(
                    objectClass,
                    (fieldBased ? JSONWriter.Feature.FieldBased.mask : 0)
                            | (fieldNamesSorted ? JSONWriter.Feature.SortFieldNamesAlphabetically.mask : 0),
                    this
            );
            synchronized (createLock) {
""", """            ObjectWriterCreator creator = getCreator();
            synchronized (createLock) {
                objectWriter = creator.createObjectWriter(
                        objectClass,
                        (fieldBased ? JSONWriter.Feature.FieldBased.mask : 0)
                                | (fieldNamesSorted ? JSONWriter.Feature.SortFieldNamesAlphabetically.mask : 0),
                        this
                );
""")
elif name == 'nolock':               # P: link and publication no longer one step
    rep(P, 'synchronized (createLock) {', '{')
elif name == 'link-after-publish':   # 14:27: publish first, link under the lock afterwards
    rep(P, '                linkSortedVariant(objectType, fieldBased, fieldNamesSorted, objectWriter);\n'
           '                ObjectWriter previous = cacheOf(fieldBased, fieldNamesSorted).putIfAbsent(objectType, objectWriter);\n'
           '                if (previous != null) {\n                    objectWriter = previous;\n                }\n',
           '                ObjectWriter previous = cacheOf(fieldBased, fieldNamesSorted).putIfAbsent(objectType, objectWriter);\n'
           '                if (previous != null) {\n                    objectWriter = previous;\n                }\n'
           '                linkSortedVariant(objectType, fieldBased, fieldNamesSorted, objectWriter);\n')
elif name == 'no-link':              # sanity: created variants are never linked
    rep(P, '                linkSortedVariant(objectType, fieldBased, fieldNamesSorted, objectWriter);\n', '')
elif name == 'key-new-context':      # R: the c47a3cf51 key context on the default branch
    rep(M, '            str = JSON.toJSONString(key, context);\n',
           '            JSONWriter.Context keyContext = new JSONWriter.Context(context.provider);\n'
           '            keyContext.setFeatures(jsonWriter.getFeatures() | this.features | features);\n'
           '            str = JSON.toJSONString(key, keyContext);\n')
elif name == 'sorted-key-new-context':  # the field-level sort branch writes keys without the caller's context
    rep(M, 'try (JSONWriter keyWriter = JSONWriter.of(context)) {',
           'JSONWriter.Context keyContext = new JSONWriter.Context(context.provider);\n'
           '            keyContext.setFeatures(contextFeatures);\n'
           '            try (JSONWriter keyWriter = JSONWriter.of(keyContext)) {')
elif name == 'no-sorted-key-branch':    # field-level sort no longer selects the key's sorted variant
    rep(M, 'if (((this.features | features) & ~contextFeatures & JSONWriter.Feature.SortFieldNamesAlphabetically.mask) == 0) {',
           'if (true) {')
elif name == 'no-root':                 # 6bf404c01 as committed
    rep(M, '                keyWriter.setRootObject(key);\n', '')
else:
    raise SystemExit('unknown ' + name)
print('applied', name)
