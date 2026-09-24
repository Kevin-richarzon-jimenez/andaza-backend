-- Los comentarios se publican al instante; el administrador puede ocultarlos después.
-- PENDING/APPROVED/REJECTED pasan a PUBLISHED/HIDDEN.
ALTER TABLE comments DROP CONSTRAINT comments_status_check;

UPDATE comments SET status = CASE status WHEN 'REJECTED' THEN 'HIDDEN' ELSE 'PUBLISHED' END;

ALTER TABLE comments
    ALTER COLUMN status SET DEFAULT 'PUBLISHED',
    ADD CONSTRAINT comments_status_check CHECK (status IN ('PUBLISHED', 'HIDDEN'));
