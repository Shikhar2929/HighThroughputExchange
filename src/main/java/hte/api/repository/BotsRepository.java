package hte.api.repository;

import hte.api.entities.User;

public interface BotsRepository extends KeyValueRepository<String, User>, KeyEnumerable<String> {}
