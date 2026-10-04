# alibaba/fastjson2#7880 — round-2 re-verification artifacts

Verified commit: PR head `51643676c` against `main` `37d1f9bff`. Round-1 artifacts (for `65f8f58f7`) are in the parent folder.

| path | content |
|---|---|
| `r2-01..05-*.png` | screenshots embedded in the round-2 PR comment |
| `harness/Verify7880.java` | verification matrix; round 2 adds field-level `BeanToArray` checks (B3), a type-matched `canConvertTo*` differential vs jackson and an extreme-exponent cost check (C) |
| `harness/Repro7880.java` | repro items 1–4 (round 1, now fixed) and 5–7 (round 2) |
| `harness/DefaultDump.java` | default-feature dump used to prove byte-identical behavior vs `main` |
| `fix-r2.diff` | suggested round-2 patch (applies on `51643676c`), incl. regression tests |
| `results/` | harness runs per JDK (`r2-run-v2-*` = PR head, `r2-run-fix2-*` = with patch), repro outputs, default dumps, japicmp report, JMH JSON |

The leak bean, JMH benchmark and A/B driver are unchanged from round 1 (`../harness/leak`, `../harness/bench`).
