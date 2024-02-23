package io.nuvalence.user.management.api.service.entity.profile;

import com.google.common.base.Objects;
import io.nuvalence.auth.access.AccessResource;
import io.nuvalence.user.management.api.service.entity.UpdateTrackedEntity;
import io.nuvalence.user.management.api.service.entity.UpdateTrackedEntityEventListener;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@AccessResource("individual_profile")
@Table(name = "individual_profile")
@EntityListeners(UpdateTrackedEntityEventListener.class)
public class IndividualProfile implements UpdateTrackedEntity {
    @Id
    @Column(name = "id", insertable = false, updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ssn", nullable = false)
    private String ssn;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @OneToOne(
            mappedBy = "individualForAddress",
            orphanRemoval = true,
            cascade = {CascadeType.ALL},
            fetch = FetchType.EAGER)
    private Address primaryAddress;

    @OneToOne(
            mappedBy = "individualForMailing",
            orphanRemoval = true,
            cascade = {CascadeType.ALL},
            fetch = FetchType.EAGER)
    private Address mailingAddress;

    @Column(name = "created_by", length = 36, nullable = false)
    protected String createdBy;

    @Column(name = "last_updated_by", length = 36, nullable = false)
    protected String lastUpdatedBy;

    @Column(name = "created_timestamp", nullable = false)
    protected OffsetDateTime createdTimestamp;

    @Column(name = "last_updated_timestamp", nullable = false)
    private OffsetDateTime lastUpdatedTimestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndividualProfile that = (IndividualProfile) o;
        return Objects.equal(id, that.id)
                && Objects.equal(ssn, that.ssn)
                && Objects.equal(firstName, that.firstName)
                && Objects.equal(middleName, that.middleName)
                && Objects.equal(lastName, that.lastName)
                && Objects.equal(email, that.email)
                && Objects.equal(phoneNumber, that.phoneNumber)
                && Objects.equal(primaryAddress, that.primaryAddress)
                && Objects.equal(mailingAddress, that.mailingAddress)
                && Objects.equal(createdBy, that.createdBy)
                && Objects.equal(lastUpdatedBy, that.lastUpdatedBy)
                && Objects.equal(createdTimestamp, that.createdTimestamp)
                && Objects.equal(lastUpdatedTimestamp, that.lastUpdatedTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                id,
                ssn,
                firstName,
                middleName,
                lastName,
                email,
                phoneNumber,
                createdBy,
                lastUpdatedBy,
                createdTimestamp,
                lastUpdatedTimestamp);
    }
}
