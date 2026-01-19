package HighThroughPutExchange.api.repository;

import HighThroughPutExchange.api.entities.Session;

public interface SessionsRepository
        extends KeyValueRepository<String, Session>, Deletable<String> {}
