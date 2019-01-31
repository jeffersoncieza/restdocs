package com.skycode.restdocs.controller;

import com.skycode.restdocs.entity.Person;
import com.skycode.restdocs.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/people")
public class PersonController {

    @Autowired
    private PersonRepository repository;

    @GetMapping
    public Iterable<Person> listPeople() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Person getPerson(@PathVariable Long id) {
        return repository.findById(id).get();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createPerson(@RequestBody Person person) {
        repository.save(new Person(person.getFirstName(), person.getLastName()));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePerson(@PathVariable Long id, @RequestBody Person person) {
        Person existingPerson = repository.findById(id).get();
        existingPerson.setFirstName(person.getFirstName());
        existingPerson.setLastName(person.getLastName());
        repository.save(existingPerson);
    }
}
