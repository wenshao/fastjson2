# Single-change mutations of 575bd9fa2 + fix-r9.diff: one revert per suggested change, and the generated-reader
# call sites of 575bd9fa2 in their fix-r9 form.
import sys
root, name = sys.argv[1], sys.argv[2]
B = root + '/core/src/main/java/com/alibaba/fastjson2/'
E = 'JSONReader.Feature.ErrorOnDuplicateKeys.mask'
def rep(rel, old, new, count=1):
    p = B + rel
    s = open(p).read()
    assert s.count(old) == count, (rel, old, s.count(old))
    open(p, 'w').write(s.replace(old, new))
if name == 'V-guard':
    rep('writer/FieldWriterObject.java', '''        if (format == null) {
            formattedWriter = jsonWriter.context.provider.getObjectWriterFromCache(''', '''        if (format == null && !Map.class.isAssignableFrom(fieldClass)) {
            formattedWriter = jsonWriter.context.provider.getObjectWriterFromCache(''')
elif name == 'W-unmask':
    rep('reader/ObjectReaderCreatorASM.java', 'mw.visitLdcInsn(fieldReader.features & %s);' % E, 'mw.visitLdcInsn(fieldReader.features);', 2)
elif name == 'asmr-site-mapping':
    rep('reader/ObjectReaderCreatorASM.java', '''                mw.visitLdcInsn(fieldReader.features & %s);
                mw.lload(FEATURES);
                mw.lor();''' % E, '''                mw.lload(FEATURES);''')
elif name == 'asmr-site':
    rep('reader/ObjectReaderCreatorASM.java', '''            mw.visitLdcInsn(fieldReader.fieldName);
            mw.visitLdcInsn(fieldReader.features & %s);
            mw.lload(FEATURES);
            mw.lor();''' % E, '''            mw.visitLdcInsn(fieldReader.fieldName);
            mw.lload(FEATURES);''')
elif name == 'X-arraykey':
    rep('JSONReader.java', '''                } else if (ch == '[') {
                    name = readArray(features);''', '''                } else if (ch == '[') {
                    name = readArray();''')
elif name == 'Y-fraction':
    rep('JSONObject.java', 'return Math.abs(number.longValue() - number.doubleValue()) < 1;', 'return number.longValue() == number.doubleValue();')
elif name == 'readany-word':
    rep('JSONReader.java', '''    public Object readAny(long features) {
        if ((features & Feature.ErrorOnDuplicateKeys.mask) != 0) {''', '''    public Object readAny(long features) {
        if (false) {''')
elif name == 'asmw-bail':
    rep('writer/ObjectWriterCreatorASM.java', '''&& (features & (JSONWriter.Feature.BeanToArray.mask
                        | JSONWriter.Feature.SortFieldNamesAlphabetically.mask)) == 0) {''', '''&& (features & JSONWriter.Feature.BeanToArray.mask) == 0) {''')
elif name == 'fwo-mapnull':
    rep('writer/FieldWriterObject.java', 'if (formattedWriter instanceof ObjectWriterImplMap && Map.class.isAssignableFrom(fieldClass)) {',
        'if (false && formattedWriter instanceof ObjectWriterImplMap && Map.class.isAssignableFrom(fieldClass)) {')
else:
    raise SystemExit('unknown ' + name)
print('applied', name)
