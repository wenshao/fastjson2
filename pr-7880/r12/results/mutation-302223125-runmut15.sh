#!/bin/bash
# Round 12: single-change mutations of 302223125. "h<NN>-<File>" reverts one hunk of cb60e5eaa..302223125 (core/src/main,
# env/mut15/<name>.patch); "c:<name>" applies a custom mutation from mut15.py. W (worktree), CREATOR, CPUS from the environment.
P=<work>
T='ErrorOnDuplicateKeysTest,SortFieldNamesAlphabeticallyTest,SortFieldNamesRegistryTest,ObjectWriterAdapterLinkTest,TreeModelTest,TreeConversionLongFeaturesTest,ObjectWriterProviderCreationLockTest,MapKeyContextTest,ObjectWriterProviderVariantRaceTest,MapFilteredSortTest,AnySetterTest'
for m in "$@"; do
  cd $W && git checkout -q HEAD -- core/src/main
  case $m in
    c:*) python3 $P/env/mut15.py $W ${m#c:} > /dev/null || { echo "apply failed $m"; continue; } ;;
    *) git apply -R $P/env/mut15/$m.patch || { echo "apply failed $m"; continue; } ;;
  esac
  rm -rf core/target/surefire-reports
  tag=${m#c:}-${CREATOR:-asm}
  JAVA_TOOL_OPTIONS=${CREATOR:+-Dfastjson2.creator=$CREATOR} taskset -c ${CPUS:-0-15} mvn -B -q -pl core test -Dtest="$T" -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.skip=true > $P/logs/mut15-$tag.log 2>&1
  rc=$?
  echo "=== mutation: $m ${CREATOR:+[creator=$CREATOR]} (mvn rc=$rc)"
  grep -q "COMPILATION ERROR" $P/logs/mut15-$tag.log && echo "  COMPILATION ERROR"
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
for x in sorted(fails)[:12]: print('   FAIL', x)
PY
done
cd $W && git checkout -q HEAD -- core/src/main
