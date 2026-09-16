package com.algoadda.core.order;

import com.algoadda.core.bot.BotVersion;
import com.algoadda.core.listing.Listing;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_version_id", nullable = false)
    private BotVersion botVersion;

    @Column(name = "price_at_purchase", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtPurchase = BigDecimal.ZERO;

    public OrderItem() {
    }

    public OrderItem(UUID id, Order order, Listing listing, BotVersion botVersion, BigDecimal priceAtPurchase) {
        this.id = id;
        this.order = order;
        this.listing = listing;
        this.botVersion = botVersion;
        this.priceAtPurchase = priceAtPurchase != null ? priceAtPurchase : BigDecimal.ZERO;
    }

    public static OrderItemBuilder builder() {
        return new OrderItemBuilder();
    }

    public static class OrderItemBuilder {
        private UUID id;
        private Order order;
        private Listing listing;
        private BotVersion botVersion;
        private BigDecimal priceAtPurchase = BigDecimal.ZERO;

        public OrderItemBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public OrderItemBuilder order(Order order) {
            this.order = order;
            return this;
        }

        public OrderItemBuilder listing(Listing listing) {
            this.listing = listing;
            return this;
        }

        public OrderItemBuilder botVersion(BotVersion botVersion) {
            this.botVersion = botVersion;
            return this;
        }

        public OrderItemBuilder priceAtPurchase(BigDecimal priceAtPurchase) {
            this.priceAtPurchase = priceAtPurchase;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(id, order, listing, botVersion, priceAtPurchase);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public BotVersion getBotVersion() {
        return botVersion;
    }

    public void setBotVersion(BotVersion botVersion) {
        this.botVersion = botVersion;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }
}
