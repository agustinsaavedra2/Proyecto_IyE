#!/usr/bin/env python3
import json
from pathlib import Path
IN = Path('index_labeled.jsonl')
OUT = Path('index_labeled_normalized.jsonl')
NORMAL_MAP = {
    'alto':'alto','a':'alto','high':'alto',
    'medio':'medio','m':'medio','medium':'medio',
    'bajo':'bajo','low':'bajo','l':'bajo',
}
counts = {'alto':0,'medio':0,'bajo':0,'none':0}

with IN.open('r', encoding='utf-8') as fin, OUT.open('w', encoding='utf-8') as fout:
    for line in fin:
        line=line.strip()
        if not line: continue
        try:
            obj=json.loads(line)
        except Exception:
            continue
        lab=obj.get('label')
        norm=None
        if isinstance(lab, dict):
            raw=lab.get('riesgo')
            if raw is None:
                norm=None
            else:
                s=str(raw).strip().lower()
                # clean common placeholders
                if s in ['null','nulo','none','na','n/a','']:
                    norm=None
                else:
                    # remove non-alpha
                    s2=''.join(c for c in s if c.isalpha())
                    norm=NORMAL_MAP.get(s) or NORMAL_MAP.get(s2)
            obj['label']['riesgo']=norm
        else:
            norm=None
        if norm is None:
            counts['none']+=1
        else:
            counts[norm]+=1
        fout.write(json.dumps(obj, ensure_ascii=False)+'\n')

print('WROTE', OUT, 'SUMMARY:')
for k,v in counts.items():
    print(k, v)

