import subprocess, sys, re
def sizes(jar, cls):
    out = subprocess.run(['<jdk21>/bin/javap', '-c', '-p', '-cp', jar, cls], capture_output=True, text=True).stdout
    res = {}; cur = None; last = 0
    for line in out.splitlines():
        m = re.match(r'^  (?:[\w<>\[\], .$?]+ )?([\w$<>]+)\(([^)]*)\)', line)
        if m and not line.startswith('    '):
            if cur: res[cur] = last + 1
            cur = m.group(1) + '(' + m.group(2) + ')'; last = 0
            continue
        m2 = re.match(r'^\s+(\d+): (\w+)', line)
        if m2 and cur:
            off = int(m2.group(1)); op = m2.group(2)
            # approximate instruction length: use next offset; store last offset + its length later
            last = off + (0 if op in ('ireturn','areturn','return','lreturn','dreturn','freturn','athrow') else (2 if op in ('goto',) else 0))
    if cur: res[cur] = last + 1
    return res
targets = {
 'com.alibaba.fastjson2.writer.FieldWriterObjectArray': ['getItemWriter', 'resolveItemWriter', 'writeArray'],
 'com.alibaba.fastjson2.writer.ObjectWriterAdapter': ['toJSON', 'setValueFilter', 'linkSortedVariant', 'toJSONObject'],
}
for cls, ms in targets.items():
    for v in sys.argv[1:]:
        r = sizes('lib/fastjson2-%s.jar' % v, cls)
        print(v.ljust(5), cls.split('.')[-1], ', '.join('%s=%s' % (k, r[k]) for k in sorted(r) if k.split('(')[0] in ms))
