#!/bin/bash
# Round 10: single-change mutations of 575bd9fa2. "h<NN>-<File>" reverts one hunk of the commit
# (env/mut12/<name>.patch); "c:<name>" applies a custom mutation from mut12.py for hunks that cannot be reverted alone.
# TESTS=full runs the whole core suite instead of the ten-class set.
P=<work>; W=$P/wt-mut12
T='ErrorOnDuplicateKeysTest,SortFieldNamesAlphabeticallyTest,SortFieldNamesRegistryTest,ObjectWriterAdapterLinkTest,TreeModelTest,TreeConversionLongFeaturesTest,ObjectWriterProviderCreationLockTest,MapKeyContextTest,ObjectWriterProviderVariantRaceTest,MapFilteredSortTest'
for m in "$@"; do
  cd $W && git checkout -q HEAD -- core/src/main
  case $m in
    c:*) python3 $P/env/mut12.py $W ${m#c:} > /dev/null || { echo "apply failed $m"; continue; } ;;
    *) git apply -R $P/env/mut12/$m.patch || { echo "apply failed $m"; continue; } ;;
  esac
  rm -rf core/target/surefire-reports
  tag=${m#c:}; [ "$TESTS" = full ] && tag=$tag-full
  if [ "$TESTS" = full ]; then
    taskset -c ${CPUS:-0-15} mvn -B -q -pl core test -Djacoco.skip=true > $P/logs/mut12-$tag.log 2>&1
  else
    taskset -c ${CPUS:-0-15} mvn -B -q -pl core test -Dtest="$T" -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.skip=true > $P/logs/mut12-$tag.log 2>&1
  fi
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
for x in sorted(fails)[:12]: print('   FAIL', x)
PY
done
cd $W && git checkout -q HEAD -- core/src/main
