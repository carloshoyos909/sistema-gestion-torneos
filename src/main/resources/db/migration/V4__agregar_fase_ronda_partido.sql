-- Agregar campos de fase y ronda a partidos para soportar eliminatorias y grupos
ALTER TABLE partidos
    ADD COLUMN fase  VARCHAR(40),
    ADD COLUMN ronda INTEGER;
