package com.abo.todo;

import com.abo.SpringSecurityLjugApplication;
import com.abo.security.LoggedUserGetter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by joanna on 05.08.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringSecurityLjugApplication.class)
@WebAppConfiguration
public class TodoTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void readTodoTest() throws Exception {
        this.mockMvc.perform(get("/users/james_bond/todos/1").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("asdada"))
                .andExpect(jsonPath("$.user.login").value("james_bond"))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/users/james_bond/todos/1"));
    }

    @Test
    public void readTodosTest() throws Exception {
        this.mockMvc.perform(get("/users/harry_potter/todos").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].content").value("aaaaa"))
                .andExpect(jsonPath("$[0].user.login").value("harry_potter"));
    }

    @Test
    public void deleteExistingTodoTest() throws Exception {
        this.mockMvc.perform(delete("/users/james_bond/todos/1"))
                .andExpect(status().isOk());
    }
}
