# alibaba/fastjson2#7880 — round-4 re-verification artifacts

Verified commit: PR head `da5e63163` against `main` `37d1f9bff`. Round-3 artifacts (for `f382fe908`) are in `../r3`, round-2 artifacts (for `51643676c`) in `../r2`, round-1 artifacts (for `65f8f58f7`) in the parent folder.

| path | content |
|---|---|
| `r4-01..05-*.png` | screenshots embedded in the round-4 PR comment |
| `harness/Verify7880.java` | verification matrix (sections A–E, unchanged checks from rounds 1–3) |
| `harness/Round3.java` | section F (round 3) |
| `harness/Round4.java` | section G, new in round 4: G1 writer filters under the feature, G2 tree conversion with value-format features, G3 `JSON.toJSON(Object, long)` at the root, G4 API surface |
| `harness/Repro7880.java` | repro items 1–4 (round 1), 5–7 (round 2), 8–10 (round 3), 11–13 (round 4) |
| `harness/DefaultDump.java` | round-1/2 default-feature dump (byte-identical vs `main`) |
| `harness/DefaultDump3.java` | round-3 feature-off dump of the paths touched by `51643676c..f382fe908` |
| `harness/DefaultDump4.java` | round-4 dump: writer filters and tree conversion with existing features (API that exists on `main`) |
| `fix-r4.diff` | suggested round-4 patch (applies on `da5e63163`), incl. 2 regression tests |
| `results/` | harness runs per JDK (`r4-run-v4-*` = PR head, `r4-run-fix4-*` = with patch), repro outputs, dumps (`r4-dump*`), japicmp reports, mutation-check logs, JMH JSON (`jmh/`, arms `main`, `v4` = PR head, `fix4` = patched) |

The leak bean, JMH benchmark and A/B driver are unchanged from round 1 (`../harness/leak`, `../harness/bench`).
