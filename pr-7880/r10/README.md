# alibaba/fastjson2#7880 — round-10 re-verification artifacts

Verified commit: PR head `7f8ba05da` against `main` `37d1f9bff` (the merge base; `main` is now `579e7bca6`, which changes only CI files). Two commits landed since round 9 (`575bd9fa2`):
- `b2492207a`: `../r9/fix-r9.diff` (V, W, X, Y and 9 tests). It also makes `ObjectWriterProvider.getObjectWriterFromCache(Type, Class, long)` package-private and drops the `ObjectWriterException` clause of `isPlainAdapter`.
- `7f8ba05da`: `../r9/fix-r9-u.diff` (a field-level sort goes to a second list writer).

Earlier rounds: `../r9` (`575bd9fa2`), `../r8` (`61e081039`), `../r7` (`6bf404c01`), `../r6` (`07faf7789`), `../r5` (`b874205b6`), `../r4` (`da5e63163`), `../r3` (`f382fe908`), `../r2` (`51643676c`), parent folder (round 1, `65f8f58f7`).

| path | content |
|---|---|
| `r10-01..03-*.png` | screenshots embedded in the round-10 PR comment |
| `harness/probes/Q26.java` | a `List<B>` field given a single object instead of an array: field-level `ErrorOnUnknownProperties`, `SupportSmartMatch`, `TrimString`, `InitStringFieldAsEmpty`, ASM vs reflect (runs on `main`) |
| `harness/probes/Q26s.java` | the same shape with the PR's field-level `ErrorOnDuplicateKeys` on a `List<Map>` field |
| `results/` | see below |

The harness and the other probes are unchanged from `../r9/harness` and `../r8/harness`.

`results/` (build tag `v13` = `7f8ba05da`; `v12` = `575bd9fa2`; `v10` = `61e081039`; `fix12cf` = `575bd9fa2` + `fix-r9.diff` + `fix-r9-u.diff`, from round 9):

- `r13-filesha.txt`, `r13-ObjectWriterProvider-bytecode.diff`: the identity check against the round-9 patches (file SHA-1s, jar entries, the bytecode difference).
- `r13-run-v13-*.txt`, `r13-result-v13-*.json`: harness runs per JDK and creator; `repro-v13-jdk*.txt`.
- `r13-dump*-v13-*.txt`: the default-behavior dumps (`DefaultDump`, 3, 4, 5, 6). `DefaultDump7` printed the same as in round 9 (`../r9/results/r12-dump7-v12-*`), so it is not repeated here.
- `r13-q*-v13.txt`, `r13-Q10w-v13.txt`, `r13-Q11w-v13.txt`: probe outputs; `r13-q21-classify-v11-v13.txt`; `r13-q26-{main,v12,v13}.txt`, `r13-q26s-{v11,v12,v13}.txt`.
- `r13-deadlock-v13.txt`, `r13-race6-v13.txt`.
- `r13-japicmp-main-v13.md`, `r13-japicmp-v12-v13.md`.
- `fulltest-v13-summary.txt`, `coretest-v13-{reflect,lambda}-summary.txt`, `validate-v13-summary.txt`, `main-lambda-asmrefactor-summary.txt`.
- `mutation-7f8ba05da-*`: driver, mutation script, summary logs and per-mutation Maven logs. `hNN-*.patch` reverts one hunk of `575bd9fa2` (`../r9/results/mutation-575bd9fa2-patches`).
- `jmh/ab-r13u`: JMH JSON and logs of the list loops (`61e081039`, A/A copy, `575bd9fa2`, `7f8ba05da`), with the drivers. The benchmark source is `../r9/harness/bench/ListJsonb7880.java`.

All results in this folder were produced on 2026-10-07, with the harness exactly as committed here and in the earlier rounds.
