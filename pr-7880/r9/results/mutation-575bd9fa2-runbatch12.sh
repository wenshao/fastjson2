#!/bin/bash
# Round 10 mutation batches, run after JMH. A: survivors of the ten-class run on 575bd9fa2 (full core suite, then reflect).
# B: 575bd9fa2 + fix-r9.diff with the patch's tests, ASM then reflect.
E=<work>/env; L=<work>/logs
(
  CREATOR=reflect CPUS=0-5 $E/runmut12b.sh h05-FieldReaderList > $L/mut12-reflect.log 2>&1
  CREATOR=reflect CPUS=0-5 $E/runmut12b.sh h04-FieldReaderAnySetter h06-FieldReaderList h07-FieldReaderList h08-FieldReaderMapReadOnly h09-ObjectReaderCreatorASM h12-ObjectReaderImplList h13-ObjectReaderImplList h14-ObjectReaderImplMapTyped h15-ObjectReaderImplObject h19-ObjectReaderImplOptional c:readany-word >> $L/mut12-reflect.log 2>&1
  TESTS=full CPUS=0-5 $E/runmut12b.sh h03-JSONReaderJSONB h04-FieldReaderAnySetter h05-FieldReaderList h06-FieldReaderList h07-FieldReaderList h08-FieldReaderMapReadOnly h09-ObjectReaderCreatorASM h12-ObjectReaderImplList h13-ObjectReaderImplList h14-ObjectReaderImplMapTyped h15-ObjectReaderImplObject h19-ObjectReaderImplOptional h22-ObjectWriterImplMap h24-ObjectWriterProvider h27-ObjectWriterProvider c:readany-word c:fwo-cache c:asmw-bail c:asmw-cache c:fromcache-nosort > $L/mut12-full.log 2>&1
  echo "=== batch A done $(date +%T)" >> $L/mut12-full.log
) &
(
  CPUS=6-11 $E/runmut12f.sh h03-JSONReaderJSONB h04-FieldReaderAnySetter h05-FieldReaderList h06-FieldReaderList h07-FieldReaderList h08-FieldReaderMapReadOnly h11-ObjectReaderImplGenericArray h12-ObjectReaderImplList h13-ObjectReaderImplList h14-ObjectReaderImplMapTyped h15-ObjectReaderImplObject h19-ObjectReaderImplOptional h22-ObjectWriterImplMap h24-ObjectWriterProvider h27-ObjectWriterProvider c:asmr-site c:asmr-site-mapping c:readany-word c:asmw-bail c:V-guard c:W-unmask c:X-arraykey c:Y-fraction > $L/mut12f-asm.log 2>&1
  CREATOR=reflect CPUS=6-11 $E/runmut12f.sh h04-FieldReaderAnySetter h05-FieldReaderList h06-FieldReaderList h07-FieldReaderList h08-FieldReaderMapReadOnly h11-ObjectReaderImplGenericArray h12-ObjectReaderImplList h13-ObjectReaderImplList h14-ObjectReaderImplMapTyped h15-ObjectReaderImplObject h19-ObjectReaderImplOptional c:readany-word > $L/mut12f-reflect.log 2>&1
  echo "=== batch B done $(date +%T)" >> $L/mut12f-reflect.log
) &
wait
echo "=== batches done $(date +%T)"
