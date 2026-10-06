# alibaba/fastjson2#7880 — round-7 re-verification artifacts

Verified commit: PR head `6bf404c01` against `main` `37d1f9bff`. One commit landed since round 6 (`6bf404c01`). It is the round-6 suggested patch (`../r6/fix-r6.diff`) applied verbatim; `results/r9-identity.txt` has the file hashes and the jar comparison. Earlier rounds: `../r6` (`07faf7789`), `../r5` (`b874205b6`), `../r4` (`da5e63163`), `../r3` (`f382fe908`), `../r2` (`51643676c`), parent folder (round 1, `65f8f58f7`).

| path | content |
|---|---|
| `r7-01..05-*.png` | screenshots embedded in the round-7 PR comment |
| `harness/Verify7880.java`, `harness/Round8.java` | the harness entry point (now also runs section K) and section K: field-level sorted map keys (K1 reference roots, K2 caller's context, K3 sorted variant and default branch). All other harness files are unchanged from `../r6/harness` |
| `harness/probes/Q9.java` | field-level sorted map keys under `ReferenceDetection`, a masking `ValueFilter` and a date format |
| `harness/bench/MapKey7880.java`, `MapKeySorted7880.java` | JMH benchmarks for the map-key path (`mapKeyToString`) |
| `fix-r7.diff` | suggested patch on `6bf404c01` (`ObjectWriterImplMap` +2 lines, 3 tests in `MapKeyContextTest`) |
| `results/` | see below |

`results/` file prefixes (build tags: `v8` = `07faf7789`, `v9` = `6bf404c01`, `fix9` = `6bf404c01` + `fix-r7.diff`):

- `r9-identity.txt`, `r9-gitlog.txt`: `6bf404c01` vs the round-6 patch, and the commit list.
- `r9-run-*`, `r9-result-*`: harness runs per build and JDK (`-K` = section K alone on `07faf7789`).
- `r9-dump*`: the six feature-off dumps for `v9` and `fix9` (the `main` and `v8` copies are in `../r6/results`).
- `r9-japicmp-*`, `r9-deadlock.txt`, `r9-startup.txt`, `r9-race6.txt`: API comparison, the class-initialization deadlock probe, first-write timing (JVMs interleaved) and the unpaced filter race.
- `r9-q6*`, `r9-q7*`: the round-6 review-finding probes on `v9`. `r9-q9-*`: the `Q9` probe per build and creator.
- `repro-<build>-jdk<N>.txt`: repro output.
- `fulltest-*-summary.txt`: module and test totals of the full reactor runs.
- `mutation-6bf404c01-*`: the mutation script, its driver, the summary log and the per-mutation Maven logs.
- `jmh/`: `ab-r9` (main / `07faf7789` / `6bf404c01` / patched, 4 interleaved rounds) and `ab-r9s` (field-level sorted keys, PR builds only), with the driver log.

All results in this folder were produced on 2026-10-06, with the harness exactly as committed here.
