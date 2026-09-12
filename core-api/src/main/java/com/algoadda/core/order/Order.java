package com.algoadda.core.order;

import com.algoadda.core.listing.Listing;
import com.algoadda.core.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(name = "payment_reference", length = 255)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status = OrderStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Order() {
    }

    public Order(UUID id, User buyer, Listing listing, String paymentReference, OrderStatus status, Instant createdAt) {
        this.id = id;
        this.buyer = buyer;
        this.listing = listing;
        this.paymentReference = paymentReference;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.createdAt = createdAt;
    }

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public static class OrderBuilder {
        private UUID id;
        private User buyer;
        private Listing listing;
        private String paymentReference;
        private OrderStatus status = OrderStatus.PENDING;
        private Instant createdAt;

        public OrderBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public OrderBuilder buyer(User buyer) {
            this.buyer = buyer;
            return this;
        }

        public OrderBuilder listing(Listing listing) {
            this.listing = listing;
            return this;
        }

        public OrderBuilder paymentReference(String paymentReference) {
            this.paymentReference = paymentReference;
            return this;
        }

        public OrderBuilder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public OrderBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Order build() {
            return new Order(id, buyer, listing, paymentReference, status, createdAt);
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

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
