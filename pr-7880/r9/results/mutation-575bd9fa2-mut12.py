# Custom single-change mutations of 575bd9fa2 (for hunks that cannot be reverted alone).
import sys, re
root, name = sys.argv[1], sys.argv[2]
B = root + '/core/src/main/java/com/alibaba/fastjson2/'
def rep(rel, old, new):
    p = B + rel
    s = open(p).read()
    assert s.count(old) == 1, (rel, old, s.count(old))
    open(p, 'w').write(s.replace(old, new))
if name == 'readany-word':
    rep('JSONReader.java', '''    public Object readAny(long features) {
        if ((features & Feature.ErrorOnDuplicateKeys.mask) != 0) {''', '''    public Object readAny(long features) {
        if (false) {''')
elif name == 'fwo-cache':
    rep('writer/FieldWriterObject.java', 'getObjectWriterFromCache(valueClass, valueClass, features | jsonWriter.getFeatures());',
        'getObjectWriterFromCache(valueClass, valueClass, ((features | jsonWriter.getFeatures()) & JSONWriter.Feature.FieldBased.mask) != 0);')
elif name == 'fwo-mapnull':
    rep('writer/FieldWriterObject.java', 'if (formattedWriter instanceof ObjectWriterImplMap && Map.class.isAssignableFrom(fieldClass)) {',
        'if (false && formattedWriter instanceof ObjectWriterImplMap && Map.class.isAssignableFrom(fieldClass)) {')
elif name == 'asmw-bail':
    rep('writer/ObjectWriterCreatorASM.java', '''&& (features & (JSONWriter.Feature.BeanToArray.mask
                        | JSONWriter.Feature.SortFieldNamesAlphabetically.mask)) == 0) {''', '''&& (features & JSONWriter.Feature.BeanToArray.mask) == 0) {''')
elif name == 'asmw-cache':
    rep('writer/ObjectWriterCreatorASM.java', 'provider.getObjectWriterFromCache(itemType, itemClass, features);',
        'provider.getObjectWriterFromCache(itemType, itemClass, FieldBased.isEnabled(features));')
elif name == 'fromcache-nosort':
    rep('writer/ObjectWriterProvider.java', '''        return cacheOf(fieldBased, fieldNamesSorted).get(objectType);
    }

    /**
     * Gets an ObjectWriter for the specified type, class, and format with field-based option.''', '''        return cacheOf(fieldBased, false).get(objectType);
    }

    /**
     * Gets an ObjectWriter for the specified type, class, and format with field-based option.''')
else:
    raise SystemExit('unknown ' + name)
print('applied', name)
