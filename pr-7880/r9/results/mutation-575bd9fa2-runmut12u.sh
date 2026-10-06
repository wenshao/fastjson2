#!/bin/bash
# fix-r9-u.diff: remove the delegation at the top of ObjectWriterImplList.write / writeJSONB, one at a time.
P=<work>; W=$P/wt-fix12c; F=core/src/main/java/com/alibaba/fastjson2/writer/ObjectWriterImplList.java
for m in text jsonb; do
  cd $W && cp $F <work>/ImplList.orig
  python3 - $W/$F $m <<'PY'
import sys
p, m = sys.argv[1], sys.argv[2]
s = open(p).read()
call = 'sortedItemsWriter().write(jsonWriter, object, fieldName, fieldType, features);' if m == 'text' else 'sortedItemsWriter().writeJSONB(jsonWriter, object, fieldName, fieldType, features);'
old = '''        if ((features & ~this.features & JSONWriter.Feature.SortFieldNamesAlphabetically.mask) != 0) {
            %s''' % call
assert s.count(old) == 1
s = s.replace(old, '''        if (false) {
            %s''' % call)
open(p, 'w').write(s)
PY
  rm -rf core/target/surefire-reports
  taskset -c 12-15 mvn -B -q -pl core test -Dtest='SortFieldNamesAlphabeticallyTest,SortFieldNamesRegistryTest,MapKeyContextTest' -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.skip=true > $P/logs/mut12u-$m.log 2>&1
  echo "=== mutation: delegation-$m (mvn rc=$?)"
  python3 - "$W/core/target/surefire-reports" <<'PY'
import sys, glob, re
fails=[]; total=0
for f in glob.glob(sys.argv[1]+'/TEST-*.xml'):
    s=open(f).read()
    for m in re.finditer(r'<testcase name="([^"]+)" classname="([^"]+)"[^>]*?(/>|>(.*?)</testcase>)', s, re.S):
        total+=1
        if '<failure' in (m.group(4) or '') or '<error' in (m.group(4) or ''):
            fails.append(m.group(2).split('.')[-1]+'.'+m.group(1))
print('  tests=%d failed=%d' % (total, len(fails)))
for x in sorted(fails): print('   FAIL', x)
PY
  cp <work>/ImplList.orig $W/$F
done
