#!/bin/bash
cd <work>/env
J8=<work>/jdks/jdk8/bin/java; J11=<work>/jdks/jdk11/bin/java; J17=<work>/jdks/jdk17/bin/java; J21=<jdk21>/bin/java
J25=$(command -v java); J26=/usr/lib/jvm/zulu-26-amd64/bin/java
CPB=classes3:lib/jackson-core-2.17.2.jar:lib/jackson-databind-2.17.2.jar:lib/jackson-annotations-2.17.2.jar
run() { tag=$1; jar=$2; java=$3; shift 3; taskset -c 12-15 $java -Dleak.dir=leak -Dout=r15-result-$tag.json "$@" -cp $CPB:lib/fastjson2-$jar.jar verify.Verify7880 > r15-run-$tag.txt 2>&1; echo "$tag: $(grep -a SUMMARY r15-run-$tag.txt)"; }
for t in jdk8:$J8 jdk11:$J11 jdk17:$J17 jdk21:$J21 jdk25:$J25 jdk26:$J26; do run v15-${t%%:*} v15 ${t#*:}; done
run v15-jdk8-reflect v15 $J8 -Dfastjson2.creator=reflect
run v15-jdk21-reflect v15 $J21 -Dfastjson2.creator=reflect
taskset -c 12-15 $J21 -Dfastjson2.writer.alphabetic=false -cp $CPB:lib/fastjson2-v15.jar verify.Verify7880 globalAlphabeticOff > r15-run-v15-jdk21-alphaoff.txt 2>&1; echo "alphaoff: $(grep -a SUMMARY r15-run-v15-jdk21-alphaoff.txt)"
for v in v15; do for j in 21 8; do J=$( [ $j = 8 ] && echo $J8 || echo $J21 ); taskset -c 12-15 $J -cp $CPB:lib/fastjson2-$v.jar verify.Repro7880 > repro-$v-jdk$j.txt 2>&1; echo "repro $v jdk$j OK=$(grep -c ' OK$' repro-$v-jdk$j.txt) MISMATCH=$(grep -c MISMATCH repro-$v-jdk$j.txt)"; done; done
