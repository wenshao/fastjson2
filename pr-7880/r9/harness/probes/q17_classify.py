"""Classifies r11-q17-<old>.txt vs r11-q17-<new>.txt cell by cell (ASM output against the reflect output and the old build)."""
import collections
import json
import sys


def load(v):
    d = {}
    for line in open(f'r11-q17-{v}.txt'):
        k, rest = line.rstrip('\n').split(' || asm ', 1)
        a, r = rest.split(' || reflect ', 1)
        d[k] = (a, r)
    return d


def srt(o):
    if isinstance(o, dict):
        return {nk: srt(o[k]) for k, nk in sorted(((k, srtstr(k)) for k in o), key=lambda x: x[1])}
    if isinstance(o, list):
        return [srt(x) for x in o]
    if isinstance(o, str):
        return srtstr(o)
    return o


def srtstr(s):
    if s[:1] in '[{':
        try:
            return 'J:' + json.dumps(srt(json.loads(s)), sort_keys=True)
        except Exception:
            return s
    return s


def norm(s):
    """the document with every object's keys sorted, recursively, including JSON text inside map keys"""
    if s.startswith('EXC'):
        return s
    return json.dumps(srt(json.loads(s)), sort_keys=True)


old, new = (sys.argv[1:] + ['v10', 'v11'])[:2]
a, b = load(old), load(new)
c = collections.Counter()
for k in a:
    a0, r0 = a[k]
    a1, r1 = b[k]
    c['cells'] += 1
    if r0 != r1:
        c['reflect output changed'] += 1
    if a0 == a1:
        c['ASM unchanged, ' + ('equal to reflect' if a1 == r1 else 'differs from reflect')] += 1
        continue
    c['ASM changed'] += 1
    c['  of which key order only' if norm(a0) == norm(a1) else '  of which content changed'] += 1
    if a1 == r1:
        c['  of which now equal to reflect'] += 1
    elif a0 == r0:
        c['  of which REGRESSION (was equal to reflect)'] += 1
    else:
        c['  of which still differ from reflect in content (X does not reach the beans under ASM)' if norm(a1) != norm(r1) and norm(a0) != norm(r0) else '  other'] += 1
for k in ['cells', 'reflect output changed', 'ASM unchanged, equal to reflect', 'ASM unchanged, differs from reflect', 'ASM changed',
          '  of which key order only', '  of which content changed', '  of which now equal to reflect',
          '  of which still differ from reflect in content (X does not reach the beans under ASM)', '  of which REGRESSION (was equal to reflect)', '  other']:
    print(f"{c.get(k, 0):5}  {k}")
