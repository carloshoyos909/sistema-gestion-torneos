-- Agregar columna creador_id a torneos (nullable para compatibilidad con datos existentes)
ALTER TABLE torneos
    ADD COLUMN creador_id BIGINT,
    ADD CONSTRAINT fk_torneo_creador
        FOREIGN KEY (creador_id) REFERENCES usuarios(id) ON DELETE SET NULL;

CREATE INDEX idx_torneos_creador ON torneos(creador_id);
