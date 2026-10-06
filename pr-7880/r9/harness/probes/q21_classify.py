#!/usr/bin/env python3
"""Classifies the Q21 sweep: v11 -> v12 changes per creator, and ASM/reflect agreement for field-level X."""
import sys, re, collections
def load(path):
    d = {}
    for line in open(path):
        p = line.rstrip('\n').split('\t')
        if len(p) < 7: continue
        cn, x, shape, item = p[:4]
        vals = dict(kv.split('=', 1) for kv in p[4:])
        d[(cn, x, shape, item)] = vals
    return d
a = load(sys.argv[1]); b = load(sys.argv[2])
mask = lambda s: re.sub(r'@[0-9a-f]{5,8}', '@H', s)
changed = collections.defaultdict(list)
for k in sorted(b):
    for col in ('field', 'context', 'base'):
        if mask(a[k][col]) != mask(b[k][col]):
            changed[(k[0], col)].append(k)
print("cells per build: %d (x3 columns)" % len(b))
for (cn, col), ks in sorted(changed.items()):
    feats = collections.Counter(k[1] for k in ks)
    shapes = collections.Counter(k[2] for k in ks)
    print("%s %-7s changed %4d  features=%s shapes=%s" % (cn, col, len(ks), dict(feats), dict(shapes)))
# ASM vs reflect for field-level X on the new build, compared with the old build
def split(d):
    agree = set(); 
    for k in d:
        if k[0] != 'asm': continue
        r = ('reflect',) + k[1:]
        if mask(d[k]['field']) == mask(d[r]['field']): agree.add(k[1:])
    return agree
ag_a = split(a); ag_b = split(b)
tot = sum(1 for k in b if k[0] == 'asm')
print("field-level ASM==reflect: old %d/%d new %d/%d" % (len(ag_a), tot, len(ag_b), tot))
newly_div = sorted(ag_a - ag_b); newly_agree = sorted(ag_b - ag_a)
print("newly divergent (agree before, differ now): %d" % len(newly_div))
for k in newly_div[:400]:
    print("  DIV", k, "| asm:", b[('asm',)+k]['field'][:150], "| reflect:", b[('reflect',)+k]['field'][:150])
print("newly agreeing: %d" % len(newly_agree))
for k in newly_agree[:400]:
    print("  AGREE", k, "|", b[('asm',)+k]['field'][:150])
