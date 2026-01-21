Postgres backup strategy

This project includes a lightweight backup container `pg_backup` in `docker-compose.yml` which:

- Runs `pg_dump` daily (24h loop) and stores compressed custom-format dumps in `./backups`.
- Archives `ml_artifacts` (if present) alongside DB dumps.
- Keeps 7-day retention (deletes files older than 7 days).

How it works

- The service uses the official `postgres:15` image and runs a shell loop that performs `pg_dump` using env variables set in `docker-compose.yml`.
- Backups are written to the host-mounted folder `./backups`.

Manual restore

1) Copy desired dump from `./backups` into a container or accessible path.
2) Use `pg_restore` to restore into a target database (BE CAREFUL: this will overwrite objects):

```bash
# Example (restore into `postgres` DB):
PGPASSWORD=${DB_PASSWORD} pg_restore -h localhost -U ${DB_POSTGRES_USER} -d ${DB_POSTGRES_NAME} -c /path/to/pgdump-yourdb.dump
```

Notes & recommendations

- For production use: use dedicated backup tooling (WAL shipping, base backups, object storage like S3).
- Consider encrypting backups at rest (gpg) and using network-backed storage.
- If ML artifacts are large, configure incremental sync (rsync) instead of full tar every time.

