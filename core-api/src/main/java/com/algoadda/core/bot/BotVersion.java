package com.algoadda.core.bot;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "bot_versions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_bot_version", columnNames = {"bot_id", "version_number"})
    }
)
public class BotVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_id", nullable = false)
    private Bot bot;

    @Column(name = "version_number", nullable = false, length = 50)
    private String versionNumber;

    @Column(name = "disclosed_logic", nullable = false, columnDefinition = "TEXT")
    private String disclosedLogic;

    @Column(name = "file_storage_key", length = 500)
    private String fileStorageKey;

    @Column(columnDefinition = "TEXT")
    private String changelog;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BotVersion() {
    }

    public BotVersion(UUID id, Bot bot, String versionNumber, String disclosedLogic, String fileStorageKey, String changelog, Instant createdAt) {
        this.id = id;
        this.bot = bot;
        this.versionNumber = versionNumber;
        this.disclosedLogic = disclosedLogic;
        this.fileStorageKey = fileStorageKey;
        this.changelog = changelog;
        this.createdAt = createdAt;
    }

    public static BotVersionBuilder builder() {
        return new BotVersionBuilder();
    }

    public static class BotVersionBuilder {
        private UUID id;
        private Bot bot;
        private String versionNumber;
        private String disclosedLogic;
        private String fileStorageKey;
        private String changelog;
        private Instant createdAt;

        public BotVersionBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public BotVersionBuilder bot(Bot bot) {
            this.bot = bot;
            return this;
        }

        public BotVersionBuilder versionNumber(String versionNumber) {
            this.versionNumber = versionNumber;
            return this;
        }

        public BotVersionBuilder disclosedLogic(String disclosedLogic) {
            this.disclosedLogic = disclosedLogic;
            return this;
        }

        public BotVersionBuilder fileStorageKey(String fileStorageKey) {
            this.fileStorageKey = fileStorageKey;
            return this;
        }

        public BotVersionBuilder changelog(String changelog) {
            this.changelog = changelog;
            return this;
        }

        public BotVersionBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BotVersion build() {
            return new BotVersion(id, bot, versionNumber, disclosedLogic, fileStorageKey, changelog, createdAt);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getDisclosedLogic() {
        return disclosedLogic;
    }

    public void setDisclosedLogic(String disclosedLogic) {
        this.disclosedLogic = disclosedLogic;
    }

    public String getFileStorageKey() {
        return fileStorageKey;
    }

    public void setFileStorageKey(String fileStorageKey) {
        this.fileStorageKey = fileStorageKey;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
