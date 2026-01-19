package HighThroughPutExchange.API.repository;

import HighThroughPutExchange.API.database_objects.Session;

public interface BotSessionsRepository
        extends KeyValueRepository<String, Session>, Deletable<String> {}
