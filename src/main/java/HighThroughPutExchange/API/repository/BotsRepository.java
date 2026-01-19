package HighThroughPutExchange.api.repository;

import HighThroughPutExchange.api.entities.User;

public interface BotsRepository extends KeyValueRepository<String, User>, KeyEnumerable<String> {}
