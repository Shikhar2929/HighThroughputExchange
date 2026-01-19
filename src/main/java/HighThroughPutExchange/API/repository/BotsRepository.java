package HighThroughPutExchange.API.repository;

import HighThroughPutExchange.API.database_objects.User;

public interface BotsRepository extends KeyValueRepository<String, User>, KeyEnumerable<String> {}
