package hte.api.repository;

import hte.api.entities.User;

public interface UsersRepository extends KeyValueRepository<String, User>, KeyEnumerable<String> {}
