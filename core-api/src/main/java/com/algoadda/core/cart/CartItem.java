package com.algoadda.core.cart;

import com.algoadda.core.listing.Listing;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cart_listing", columnNames = {"cart_id", "listing_id"})
})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    public CartItem() {
    }

    public CartItem(UUID id, Cart cart, Listing listing, Instant addedAt) {
        this.id = id;
        this.cart = cart;
        this.listing = listing;
        this.addedAt = addedAt;
    }

    public static CartItemBuilder builder() {
        return new CartItemBuilder();
    }

    public static class CartItemBuilder {
        private UUID id;
        private Cart cart;
        private Listing listing;
        private Instant addedAt;

        public CartItemBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public CartItemBuilder cart(Cart cart) {
            this.cart = cart;
            return this;
        }

        public CartItemBuilder listing(Listing listing) {
            this.listing = listing;
            return this;
        }

        public CartItemBuilder addedAt(Instant addedAt) {
            this.addedAt = addedAt;
            return this;
        }

        public CartItem build() {
            return new CartItem(id, cart, listing, addedAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }
}
