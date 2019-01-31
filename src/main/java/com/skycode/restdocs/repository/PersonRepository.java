package com.skycode.restdocs.repository;

import com.skycode.restdocs.entity.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, Long> {
}
