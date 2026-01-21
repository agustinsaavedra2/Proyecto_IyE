-- Esquema para almacenar chunks y registros gold
-- Tabla para chunks (si quieres importar chunks individuales)
CREATE TABLE IF NOT EXISTS chunks (
  chunk_id TEXT PRIMARY KEY,
  source_pdf TEXT,
  text TEXT,
  records JSONB,
  tokens INTEGER,
  metadata JSONB
);

-- Tabla para gold cases (etiquetas)
CREATE TABLE IF NOT EXISTS gold_cases (
  id TEXT PRIMARY KEY,
  source_pdf TEXT,
  table_id TEXT,
  row_number INTEGER,
  fields JSONB,
  synthetic_text TEXT,
  label TEXT,
  reason TEXT
);

