package com.skycode.restdocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skycode.restdocs.entity.Person;
import com.skycode.restdocs.repository.PersonRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PersonControllerTest {

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    private PersonRepository repository;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Test
    public void listPeople() throws Exception {
        createSamplePerson("George", "King");
        createSamplePerson("Mary", "Queen");

        this.mockMvc.perform(get("/people").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("people", responseFields(
                        fieldWithPath("[].id").description("The persons' ID"),
                        fieldWithPath("[].firstName").description("The persons' first name"),
                        fieldWithPath("[].lastName").description("The persons' last name")
                )));
    }

    @Test
    public void getPerson() throws Exception {
        Person samplePerson = createSamplePerson("Henry", "King");

        this.mockMvc.perform(get("/people/" + samplePerson.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("person", responseFields(
                        fieldWithPath("id").description("The person's ID"),
                        fieldWithPath("firstName").description("The person's first name"),
                        fieldWithPath("lastName").description("The person's last name")
                )));
    }

    @Test
    public void createPerson() throws Exception {
        Map<String, String> newPerson = new HashMap<>();
        newPerson.put("firstName", "Anne");
        newPerson.put("lastName", "Queen");

        ConstrainedFields fields = new ConstrainedFields(Person.class);

        this.mockMvc.perform(post("/people").contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(newPerson)))
                .andExpect(status().isCreated())
                .andDo(document("create-person", requestFields(
                        fields.withPath("firstName").description("The person's first name"),
                        fields.withPath("lastName").description("The person's last name")
                )));
    }

    @Test
    public void updatePerson() throws Exception {
        Person originalPerson = createSamplePerson("Victoria", "Queen");
        Map<String, String> updatedPerson = new HashMap<>();
        updatedPerson.put("firstName", "Edward");
        updatedPerson.put("lastName", "King");

        ConstrainedFields fields = new ConstrainedFields(Person.class);

        this.mockMvc.perform(put("/people/" + originalPerson.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedPerson)))
                .andExpect(status().isNoContent())
                .andDo(document("update-person", requestFields(
                        fields.withPath("firstName").description("The person's first name"),
                        fields.withPath("lastName").description("The person's last name")
                )));
    }

    private Person createSamplePerson(String firstName, String lastName) {
        return repository.save(new Person(firstName, lastName));
    }

    private static class ConstrainedFields {
        private final ConstraintDescriptions descriptions;

        ConstrainedFields(Class<?> input) {
            this.descriptions = new ConstraintDescriptions(input);
        }

        private FieldDescriptor withPath(String path) {
            return fieldWithPath(path).attributes(key("constraints")
                    .value(StringUtils.collectionToDelimitedString(
                            this.descriptions.descriptionsForProperty(path), ". ")));
        }
    }
}
