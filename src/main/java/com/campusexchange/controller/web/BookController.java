package com.campusexchange.controller.web;

import com.campusexchange.dto.BookForm;
import com.campusexchange.dto.ListingDetail;
import com.campusexchange.model.Book;
import com.campusexchange.model.ItemCondition;
import com.campusexchange.service.BookService;
import com.campusexchange.service.ListingSort;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Server-rendered textbook pages: browse, detail, create and edit.
 * Sell / relist / delete live in {@link ListingActionsController}.
 */
@Controller
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public String listBooks(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "semester", required = false) Integer semester,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "sort", defaultValue = "newest") String sort,
            Model model) {

        Page<Book> booksPage = bookService.searchAvailableBooks(q, semester, page, size, sort);
        model.addAttribute("booksPage", booksPage);
        model.addAttribute("q", q);
        model.addAttribute("semester", semester);
        model.addAttribute("sort", ListingSort.fromKey(sort).getKey());
        model.addAttribute("sorts", ListingSort.values());
        model.addAttribute("currentPage", booksPage.getNumber());
        model.addAttribute("totalPages", booksPage.getTotalPages());
        return "books/list";
    }

    @GetMapping("/books/{id:[0-9]+}")
    public String viewBook(@PathVariable("id") Long id, Model model, Principal principal) {
        ListingDetail<Book> detail = bookService.getBookDetail(id, principal == null ? null : principal.getName());
        model.addAttribute("book", detail.getListing());
        model.addAttribute("seller", detail.getSeller());
        return "books/detail";
    }

    @GetMapping("/books/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("bookForm")) {
            model.addAttribute("bookForm", new BookForm());
        }
        return formView(model, null);
    }

    @PostMapping("/books")
    public String createBook(
            @Valid @ModelAttribute("bookForm") BookForm bookForm,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            return formView(model, null);
        }

        Book created = bookService.createBook(bookForm, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Your textbook is on the board. Classmates can find it now.");
        redirectAttributes.addFlashAttribute("justPublished", true);
        return "redirect:/books/" + created.getId();
    }

    @GetMapping("/books/{id:[0-9]+}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, Principal principal) {
        Book book = bookService.getOwnedBook(id, principal.getName());
        if (!model.containsAttribute("bookForm")) {
            model.addAttribute("bookForm", BookForm.from(book));
        }
        return formView(model, id);
    }

    @PostMapping("/books/{id:[0-9]+}")
    public String updateBook(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("bookForm") BookForm bookForm,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            // Still verify ownership so a non-owner can't probe the edit form.
            bookService.getOwnedBook(id, principal.getName());
            return formView(model, id);
        }

        bookService.updateBook(id, bookForm, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Changes saved.");
        return "redirect:/books/" + id;
    }

    private String formView(Model model, Long bookId) {
        model.addAttribute("conditions", ItemCondition.values());
        model.addAttribute("isEdit", bookId != null);
        model.addAttribute("bookId", bookId);
        return "books/form";
    }
}
