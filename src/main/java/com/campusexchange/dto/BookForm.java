package com.campusexchange.dto;

import com.campusexchange.model.Book;
import com.campusexchange.model.ItemCondition;
import com.campusexchange.util.Text;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Web form for creating and editing a textbook listing. */
public class BookForm {

    @NotBlank(message = "Enter the book title")
    @Size(max = 200, message = "Title must be 200 characters or fewer")
    private String title;

    @NotBlank(message = "Enter the author or authors")
    @Size(max = 150, message = "Author must be 150 characters or fewer")
    private String author;

    @Size(max = 20, message = "ISBN must be 20 characters or fewer")
    @Pattern(regexp = ValidationRules.ISBN, message = "Enter a 10- or 13-digit ISBN, e.g. 978-0262033848")
    private String isbn;

    @Size(max = 100, message = "Subject must be 100 characters or fewer")
    private String subject;

    @Min(value = 1, message = "Semester must be between 1 and 8")
    @Max(value = 8, message = "Semester must be between 1 and 8")
    private Integer semester;

    @NotNull(message = "Choose the condition")
    private ItemCondition condition;

    @NotNull(message = "Enter a price")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @DecimalMax(value = ValidationRules.MAX_PRICE, message = "Price must be ₹5,00,000 or less")
    @Digits(integer = 8, fraction = 2, message = "Use at most 2 decimal places, e.g. 450.50")
    private BigDecimal price;

    @Size(max = 1000, message = "Description must be 1000 characters or fewer")
    private String description;

    public BookForm() {
    }

    /** Pre-fills the edit form from an existing listing. */
    public static BookForm from(Book book) {
        BookForm form = new BookForm();
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setIsbn(book.getIsbn());
        form.setSubject(book.getSubject());
        form.setSemester(book.getSemester());
        form.setCondition(book.getCondition());
        form.setPrice(book.getPrice());
        form.setDescription(book.getDescription());
        return form;
    }

    /** Copies the (validated) form values onto the entity, normalising whitespace. */
    public void applyTo(Book book) {
        book.setTitle(Text.trimToNull(title));
        book.setAuthor(Text.trimToNull(author));
        book.setIsbn(Text.trimToNull(isbn));
        book.setSubject(Text.trimToNull(subject));
        book.setSemester(semester);
        book.setCondition(condition);
        book.setPrice(price);
        book.setDescription(Text.trimToNull(description));
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

    public ItemCondition getCondition() {
        return condition;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
