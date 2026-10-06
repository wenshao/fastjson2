#!/bin/bash
# Runs each mutation of mut9.py in wt-mut9 against the round-6 mutation test set (9 classes).
P=<work>; W=$P/wt-mut9
T='ErrorOnDuplicateKeysTest,SortFieldNamesAlphabeticallyTest,SortFieldNamesRegistryTest,ObjectWriterAdapterLinkTest,TreeModelTest,TreeConversionLongFeaturesTest,ObjectWriterProviderCreationLockTest,MapKeyContextTest,ObjectWriterProviderVariantRaceTest'
for m in "$@"; do
  cd $W && git checkout -q HEAD -- core/src/main && python3 $P/env/mut9.py $W $m > /dev/null || { echo "apply failed $m"; continue; }
  rm -rf core/target/surefire-reports core/target/classes/com/alibaba/fastjson2/writer/ObjectWriterProvider.class core/target/classes/com/alibaba/fastjson2/writer/ObjectWriterImplMap.class
  taskset -c 8-15 mvn -B -q -pl core test -Dtest="$T" -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.skip=true -Dcheckstyle.skip=true > $P/logs/mut9-$m.log 2>&1
  echo "=== mutation: $m (mvn rc=$?)"
  python3 - "$W/core/target/surefire-reports" <<'PY'
import sys, glob, re
fails=[]; total=0
for f in glob.glob(sys.argv[1]+'/TEST-*.xml'):
    s=open(f).read()
    for m in re.finditer(r'<testcase name="([^"]+)" classname="([^"]+)"[^>]*?(/>|>(.*?)</testcase>)', s, re.S):
        total+=1
        body=m.group(4) or ''
        if '<failure' in body or '<error' in body:
            fails.append(m.group(2).split('.')[-1]+'.'+m.group(1))
print('  tests=%d failed=%d' % (total, len(fails)))
for x in sorted(fails): print('   FAIL', x)
PY
done
cd $W && git checkout -q HEAD -- core/src/main
