package com.campusexchange.controller.web;

import com.campusexchange.exception.ResourceNotFoundException;
import com.campusexchange.model.ListingKind;
import com.campusexchange.model.ListingStatus;
import com.campusexchange.service.ListingLifecycle;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Owner actions shared by every kind of listing:
 * {@code POST /books/{id}/sold}, {@code /available}, {@code /delete} and the same under {@code /devices}.
 * All rules live in {@link ListingLifecycle}.
 */
@Controller
@RequestMapping("/{kind:books|devices}/{id:[0-9]+}")
public class ListingActionsController {

    private final ListingLifecycle lifecycle;

    public ListingActionsController(ListingLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    @PostMapping("/sold")
    public String markSold(@PathVariable("kind") String kind, @PathVariable("id") Long id,
                           Principal principal, RedirectAttributes redirect) {
        ListingKind listingKind = kindOf(kind);
        lifecycle.changeStatus(listingKind, id, ListingStatus.SOLD, principal.getName());
        redirect.addFlashAttribute("successMessage", "Marked as sold. Nice one.");
        redirect.addFlashAttribute("justSold", true);
        return detailRedirect(listingKind, id);
    }

    /** Undo for "mark as sold": puts the listing back on the board. */
    @PostMapping("/available")
    public String markAvailable(@PathVariable("kind") String kind, @PathVariable("id") Long id,
                                Principal principal, RedirectAttributes redirect) {
        ListingKind listingKind = kindOf(kind);
        lifecycle.changeStatus(listingKind, id, ListingStatus.AVAILABLE, principal.getName());
        redirect.addFlashAttribute("successMessage", "Back on the board and visible to buyers.");
        return detailRedirect(listingKind, id);
    }

    @PostMapping("/delete")
    public String withdraw(@PathVariable("kind") String kind, @PathVariable("id") Long id,
                           Principal principal, RedirectAttributes redirect) {
        lifecycle.withdraw(kindOf(kind), id, principal.getName());
        redirect.addFlashAttribute("successMessage", "Listing removed.");
        return "redirect:/my/listings";
    }

    private static ListingKind kindOf(String segment) {
        return ListingKind.fromPathSegment(segment)
                .orElseThrow(() -> new ResourceNotFoundException("No such page"));
    }

    private static String detailRedirect(ListingKind kind, Long id) {
        return "redirect:/" + kind.getPathSegment() + "/" + id;
    }
}
