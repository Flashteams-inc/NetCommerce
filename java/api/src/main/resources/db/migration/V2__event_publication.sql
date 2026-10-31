-- V2: the table Spring Modulith keeps its event publications in (its own
-- schema-postgresql.sql, spring-modulith-events-jdbc 1.4.14). The only schema
-- addition the Java application makes.
CREATE TABLE IF NOT EXISTS public.event_publication
(
  id               UUID NOT NULL,
  listener_id      TEXT NOT NULL,
  event_type       TEXT NOT NULL,
  serialized_event TEXT NOT NULL,
  publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
  completion_date  TIMESTAMP WITH TIME ZONE,
  PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash_idx ON public.event_publication USING hash(serialized_event);
CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx ON public.event_publication (completion_date);
