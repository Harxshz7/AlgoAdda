package com.algoadda.core.listing;

import com.algoadda.core.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "listing_views")
public class ListingView {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User viewer;

    @CreationTimestamp
    @Column(name = "viewed_at", nullable = false, updatable = false)
    private Instant viewedAt;

    public ListingView() {
    }

    public ListingView(UUID id, Listing listing, User viewer, Instant viewedAt) {
        this.id = id;
        this.listing = listing;
        this.viewer = viewer;
        this.viewedAt = viewedAt;
    }

    public static ListingViewBuilder builder() {
        return new ListingViewBuilder();
    }

    public static class ListingViewBuilder {
        private UUID id;
        private Listing listing;
        private User viewer;
        private Instant viewedAt;

        public ListingViewBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ListingViewBuilder listing(Listing listing) {
            this.listing = listing;
            return this;
        }

        public ListingViewBuilder viewer(User viewer) {
            this.viewer = viewer;
            return this;
        }

        public ListingViewBuilder viewedAt(Instant viewedAt) {
            this.viewedAt = viewedAt;
            return this;
        }

        public ListingView build() {
            return new ListingView(id, listing, viewer, viewedAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public User getViewer() {
        return viewer;
    }

    public void setViewer(User viewer) {
        this.viewer = viewer;
    }

    public Instant getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(Instant viewedAt) {
        this.viewedAt = viewedAt;
    }
}
