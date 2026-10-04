#!/bin/bash
# Interleaved A/B: main and PR alternate, 1 fork per run, pinned to a fixed CPU set.
# usage: ab.sh <java> <tag> <rounds> <regex> [jar names...]
S=${WORK:-$(pwd)}   # directory containing env/ (jars, jmhclasses, jmh.cp)
E=$S/env
JAVA=$1; TAG=$2; ROUNDS=$3; RE=$4; shift 4
JARS=${@:-main pr7880}
JMH=$(cat $E/jmh.cp)
mkdir -p $E/jmh/ab-$TAG
for r in $(seq 1 $ROUNDS); do
  for v in $JARS; do
    echo "=== $TAG round $r $v $(date +%T)"
    taskset -c 4-7 $JAVA -cp "$E/jmhclasses:$JMH:$E/lib/fastjson2-$v.jar" org.openjdk.jmh.Main "$RE" \
      -f 1 -wi 4 -w 1s -i 5 -r 1s -jvmArgs "-Xms1g -Xmx1g" -rf json -rff $E/jmh/ab-$TAG/$v-r$r.json > $E/jmh/ab-$TAG/$v-r$r.log 2>&1 \
      || echo "!!! failed $v r$r"
  done
done
echo "=== $TAG done $(date +%T)"
