# alibaba/fastjson2#7880 — round-12 re-verification artifacts

Verified commit: PR head `302223125`. Three commits since round 11 (`cb60e5eaa`, see `../r11`): `fa7e339e3`, `01f410791` (main and tests) and `302223125` (tests only).

| path | content |
|---|---|
| `r12-01..05-*.png` | screenshots embedded in the round-12 PR comment |
| `fix-r12.diff` | candidate fix on top of `302223125` for the three regressions, plus the javadoc wording (5 main files, 3 test files) |
| `fix-r12-tests.diff` | the test part of `fix-r12.diff` alone (fails on `302223125`) |
| `harness/probes/Q27.java` | sorted map keys (A), scalar key spelling (B), `canConvertTo*` on other `Number` types (D), `required` (E), `SortMapEntriesByKeys` in tree conversion (F), type-level sort (G), default order (H), typed map-value memo (J), polymorphic sorted field (K) |
| `harness/probes/Q27g.java` | type-level sort: `BeanToArray` round trip and first-write dependence |
| `harness/probes/Q27h.java` | default property order per creator (runs on `main`) |
| `harness/probes/Q27f.java` | a field-level `FieldBased` on Map, bean and List fields, per creator (runs on `main`) |
| `harness/probes/Q27t.java` | `SortMapEntriesByKeys`: `toJSONString` vs `JSON.toJSON` for map-valued fields and maps inside a list |
| `results/` | see below |

The harness and the other probes are unchanged from `../r8`–`../r10`.

`results/` (build tags: `v14` = `cb60e5eaa`, `v15` = `302223125`, `fix15` = `302223125` + `fix-r12.diff`, `main` = `37d1f9bff`):

- `r15-gitlog.txt`, `r15-diffstat.txt`, `r15-main.diff` (the main-source diff `cb60e5eaa..302223125`).
- `r15-run-v15-*.txt`, `r15-result-v15-*.json`: harness per JDK and creator; `repro-v15-jdk*.txt`. `r15-run-fix15-*.txt`: the harness with the fix (JDK 21, 21 reflect, 8).
- `r15-dump*-v15-*.txt`: the default-behavior dumps (`DefaultDump`, 3, 4, 5, 6), identical to round 10.
- `r15-q*-v15.txt`, `r15-Q10w-v15.txt`, `r15-Q11w-v15.txt`: the round 6–11 probes; `r15-q27*-{main,v14,v15,fix15}.txt`: the new probes.
- `r15-deadlock-v15.txt`, `r15-race6-v15.txt`.
- `r15-japicmp-main-v15.md`, `r15-japicmp-v14-v15.md`.
- `fulltest-v15-summary.txt`, `coretest-v15-{reflect,lambda}-summary.txt`, `validate-v15-summary.txt`; `coretest-fix15{,-reflect}-summary.txt`; `red15-{asm,reflect}-summary.txt` (the new tests on `302223125`).
- `mutation-302223125-*`: driver, custom mutations, the hunk patches (`mut15/hNN-*.patch`, one hunk of `cb60e5eaa..302223125` each), summary logs and per-mutation Maven logs.

All results in this folder were produced on 2026-10-07, with the harness exactly as committed here and in the earlier rounds.
