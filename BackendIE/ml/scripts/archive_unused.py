"""Script no-destructivo para mover archivos duplicados o no usados a ml/_cleanup_backup
No borra nada: mueve archivos a un backup y produce report en ml/cleanup_report.md
Uso: python ml/scripts/archive_unused.py --dry-run (solo reporte)
"""
import argparse
from pathlib import Path
import shutil

ROOT = Path(__file__).parent.parent
BACKUP = ROOT / '_cleanup_backup'

# Reglas: archivos a archivar (ejemplos)
CANDIDATES = [
    'ml/service/requirements.txt',
    'ml/manifest.json',
]


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--dry-run', action='store_true')
    args = p.parse_args()
    BACKUP.mkdir(parents=True, exist_ok=True)
    report = []
    for rel in CANDIDATES:
        src = ROOT / Path(rel).relative_to('ml') if str(rel).startswith('ml/') else ROOT / rel
        # normalize
        src = ROOT / Path(rel)
        if src.exists():
            dest = BACKUP / src.name
            report.append(f'Would move: {src} -> {dest}')
            if not args.dry_run:
                shutil.move(str(src), str(dest))
                report.append(f'Moved: {src} -> {dest}')
        else:
            report.append(f'Not found: {src}')
    (ROOT / 'cleanup_report.md').write_text('\n'.join(report), encoding='utf8')
    print('\n'.join(report))

if __name__=='__main__':
    main()

