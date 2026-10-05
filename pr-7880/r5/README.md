# alibaba/fastjson2#7880 — round-5 re-verification artifacts

Verified commit: PR head `b874205b6` against `main` `37d1f9bff`. Earlier rounds: `../r4` (`da5e63163`), `../r3` (`f382fe908`), `../r2` (`51643676c`), parent folder (round 1, `65f8f58f7`).

| path | content |
|---|---|
| `r5-01..05-*.png` | screenshots embedded in the round-5 PR comment |
| `harness/Verify7880.java` | verification matrix (sections A–E, unchanged checks from rounds 1–4) |
| `harness/Round3.java`, `harness/Round4.java` | sections F (round 3) and G (round 4); G3 now calls the package-private tree helper |
| `harness/Round5.java` | section H, new in round 5: H1 registered-writer variants over 20 field shapes, H2 a registered writer held by both cells, H3 field-level BeanToArray on bean arrays, H4 tree helper with global defaults, H5 API surface |
| `harness/Repro7880.java` | repro items 1–4 (round 1), 5–7 (round 2), 8–10 (round 3), 11–13 (round 4), 14–15 (round 5) |
| `harness/DefaultDump*.java` | feature-off dumps; `DefaultDump5` (list and array writing) is new in round 5 |
| `harness/bench/Bench7880.java` | JMH benchmark, now with `treeFromBasket100` and `treeFromMediaContent` |
| `fix-r5.diff` | suggested round-5 patch (applies on `b874205b6`), incl. 2 regression tests |
| `results/` | harness runs per JDK (`r5-run-v5-*` = PR head, `r5-run-fix5-*` = with patch, `r5-result-v4h-*` = section H on `da5e63163`), repro outputs, dumps (`r5-dump*`), japicmp reports, mutation-check logs, JMH JSON (`jmh/`, arms `main`, `v5` = PR head, `fix5` = patched) |

The leak bean and A/B driver are unchanged from round 1 (`../harness/leak`, `../harness/bench`).
