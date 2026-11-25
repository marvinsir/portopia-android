#!/usr/bin/env python3
"""Validate scene graph and explore reachable state combinations."""
import json,sys,collections
from pathlib import Path
p=Path(sys.argv[1]) if len(sys.argv)>1 else Path(__file__).resolve().parents[1]/'app/src/main/assets/demo.json'
g=json.loads(p.read_text(encoding='utf-8')); langs={'ja','zh-Hans','zh-Hant','en'}
def text(o): assert langs<=o.keys() and all(isinstance(o[l],str) and o[l].strip() for l in langs),o
assert g['schema']==1; text(g['title']); scenes=g['scenes']; assert g['start'] in scenes
for sid,s in scenes.items():
    text(s['title']);text(s['text'])
    for a in s['actions']:
        text(a['label']); assert a.get('goto',sid) in scenes
        if 'reply' in a:text(a['reply'])
        for k in ('requires','forbids','set','clear'):
            assert isinstance(a.get(k,[]),list) and all(isinstance(x,str) for x in a.get(k,[])),(sid,k)
q=collections.deque([(g['start'],frozenset())]); seen=set(q); endings=[]; dead=[]; edges=0
while q:
    sid,f=q.popleft(); aa=scenes[sid]['actions']; active=[a for a in aa if set(a.get('requires',[]))<=f and not set(a.get('forbids',[]))&f]
    if not aa:endings.append(sid)
    elif not active:dead.append((sid,sorted(f)))
    for a in active:
        nxt=(a.get('goto',sid),frozenset((f|set(a.get('set',[])))-set(a.get('clear',[]))));edges+=1
        if nxt not in seen:seen.add(nxt);q.append(nxt)
    if len(seen)>100000:raise RuntimeError('State exploration limit exceeded')
print(f'{len(scenes)} scenes; {len(seen)} reachable states; {edges} transitions; endings={sorted(set(endings))}; deadlocks={dead}; unreachable={sorted(set(scenes)-{s for s,f in seen})}')
assert not dead and endings and set(scenes)=={s for s,f in seen}
