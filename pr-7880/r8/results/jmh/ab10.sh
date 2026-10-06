#!/bin/bash
# Round 8: interleaved A/B, 4 rounds each, CPUs 4-7.
E=<work>/env; J=<jdk21>/bin/java
sed -e 's#E=$S/env#E=<work>/env#' $E/ab.sh > $E/ab-bench.sh; chmod +x $E/ab-bench.sh
$E/ab-bench.sh $J r10w 4 'Bench7880.(write|featureWriteList100Sorted)' main v10 dbg10
$E/ab9.sh $J r10k 4 'MapKey' v10 fix10 dbg10
