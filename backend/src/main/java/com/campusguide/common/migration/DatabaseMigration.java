package com.campusguide.common.migration;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "db_migrations")
public class DatabaseMigration {
    @Id
    private String version;
    private String description;
    private Instant executedAt;
    private long executionTimeMillis;
    private boolean success;

    public DatabaseMigration() {}

    public DatabaseMigration(String version, String description, Instant executedAt, long executionTimeMillis, boolean success) {
        this.version = version;
        this.description = description;
        this.executedAt = executedAt;
        this.executionTimeMillis = executionTimeMillis;
        this.success = success;
    }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }

    public long getExecutionTimeMillis() { return executionTimeMillis; }
    public void setExecutionTimeMillis(long executionTimeMillis) { this.executionTimeMillis = executionTimeMillis; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
