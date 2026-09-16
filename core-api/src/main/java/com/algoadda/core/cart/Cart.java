package com.algoadda.core.cart;

import com.algoadda.core.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false, unique = true)
    private User buyer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Cart() {
    }

    public Cart(UUID id, User buyer, List<CartItem> items, Instant createdAt) {
        this.id = id;
        this.buyer = buyer;
        if (items != null) {
            this.items = items;
        }
        this.createdAt = createdAt;
    }

    public static CartBuilder builder() {
        return new CartBuilder();
    }

    public static class CartBuilder {
        private UUID id;
        private User buyer;
        private List<CartItem> items = new ArrayList<>();
        private Instant createdAt;

        public CartBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public CartBuilder buyer(User buyer) {
            this.buyer = buyer;
            return this;
        }

        public CartBuilder items(List<CartItem> items) {
            this.items = items;
            return this;
        }

        public CartBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Cart build() {
            return new Cart(id, buyer, items, createdAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
