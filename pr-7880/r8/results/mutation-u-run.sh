#!/bin/bash
# Runs each mutation of mutU.py in wt-dbg10 (61e081039 + fix-r8 + U sketch, committed locally) against the two sort test classes.
P=<work>; W=$P/wt-dbg10
T='SortFieldNamesAlphabeticallyTest,MapKeyContextTest'
for m in "$@"; do
  cd $W && git checkout -q HEAD -- core/src/main && python3 $P/env/mutU.py $W $m > /dev/null || { echo "apply failed $m"; continue; }
  rm -rf core/target/surefire-reports
  taskset -c 0-3 mvn -B -q -pl core test -Dtest="$T" -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.skip=true > $P/logs/mutU-$m.log 2>&1
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
            msg=re.search(r'message="([^"]{0,160})', body)
            fails.append(m.group(2).split('.')[-1]+'.'+m.group(1)+('  ['+msg.group(1)+']' if msg else ''))
print('  tests=%d failed=%d' % (total, len(fails)))
for x in sorted(fails): print('   FAIL', x)
PY
done
cd $W && git checkout -q HEAD -- core/src/main
