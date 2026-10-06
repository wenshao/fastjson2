# alibaba/fastjson2#7880 — round-8 re-verification artifacts

Verified commit: PR head `61e081039` against `main` `37d1f9bff`. One commit landed since round 7 (`61e081039`). It is the round-7 suggested patch (`../r7/fix-r7.diff`) applied verbatim; `results/r10-identity.txt` has the diff comparison, the file hashes and the jar comparison. Earlier rounds: `../r7` (`6bf404c01`), `../r6` (`07faf7789`), `../r5` (`b874205b6`), `../r4` (`da5e63163`), `../r3` (`f382fe908`), `../r2` (`51643676c`), parent folder (round 1, `65f8f58f7`).

| path | content |
|---|---|
| `r8-01..05-*.png` | screenshots embedded in the round-8 PR comment |
| `harness/Verify7880.java`, `harness/Round9.java` | the harness entry point (now also runs section L) and section L: beans nested in field-level sorted map keys (L1, both creators, with and without a context filter) and values (L2, info rows). All other harness files are unchanged from `../r7/harness` and `../r6/harness` |
| `harness/probes/Q10.java` | key sweep: 21 key shapes × 22 features × filter × date format × ASM/reflect (3,696 cells), field-level sort on the map field against the sort on the context; D3/D4 compare keys with values |
| `harness/probes/Q11.java` | value sweep: 30 field-level sorted value shapes × JSON/UTF-8/JSONB × ASM/reflect against the sort on the context |
| `harness/probes/Q12.java`, `Q13.java` | sorted and plain holders sharing nested writers (both orders), and the item-writer cache of a typed list writer across sort words; no leak on any build |
| `harness/probes/Q15.java` | the T example: a `List` key under a field-level sort per creator and filter |
| `fix-r8.diff` | suggested patch for T on `61e081039` (`ObjectWriterImplMap` 1 line, 1 test in `MapKeyContextTest`) |
| `fix-r8-u.diff` | optional patch for U, on top of `fix-r8.diff` (4 writers, 6 call sites, 1 test in `SortFieldNamesAlphabeticallyTest`) |
| `results/` | see below |

`results/` file prefixes (build tags: `v8` = `07faf7789`, `v9` = `6bf404c01`, `v10` = `61e081039`, `fix10` = `61e081039` + `fix-r8.diff`, `dbg10` = `61e081039` + `fix-r8.diff` + `fix-r8-u.diff`):

- `r10-identity.txt`, `r10-gitlog.txt`, `r10-commit.diff`: `61e081039` against the round-7 patch, and the commit list.
- `r10-run-*`, `r10-result-*`: harness runs per build and JDK (`-L` = section L alone).
- `r10-dump*`: the six feature-off dumps for `v10`, `fix10` and `dbg10` (the `main` copies are in `../r6/results`, the `6bf404c01` copies in `../r7/results`).
- `r10-japicmp-*`, `r10-deadlock.txt`, `r10-race6.txt`, `r10-startup.txt`: API comparison, the class-initialization deadlock probe, the unpaced filter race and first-write timing.
- `r10-q6*`, `r10-q7*`, `r10-q9-*`: the earlier review-finding probes on `v10`. `r10-q10-*`, `r10-q11-*`, `r10-q15-*`: the new probes per build.
- `repro-<build>-jdk<N>.txt`: repro output.
- `fulltest-*-summary.txt`: module and test totals of the full reactor runs.
- `mutation-61e081039-*`: the mutation script, its driver, the summary log and the per-mutation Maven logs (base `61e081039` + `fix-r8.diff`).
- `mutation-u-*`: the same for the 6 call sites of `fix-r8-u.diff`.
- `jmh/`: `ab-r10w` (round-6 write benchmarks: `main` / `61e081039` / `+ T + U`, 4 interleaved rounds) and `ab-r10k` (round-7 map-key benchmarks: `61e081039` / `+ T` / `+ T + U`), with the driver log.

All results in this folder were produced on 2026-10-06, with the harness exactly as committed here.
