#!/usr/bin/env python3
"""Paired per-round ratios of interleaved JMH arms: usage jmhstat12.py <dir> <base> <arm...>. Skips empty/incomplete files."""
import json, sys, glob, os, re, statistics
d, base, arms = sys.argv[1], sys.argv[2], sys.argv[3:]
def load(arm):
    out = {}
    for f in glob.glob(os.path.join(d, arm + '-r*.json')):
        r = int(re.search(r'-r(\d+)\.json$', f).group(1))
        try:
            data = json.load(open(f))
        except Exception:
            continue
        if not data:
            continue
        out[r] = {x['benchmark'].split('.')[-1]: x['primaryMetric']['score'] for x in data}
    return out
B = load(base); A = {a: load(a) for a in arms}
benches = sorted({b for r in B.values() for b in r})
print('%-20s %12s ' % ('benchmark', base + ' ops/ms') + ' '.join('%28s' % a for a in arms))
res = {}
for bn in benches:
    cells = []
    for a in arms:
        ratios = [A[a][r][bn] / B[r][bn] - 1 for r in sorted(B) if r in A[a] and bn in A[a][r] and bn in B[r]]
        if not ratios:
            cells.append('%28s' % '-'); continue
        med = statistics.median(ratios) * 100
        slower = sum(1 for x in ratios if x < 0)
        res[(bn, a)] = (med, min(ratios) * 100, max(ratios) * 100, slower, len(ratios))
        cells.append('%+6.2f%% [%+5.1f,%+5.1f] %d/%d' % res[(bn, a)])
    base_med = statistics.median([B[r][bn] for r in B if bn in B[r]])
    print('%-20s %12.1f ' % (bn, base_med) + ' '.join('%28s' % c for c in cells))
print('rounds:', {a: len(A[a]) for a in arms}, base, len(B))
