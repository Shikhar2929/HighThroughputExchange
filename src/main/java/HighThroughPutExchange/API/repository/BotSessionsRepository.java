package HighThroughPutExchange.api.repository;

import HighThroughPutExchange.api.entities.Session;

public interface BotSessionsRepository
        extends KeyValueRepository<String, Session>, Deletable<String> {}
