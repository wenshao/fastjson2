# alibaba/fastjson2#7880 — round-6 re-verification artifacts

Verified commit: PR head `07faf7789` against `main` `37d1f9bff`. Five commits landed since round 5 (`2dd085431`, `2e6afd5d4`, `c47a3cf51`, `82514ab10`, `07faf7789`); `2e6afd5d4` and `82514ab10` are covered too, so each regression can be placed in the commit that introduced it. Earlier rounds: `../r5` (`b874205b6`), `../r4` (`da5e63163`), `../r3` (`f382fe908`), `../r2` (`51643676c`), parent folder (round 1, `65f8f58f7`).

| path | content |
|---|---|
| `r6-01..06-*.png` | screenshots embedded in the round-6 PR comment |
| `harness/Verify7880.java`, `Round3.java` … `Round7.java` | verification matrix; section I (`Round6`: filter links) and section J (`Round7`: writer creation vs class initializers, map keys and the context) are new |
| `harness/HookedProvider7880.java` | test-only `ObjectWriterProvider` subclass (package `com.alibaba.fastjson2.writer`) that pauses one thread inside the cache lookups to replay a race in a fixed order |
| `harness/Repro7880.java` | repro items 1–18 (16 = P, 17 = Q, 18 = R) |
| `harness/DefaultDump*.java` | feature-off dumps; `DefaultDump6` (tree conversion × features) and `DefaultDump7` (containers, map keys, readers) are new |
| `harness/bench/Bench7880.java` | JMH benchmark; adds `writeBasketArr100*`, `writeSet100Beans`, `parseList100Objects`, `featureWriteBasketArr100Sorted` |
| `harness/probes/` | `Deadlock7` (Q), `Startup` + `gen_beans.py` (first-write timing), `Race6` (unpaced filter race), `Q6`/`Q6b` (15:34 review findings), `Q7*` (R and the other `c47a3cf51` output changes), `Size6`/`SizeAgent` (footprint), `codesize.py` |
| `fix-r6.diff` | suggested patch on `07faf7789` (2 main files, 3 test classes) |
| `results/` | see below |

`results/` file prefixes:

- `r6-*`: `2e6afd5d4` (`v6`), `b874205b6` (`v5`) and the superseded patch on `2e6afd5d4` (`fix6`). Harness runs, repro, dumps, japicmp, race probe, review-finding probes (`r6-q6*`), footprint and codesize.
- `r7-*`: `82514ab10` (`v7`) and the superseded patch on it (`fix7`). Harness runs, section J alone on `main` / `b874205b6` / `2e6afd5d4` (`*-J.*`), `DefaultDump7`, deadlock and startup probes, `Q7*` probes.
- `r8-*`: `07faf7789` (`v8`) and the suggested patch (`fix8`). Harness runs, all dumps (`main` copies included for diffing), japicmp, deadlock, startup, race and review probes, codesize of the touched methods, the GC-setting matrix for `ObjectWriterAdapterLinkTest`, and `git log`.
- `repro-<build>-jdk<N>.txt`: repro output per build and JDK.
- `mutation-07faf7789-*`: the mutation script, the summary log and the per-mutation Maven logs. `patch-tests-on-82514ab10.log`: the patch's tests run against unpatched `82514ab10`.
- `superseded-fix-on-*.diff`: the earlier versions of the patch for `2e6afd5d4` and `82514ab10`.
- `jmh/`: `ab-r7` (main / `2e6afd5d4` / `07faf7789`, 6 interleaved rounds) and `ab-r7feat` (opt-in sorted writes, `2e6afd5d4` vs `07faf7789`, 4 rounds), with driver logs.

All results in this folder were produced on 2026-10-06, with the harness exactly as committed here.

The leak bean and A/B driver are unchanged from round 1 (`../harness/leak`, `../harness/bench`).
