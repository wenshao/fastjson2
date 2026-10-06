#!/bin/bash
# Probes, dumps, deadlock/race and japicmp for the patched builds (fix12f = 575bd9fa2 + fix-r9.diff; fix12cf = + list delegation).
cd <work>/env
J="taskset -c 12-15 <jdk21>/bin/java"; CPB=classes3:lib/jackson-core-2.17.2.jar:lib/jackson-databind-2.17.2.jar:lib/jackson-annotations-2.17.2.jar
for b in fix12f fix12cf; do
  $J -cp probe10/classes:lib/fastjson2-$b.jar p19.Q19 > r12-q19-$b.txt 2>&1
  $J -cp probe10/classes:lib/fastjson2-$b.jar p19b.Q19b > r12-q19b-$b.txt 2>&1
  $J -cp probe10/classes:lib/fastjson2-$b.jar p20.Q20 > r12-q20-$b.txt 2>&1
  $J -cp probe10/classes:lib/fastjson2-$b.jar p21.Q21 > r12-q21-$b.txt 2>&1
  $J -cp probe10/classes:lib/fastjson2-$b.jar p22.Q22 > r12-q22-$b.txt 2>&1
  $J -cp probe10/classes:lib/fastjson2-$b.jar p23.Q23 > r12-q23-$b.txt 2>&1
  $J -Xss512k -cp probe10/classes:lib/fastjson2-$b.jar p24.Q24 > r12-q24-$b.txt 2>&1
  $J -cp probe10/classes:lib/fastjson2-$b.jar p24b.Q24b > r12-q24b-$b.txt 2>&1
  $J -cp probe10/classes:lib/fastjson2-$b.jar p25.Q25 > r12-q25-$b.txt 2>&1
  for q in 10 11 12 15 17 18; do $J -cp probe10/classes:lib/fastjson2-$b.jar p$q.Q$q > r12-q$q-$b.txt 2>&1; done
  $J -cp probe10/classes:lib/fastjson2-$b.jar p10w.Q10w > r12-Q10w-$b.txt 2>&1
  $J -cp probe10/classes:lib/fastjson2-$b.jar p11w.Q11w > r12-Q11w-$b.txt 2>&1
  for q in Q6 Q6b Q7 Q7e; do $J -cp probe6/classes:lib/fastjson2-$b.jar p.$q > r12-$(echo $q | tr Q q)-$b.txt 2>&1; done
  $J -cp probe9/classes:lib/fastjson2-$b.jar p9.Q9 > r12-q9-$b-asm.txt 2>&1
  $J -Dfastjson2.creator=reflect -cp probe9/classes:lib/fastjson2-$b.jar p9.Q9 > r12-q9-$b-reflect.txt 2>&1
  $J -cp $CPB:lib/fastjson2-$b.jar verify.DefaultDump > r12-dump-$b.txt 2>&1
  $J -Dfastjson2.creator=reflect -cp $CPB:lib/fastjson2-$b.jar verify.DefaultDump > r12-dump-$b-reflect.txt 2>&1
  for n in 3 4 5 6 7; do
    $J -cp $CPB:lib/fastjson2-$b.jar verify.DefaultDump$n > r12-dump$n-$b-asm.txt 2>&1
    $J -Dfastjson2.creator=reflect -cp $CPB:lib/fastjson2-$b.jar verify.DefaultDump$n > r12-dump$n-$b-reflect.txt 2>&1
  done
  java -jar lib/japicmp.jar -o lib/fastjson2-v12.jar -n lib/fastjson2-$b.jar --markdown --only-modified -a protected -i 'com.alibaba.fastjson2.*' > r12-japicmp-v12-$b.md 2>&1
  java -jar lib/japicmp.jar -o lib/fastjson2-main.jar -n lib/fastjson2-$b.jar --markdown --only-modified -a protected -i 'com.alibaba.fastjson2.*' > r12-japicmp-main-$b.md 2>&1
done
(echo "# probe6/p/Deadlock7.java, JDK 21, default provider, no features; HUNG = both threads still blocked after 10 s"
 for b in fix12f fix12cf; do for v in ctor enum; do for c in asm reflect; do $J -Dfastjson2.creator=$c -cp probe6/classes:lib/fastjson2-$b.jar p.Deadlock7 $v 2>&1 | grep -v "done:"; done; done; done) > r12-deadlock-fix.txt
for b in fix12f fix12cf; do for c in asm reflect; do $J -cp probe6/classes:lib/fastjson2-$b.jar p.Race6 3000 6 $([ $c = reflect ] && echo reflect) 2>&1 | tail -1 | sed "s/^/$b $c /"; done; done > r12-race6-fix.txt
echo "=== probes done $(date +%T)"
