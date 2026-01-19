package hte.api.repository;

import hte.api.entities.Session;

public interface SessionsRepository
        extends KeyValueRepository<String, Session>, Deletable<String> {}
