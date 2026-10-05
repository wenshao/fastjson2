# alibaba/fastjson2#7880 — round-3 re-verification artifacts

Verified commit: PR head `f382fe908` against `main` `37d1f9bff`. Round-2 artifacts (for `51643676c`) are in `../r2`, round-1 artifacts (for `65f8f58f7`) in the parent folder.

| path | content |
|---|---|
| `r3-01..05-*.png` | screenshots embedded in the round-3 PR comment |
| `harness/Verify7880.java` | verification matrix (sections A–E, unchanged checks from rounds 1–2) |
| `harness/Round3.java` | section F, new in round 3: F1 registered `ObjectWriters` adapters, F2 field-writer gates and the cold-provider `BeanToArray` path, F3 `readObject(long)`, F4 duplicate keys, F5 `deepCopy`, F6 tree conversion, F7 root writers, F8 loader release |
| `harness/Repro7880.java` | repro items 1–4 (round 1), 5–7 (round 2), 8–10 (round 3) |
| `harness/DefaultDump.java` | round-1/2 default-feature dump (byte-identical vs `main`) |
| `harness/DefaultDump3.java` | round-3 feature-off dump of the paths touched by `51643676c..f382fe908` |
| `fix-r3.diff` | suggested round-3 patch (applies on `f382fe908`), incl. 4 regression tests |
| `results/` | harness runs per JDK (`r3-run-v3-*` = PR head, `r3-run-fix3-*` = with patch), repro outputs, dumps (`r3-dump*`), japicmp reports, mutation-check logs, JMH JSON (`jmh/`, arms `main`, `v3` = PR head, `fix3` = patched) |

The leak bean, JMH benchmark and A/B driver are unchanged from round 1 (`../harness/leak`, `../harness/bench`).
