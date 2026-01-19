package HighThroughPutExchange.API.repository;

import HighThroughPutExchange.API.database_objects.User;

public interface UsersRepository extends KeyValueRepository<String, User>, KeyEnumerable<String> {}
