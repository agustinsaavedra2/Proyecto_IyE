Limpieza recomendada del directorio ml

Acciones propuestas (no destructivas):
- Consolidar dependencias en `ml/requirements.txt` (se añadió fastapi y uvicorn).
- Mantener `ml/service/app.py` y eliminar `ml/service/requirements.txt` (si se desea) porque ahora las dependencias están centralizadas en `ml/requirements.txt`.
- Mantener scripts principales: `prepare_dataset.py`, `trainer.py`, `predict.py`, `ml/scripts/generate_policy.py`.
- Archivar archivos duplicados/innecesarios en `ml/_cleanup_backup/` usando `ml/scripts/archive_unused.py`.

Sugerencias:
- Revisar `ml/scripts/` y marcar scripts que no se usen en producción (ej: scripts experimentales) y moverlos a `_cleanup_backup`.
- Añadir un README en `ml/scripts/` con el catálogo de scripts y su propósito.

Para ejecutar (dry-run):

python ml/scripts/archive_unused.py --dry-run

Luego para aplicar:

python ml/scripts/archive_unused.py

