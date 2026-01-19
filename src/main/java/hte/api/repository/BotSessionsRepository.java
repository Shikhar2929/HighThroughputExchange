package hte.api.repository;

import hte.api.entities.Session;

public interface BotSessionsRepository
        extends KeyValueRepository<String, Session>, Deletable<String> {}
