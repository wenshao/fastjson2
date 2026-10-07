# Round 12: custom single-change mutations of 302223125 for hunks that cannot be reverted alone.
import sys
root, name = sys.argv[1], sys.argv[2]
B = root + '/core/src/main/java/com/alibaba/fastjson2/'
def rep(rel, old, new, count=1):
    p = B + rel
    s = open(p).read()
    assert s.count(old) == count, (rel, old, s.count(old))
    open(p, 'w').write(s.replace(old, new))
if name == 'none':
    pass
elif name == 'canlong-custom-true':
    # the 302223125 rule for other Number types replaced by cb60e5eaa's truncation check (DoubleAdder branch kept)
    rep('JSONObject.java', '''                return Math.abs(number.longValue() - d) < 1 && d < 9.223372036854776E18;
            }
            return true;''', '''                return Math.abs(number.longValue() - d) < 1 && d < 9.223372036854776E18;
            }
            return Math.abs(number.longValue() - number.doubleValue()) < 1;''')
else:
    raise SystemExit('unknown mutation ' + name)
