# Resolves the Antenna preprocessor lines (//#if j2se ... //#endif) in Hecl's
# source for the desktop (J2SE) version, the way Hecl's own build does with
# Antenna: inactive lines are dropped, and a file whose //#condition is false
# is skipped. This is how hecl/src was made from Hecl's git repository:
#   python3 preprocess.py <hecl checkout> <output folder> core java
import sys, os, re
SYM = {'j2se': 1, 'javaversion': 1.8, 'debugcmds': 0}
def ev(expr):
    e = expr.replace('&&', ' and ').replace('||', ' or ')
    e = re.sub(r'!(?!=)', ' not ', e)
    e = re.sub(r'[A-Za-z_]\w*', lambda m: m.group(0) if m.group(0) in ('and','or','not') else repr(SYM.get(m.group(0), 0)), e)
    return bool(eval(e))
def proc(src, dst):
    out = []; stack = []  # (active_now, any_taken, parent_active)
    active = True
    for line in open(src, encoding='latin-1'):
        s = line.strip()
        m = re.match(r'//#\s*(\w+)\s*(.*)', s)
        if m:
            d, arg = m.group(1), m.group(2).strip()
            if d == 'condition':
                if not ev(arg): return False
                continue
            if d in ('if','ifdef','ifndef'):
                c = ev(arg) if d=='if' else ((arg in SYM and SYM[arg]) if d=='ifdef' else not (arg in SYM and SYM[arg]))
                stack.append([active and c, c, active]); active = active and c
            elif d in ('elif','elifdef'):
                top = stack[-1]; c = (not top[1]) and ev(arg); top[1] = top[1] or c; active = top[2] and c
            elif d == 'else':
                top = stack[-1]; active = top[2] and not top[1]; top[1] = True
            elif d == 'endif':
                top = stack.pop(); active = top[2]
            else:
                raise SystemExit('unknown directive %s in %s' % (d, src))
            continue
        if active: out.append(line)
    assert not stack, src
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    open(dst, 'w', encoding='latin-1').write(''.join(out)); return True
srcroot, dstroot = sys.argv[1], sys.argv[2]
for sub in sys.argv[3:]:
    base = os.path.join(srcroot, sub)
    for dp, _, fs in os.walk(base):
        for f in fs:
            if f.endswith('.java'):
                p = os.path.join(dp, f); rel = os.path.relpath(p, base)
                if not proc(p, os.path.join(dstroot, rel)): print('skipped (condition):', p)
