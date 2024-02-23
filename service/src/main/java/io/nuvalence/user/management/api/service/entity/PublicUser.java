package io.nuvalence.user.management.api.service.entity;

import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@DiscriminatorValue("public")
@Builder
public class PublicUser extends UserEntity {
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "profile_id")
    private IndividualProfile profile;
}
