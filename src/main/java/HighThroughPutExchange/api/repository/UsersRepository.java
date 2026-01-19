package HighThroughPutExchange.api.repository;

import HighThroughPutExchange.api.entities.User;

public interface UsersRepository extends KeyValueRepository<String, User>, KeyEnumerable<String> {}
