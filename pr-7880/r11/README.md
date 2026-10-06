# alibaba/fastjson2#7880 — round-11 re-verification artifacts

Verified commit: PR head `cb60e5eaa`. One commit since round 10 (`7f8ba05da`, see `../r10`): `cb60e5eaa` adds the test `ErrorOnDuplicateKeysTest.singleObjectListFieldForwardsTheStrictBitWithReflect` and changes no main source.

| path | content |
|---|---|
| `r11-01-status.png` | screenshot embedded in the round-11 PR comment |
| `results/r14-gitlog.txt`, `r14-diffstat.txt`, `r14-commit.diff` | the new commit |
| `results/r14-jar-identity.txt` | the core jars of `7f8ba05da` and `cb60e5eaa`, unzipped and compared: 777 of 777 entries identical |
| `results/fulltest-v14-summary.txt`, `coretest-v14-reflect-summary.txt`, `validate-v14-summary.txt` | full reactor, core suite with `-Dfastjson2.creator=reflect`, checkstyle |
| `results/mutation-cb60e5eaa-*` | h12 (and h13, h03, h06, h09 for reference) reverted one at a time, ten round-9 test classes (129 tests), default and reflect creators; driver, summary logs, per-mutation Maven logs. The hunk patches and `mut13.py` are in `../r9/results` and `../r10/results`. |
| `results/r14-desc-api.txt` | the "New public API" lines of the PR description, English and Chinese |

Because the core jar is identical to round 10, the round-10 runtime results (`../r10`) apply to `cb60e5eaa` unchanged. All results here were produced on 2026-10-07.
