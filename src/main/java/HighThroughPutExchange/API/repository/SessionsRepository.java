package HighThroughPutExchange.API.repository;

import HighThroughPutExchange.API.database_objects.Session;

public interface SessionsRepository
        extends KeyValueRepository<String, Session>, Deletable<String> {}
