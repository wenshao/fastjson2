# alibaba/fastjson2#7880 — round-9 re-verification artifacts

Verified commit: PR head `575bd9fa2` against `main` `37d1f9bff`. Two commits landed since round 8 (`61e081039`):
- `833c666ee`: the round-8 patches `../r8/fix-r8.diff` (T) and `../r8/fix-r8-u.diff` (U), applied verbatim. `results/r11-identity.txt` has the file hashes and the jar comparison.
- `575bd9fa2`: the author's response to a separate review package (reader and writer side).

Earlier rounds: `../r8` (`61e081039`), `../r7` (`6bf404c01`), `../r6` (`07faf7789`), `../r5` (`b874205b6`), `../r4` (`da5e63163`), `../r3` (`f382fe908`), `../r2` (`51643676c`), parent folder (round 1, `65f8f58f7`).

| path | content |
|---|---|
| `r9-01..05-*.png` | screenshots embedded in the round-9 PR comment |
| `fix-r9.diff` | suggested patch on `575bd9fa2`: V, W, X, Y and 9 tests |
| `fix-r9-u.diff` | optional follow-up for U: the list item loops back to `61e081039`, a field-level sort delegated to a second list writer |
| `harness/probes/Q19.java`, `Q19b.java`, `Q19c.java` | V: writers registered for the runtime class of a field value (maps; lists, sets, beans, `JSONObject` subclasses), and a typed `Map` field after the runtime `Map` class is cached (`WriteClassName`) |
| `harness/probes/gen_q20.py` → `Q20.java` | field-level `ErrorOnDuplicateKeys` against the context-level one: 30 shapes × String/UTF-8 × ASM/reflect |
| `harness/probes/gen_q21.py` → `Q21.java`, `q21_classify.py` | W: every `JSONReader.Feature` at field level on `List<B>`, `B[]`, `Set<B>`, `Map<String,B>`, `B` × 24 item inputs × ASM/reflect (8,400 cells); the classifier compares two builds |
| `harness/probes/Q22.java` | object and array map keys × 6 APIs × 4 feature sets |
| `harness/probes/Q23.java` | `JSONObject.canConvertToLong` / `getLongValue` for `Number` types |
| `harness/probes/Q24.java`, `Q24b.java` | writer side of `575bd9fa2`: filtered map values, registered ASM-generated writers, bean keys and `$ref`, the direct-JIT list bail, module-provided writers |
| `harness/probes/Q25.java`, `Q25c.java`, `Q25f.java` | field-level `ErrorOnDuplicateKeys` on an any-setter and read-only maps, a creator parameter (compile with `-g`), a list field given one object, and a positional bean |
| `harness/probes/Q10w.java`, `Q11w.java` | `../r8/harness/probes/Q10.java` and `Q11.java` with the natural writers warm before each field-level write |
| `harness/probes/gen_q17.py` → `Q17.java`, `gen_q18.py` → `Q18.java`, `q17_classify.py` | field-level sort combined with 14 other field-level features (Q17), and those features alone (Q18) |
| `harness/bench/Read7880.java`, `ListJsonb7880.java` | JMH: the read paths and filtered map writes `575bd9fa2` changed; the list item loops `833c666ee` changed |
| `results/` | see below |

The harness itself and the probes not listed here are unchanged from `../r8/harness`.

`results/` file prefixes (build tags: `v10` = `61e081039`, `v11` = `833c666ee`, `v12` = `575bd9fa2`, `fix12f` = `575bd9fa2` + `fix-r9.diff`, `fix12cf` = `fix12f` + `fix-r9-u.diff`):

- `r11-*`: `833c666ee`. Identity check, harness runs per JDK, dumps, japicmp, deadlock and race probes, review-finding probes, and the Q17 classification (`r11-q17-classify.txt`).
- `r12-*`: `575bd9fa2`. The same set, plus Q19–Q25, `Q10w`/`Q11w`, and the Q21 raw outputs (gzip).
- `repro-<build>-jdk<N>.txt`: repro output.
- `fulltest-*-summary.txt`: module and test totals of the full reactor runs.
- `mutation-833c666ee-*`: the round-9 mutation check of `833c666ee`: driver, mutation scripts, summary log, per-mutation Maven logs.
- `mutation-575bd9fa2-*`: the same for `575bd9fa2`. `hNN-*.patch` reverts one hunk of the commit; `mut12.py` and `mut12f.py` hold the custom mutations.
- `jmh/`: `ab-r11l`, `ab-r11h`, `ab-r11c` (round-9 runs of the list loops; `ab-r11c` stopped after 7 rounds), `ab-r12r` (read paths: `833c666ee`, A/A copy, `575bd9fa2`, patch) and `ab-r12u` (list loops: `61e081039`, A/A copy, `575bd9fa2`, `575bd9fa2` + `fix-r9.diff` + `fix-r9-u.diff`), with the drivers.

JMH arm names: in `ab-r12r`, `fix12` is `575bd9fa2` + `fix-r9.diff` without X (`JSONReader.readObject(long)`, not on a benchmarked path); in `ab-r12u`, `fix12c` has the same main classes as `fix12cf`. The patch's tests changed after the JMH runs; its main source did not.

All results in this folder were produced on 2026-10-06 and 2026-10-07, with the harness exactly as committed here.
