#!/bin/sh
# Simple manual backup script (alternative to docker container loop).
set -e
PGHOST=${PGHOST:-localhost}
PGUSER=${PGUSER:-postgres}
PGDATABASE=${PGDATABASE:-postgres}
PGPASSWORD=${PGPASSWORD:-postgres}
BACKUP_DIR=${BACKUP_DIR:-./backups}

mkdir -p "$BACKUP_DIR"
TIMESTAMP=$(date +"%Y%m%d%H%M%S")
FILENAME="$BACKUP_DIR/pgdump-$PGDATABASE-$TIMESTAMP.dump"

echo "Dumping DB $PGDATABASE to $FILENAME"
PGPASSWORD="$PGPASSWORD" pg_dump -h "$PGHOST" -U "$PGUSER" -F c -b -v -f "$FILENAME" "$PGDATABASE"

# optional ML artifacts archive
if [ -d ./ml_artifacts ] && [ "$(ls -A ./ml_artifacts)" ]; then
  TARFILE="$BACKUP_DIR/ml-artifacts-$TIMESTAMP.tar.gz"
  echo "Archiving ML artifacts to $TARFILE"
  tar -czf "$TARFILE" -C ./ml_artifacts .
fi

# retention: delete >7 days
find "$BACKUP_DIR" -type f -mtime +7 -exec rm -f {} \;

echo "Backup completed"

