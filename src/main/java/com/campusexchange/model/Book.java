package com.campusexchange.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/** A used textbook listed by a student. Shared listing fields live in {@link Listing}. */
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_books_title", columnList = "title"),
    @Index(name = "idx_books_status", columnList = "status")
})
public class Book extends Listing {

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String author;

    @Size(max = 20)
    @Column(length = 20)
    private String isbn;

    @Size(max = 100)
    @Column(length = 100)
    private String subject;

    @Min(1)
    @Max(8)
    private Integer semester;

    public Book() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }
}
