package com.campusexchange.service;

import com.campusexchange.dto.DeviceResponse;
import com.campusexchange.dto.SellerCard;
import com.campusexchange.model.Book;
import com.campusexchange.model.DeviceCategory;
import com.campusexchange.model.ItemCondition;
import com.campusexchange.model.ListingStatus;
import com.campusexchange.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Plain unit tests for the deepened modules: no Spring context, no database. */
class SummaryAndSellerCardTest {

    private final User seller = new User("Aarav Patel", "aarav@campus.edu", "hash", "9876543210", "CSE");

    @Test
    void anonymousViewerSeesNoContact() {
        SellerCard card = SellerCard.forViewer(seller, null);
        assertThat(card.getFullName()).isEqualTo("Aarav Patel");
        assertThat(card.getDepartment()).isEqualTo("CSE");
        assertThat(card.isContactVisible()).isFalse();
        assertThat(card.getEmail()).isNull();
        assertThat(card.getPhone()).isNull();
        assertThat(card.isViewerIsOwner()).isFalse();
    }

    @Test
    void signedInViewerSeesContact() {
        SellerCard card = SellerCard.forViewer(seller, "priya@campus.edu");
        assertThat(card.isContactVisible()).isTrue();
        assertThat(card.getEmail()).isEqualTo("aarav@campus.edu");
        assertThat(card.getPhone()).isEqualTo("9876543210");
        assertThat(card.isViewerIsOwner()).isFalse();
    }

    @Test
    void ownerIsRecognisedCaseInsensitively() {
        assertThat(SellerCard.forViewer(seller, " AARAV@campus.edu ").isViewerIsOwner()).isTrue();
    }

    @Test
    void summaryTotalsActiveSoldAndValue() {
        MyListingsSummary summary = MyListingsSummary.of(
                List.of(book("400.00", ListingStatus.SOLD), book("650.00", ListingStatus.AVAILABLE)),
                List.of(device("950.50", ListingStatus.SOLD), device("28000.00", ListingStatus.AVAILABLE)));

        assertThat(summary.getActiveCount()).isEqualTo(2);
        assertThat(summary.getSoldCount()).isEqualTo(2);
        assertThat(summary.getSoldValue()).isEqualByComparingTo("1350.50");
        assertThat(summary.isEmpty()).isFalse();
        assertThat(MyListingsSummary.of(List.of(), List.of()).isEmpty()).isTrue();
    }

    @Test
    void sortKeysAndEnumVocabulary() {
        assertThat(ListingSort.fromKey("price_desc")).isEqualTo(ListingSort.PRICE_DESC);
        assertThat(ListingSort.fromKey("desc")).isEqualTo(ListingSort.NEWEST);
        assertThat(ListingSort.fromKey("asc")).isEqualTo(ListingSort.OLDEST);
        assertThat(ListingSort.fromKey(null)).isEqualTo(ListingSort.NEWEST);
        assertThat(DeviceCategory.isKnown(" laptop ")).isTrue();
        assertThat(DeviceCategory.isKnown("TOASTER")).isFalse();
        assertThat(ItemCondition.NEW.getGrade()).isEqualTo(4);
        assertThat(ItemCondition.FAIR.getGrade()).isEqualTo(1);
    }

    private Book book(String price, ListingStatus status) {
        Book b = new Book();
        b.setTitle("t");
        b.setAuthor("a");
        b.setPrice(new BigDecimal(price));
        b.setStatus(status);
        b.setCondition(ItemCondition.GOOD);
        return b;
    }

    private DeviceResponse device(String price, ListingStatus status) {
        DeviceResponse d = new DeviceResponse();
        d.setPrice(new BigDecimal(price));
        d.setStatus(status);
        return d;
    }
}
