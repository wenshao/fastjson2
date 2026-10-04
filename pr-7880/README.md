# alibaba/fastjson2#7880 — local verification artifacts

Verified commit: PR head `65f8f58f7` against `main` `37d1f9bff`.

| path | content |
|---|---|
| `01..05-*.png` | screenshots embedded in the PR review comment |
| `harness/verify/Verify7880.java` | independent verification matrix (jackson-databind 2.17.2 as oracle) |
| `harness/verify/Repro7880.java` | minimal reproductions of the 4 `SortFieldNamesAlphabetically` issues |
| `harness/verify/DefaultDump.java` | default-feature dump used to prove byte-identical behavior vs `main` |
| `harness/leak/LeakBean.java` | bean compiled into a separate directory for the class-loader leak check (`-Dleak.dir`) |
| `harness/bench/Bench7880.java`, `ab.sh` | JMH benchmarks and the interleaved A/B driver |
| `fix-proposal.diff` | suggested `ObjectWriterProvider` fix (applies on `65f8f58f7`) |
| `results/` | raw outputs: harness runs per JDK, repro, default dumps, japicmp report, JMH JSON |
