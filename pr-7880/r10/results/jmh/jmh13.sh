#!/bin/bash
# Report round 10 JMH on the head 7f8ba05da (v13): the list loops of U, interleaved arms, CPUs 4-7, JDK 21.
# Starts only after the mutation batches finish, so nothing else runs on the machine.
E=<work>/env; L=<work>/logs
until grep -q MUTDONE $L/mut13-reflect.log 2>/dev/null; do sleep 20; done
echo "=== load before JMH: $(cat /proc/loadavg)"
CLS=jmhclasses11 $E/ab12.sh <jdk21>/bin/java r13u 8 'bench11.ListJsonb7880' v10 v10copy v12 v13
echo "=== jmh13 all done $(date +%T) load $(cat /proc/loadavg)"
