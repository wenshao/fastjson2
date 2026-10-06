#!/bin/bash
# Round 9: single-change mutations of 833c666ee itself. Map-key / provider mutations (mut10.py) run against the nine classes of
# the round-6 set; the U call-site mutations (mutU.py) against SortFieldNamesAlphabeticallyTest and MapKeyContextTest.
P=<work>; W=$P/wt-mut11
T='ErrorOnDuplicateKeysTest,SortFieldNamesAlphabeticallyTest,SortFieldNamesRegistryTest,ObjectWriterAdapterLinkTest,TreeModelTest,TreeConversionLongFeaturesTest,ObjectWriterProviderCreationLockTest,MapKeyContextTest,ObjectWriterProviderVariantRaceTest'
for spec in "$@"; do
  script=${spec%%:*}; m=${spec#*:}
  cd $W && git checkout -q HEAD -- core/src/main && python3 $P/env/$script.py $W $m > /dev/null || { echo "apply failed $spec"; continue; }
  rm -rf core/target/surefire-reports
  taskset -c 8-15 mvn -B -q -pl core test -Dtest="$T" -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.skip=true > $P/logs/mut11-$script-$m.log 2>&1
  echo "=== mutation: $script:$m (mvn rc=$?)"
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
