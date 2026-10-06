#!/bin/bash
# Round 10 JMH, interleaved arms, CPUs 4-7, JDK 21: the read paths 575bd9fa2 changed, then the U list loops.
E=<work>/env
CLS=jmhclasses12 $E/ab12.sh <jdk21>/bin/java r12r 8 'bench12.Read7880' v11 v11copy v12 fix12
CLS=jmhclasses11 $E/ab12.sh <jdk21>/bin/java r12u 8 'bench11.ListJsonb7880' v10 v10copy v12 fix12c
echo "=== jmh12 all done $(date +%T)"
