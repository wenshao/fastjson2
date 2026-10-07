# alibaba/fastjson2#7880 — round-13 re-verification artifacts

Verified commit: PR head `0f9cd902f`. One commit since round 12 (`302223125`, see `../r12`): `0f9cd902f` applies `../r12/fix-r12.diff` and rewords the `TREE_FEATURES` javadoc in `ObjectWriterAdapter.java`.

| path | content |
|---|---|
| `r13-01-status.png` | screenshot embedded in the round-13 PR comment |
| `results/r16-gitlog.txt`, `r16-diffstat.txt`, `r16-commit.diff` | the new commit |
| `results/r16-filesha.txt` | SHA-1 of each changed file vs `302223125` + `fix-r12.diff`: 8 of 9 identical |
| `results/r16-jar-identity.txt` | core jars of `302223125` + `fix-r12.diff` and `0f9cd902f`: 777 of 778 entries identical; `ObjectWriterAdapter.class` differs only in line numbers |
| `results/fulltest-v16-summary.txt`, `coretest-v16-reflect-summary.txt`, `validate-v16-summary.txt` | full reactor, core suite with `-Dfastjson2.creator=reflect`, checkstyle |
| `results/r16-run-v16-*.txt`, `r16-result-v16-*.json`, `repro-v16-*.txt` | harness per JDK and creator |
| `results/r16-dump*-v16-*.txt`, `r16-q*-v16*.txt`, `r16-Q1*w-v16.txt`, `r16-deadlock-v16.txt`, `r16-race6-v16.txt` | dumps, probes (round 6–12), deadlock and race |
| `results/r16-japicmp-*.md` | japicmp `302223125` → `0f9cd902f` and `main` → `0f9cd902f` |

Build tag `v16` = `0f9cd902f`. The harness and probe sources are in `../r8`–`../r12`. All results were produced on 2026-10-07.
