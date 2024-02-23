package io.nuvalence.user.management.api.service.models.auditevents;

import lombok.Getter;

/**
 * Enum for business object type for audit events.
 */
public enum AuditEventBusinessObject {
    USER("user"),
    EMPLOYER("employer"),
    INDIVIDUAL("individual");

    @Getter private String value;

    AuditEventBusinessObject(String value) {
        this.value = value;
    }
}
