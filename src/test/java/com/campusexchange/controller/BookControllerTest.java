package com.campusexchange.controller;

import com.campusexchange.dto.BookForm;
import com.campusexchange.model.Book;
import com.campusexchange.model.ItemCondition;
import com.campusexchange.model.ListingStatus;
import com.campusexchange.model.User;
import com.campusexchange.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    @DisplayName("Public GET /books returns 200 OK and renders books/list template")
    void testPublicGetBooks() throws Exception {
        when(bookService.searchAvailableBooks(any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("booksPage"));
    }

    @Test
    @DisplayName("Unauthenticated GET /books/new redirects to /login")
    void testUnauthenticatedGetNewRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/books/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student1@campus.edu", roles = {"USER"})
    @DisplayName("Authenticated GET /books/new returns 200 OK and renders books/form")
    void testAuthenticatedGetNewReturnsForm() throws Exception {
        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeExists("bookForm"));
    }

    @Test
    @WithMockUser(username = "student1@campus.edu", roles = {"USER"})
    @DisplayName("Authenticated POST /books with valid data redirects to /books/{id}")
    void testCreateBookSuccess() throws Exception {
        User seller = new User("Student 1", "student1@campus.edu", "pass", "123", "CSE");
        Book created = new Book();
        created.setId(101L);
        created.setTitle("Algorithms");
        created.setAuthor("CLRS");
        created.setPrice(new BigDecimal("500.00"));
        created.setCondition(ItemCondition.GOOD);
        created.setStatus(ListingStatus.AVAILABLE);
        created.setSeller(seller);

        when(bookService.createBook(any(BookForm.class), eq("student1@campus.edu")))
                .thenReturn(created);

        mockMvc.perform(post("/books")
                        .with(csrf())
                        .param("title", "Algorithms")
                        .param("author", "CLRS")
                        .param("price", "500.00")
                        .param("condition", "GOOD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/101"));
    }

    @Test
    @WithMockUser(username = "student1@campus.edu", roles = {"USER"})
    @DisplayName("POST /books with blank title fails validation and re-renders form")
    void testCreateBookValidationFailure() throws Exception {
        mockMvc.perform(post("/books")
                        .with(csrf())
                        .param("title", "") // Invalid blank title
                        .param("author", "CLRS")
                        .param("price", "500.00")
                        .param("condition", "GOOD"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().hasErrors());
    }
}
