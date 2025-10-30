/*
 * Class name
 *	User
 *
 * Version info
 *	JavaSE-17
 *
 * Copyright notice
 *
 * Author info
 *	Name: Sharad Jain
 *	Email-ID: sharad24138@iiitd.ac.in
 *
 * Creation date
 * 	29-01-2025
 *
 * Last updated By
 * 	Sharad Jain
 *
 * Last updated Date
 * 	11-02-2025
 *
 * Description
 * 	This class is entity for user login.
 */

package ac.in.iiitd.fcs29.entity;

import ac.in.iiitd.fcs29.dto.enums.Role;
import ac.in.iiitd.fcs29.entity.converter.EncryptionConverter;
import ac.in.iiitd.fcs29.entity.converter.FileEncryptionConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class is entity for user login.
 *
 * @author sharadjain
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "user")
public class User implements UserDetails {
    @Serial
    private static final long serialVersionUID = -7235635968325478610L;

    // columns for entity.
    @Id
    @Size(min = 2, max = 320)
    @Column(name = "user_id")
    private String email;

    @Column(name = "first_name")
    @NotNull
    @Size(min = 2, max = 50)
    private String firstName;

    @Column(name = "last_name")
    @Size(max = 50)
    private String lastName;

    @NotNull
    @JsonIgnore
    @ToString.Exclude
    private String password;

    // `BLOB`: Holds up to 64 KB.
    // `MEDIUMBLOB`: Holds up to 16 MB.
    // `LONGBLOB`: Holds up to 4 GB.
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "profile_picture", columnDefinition = "MEDIUMBLOB")
    @JsonIgnore
    @ToString.Exclude
    @Convert(converter = FileEncryptionConverter.class)
    private byte[] profilePicture = new byte[0];

    @Column(name = "profile_picture_type")
    @Convert(converter = EncryptionConverter.class)
    private String profilePictureType;

    @Size(max = 500)
    @Convert(converter = EncryptionConverter.class)
    private String bio;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserRelations> relations = new ArrayList<>();

    @ManyToMany(mappedBy = "members")
    @JsonIgnore
    private List<ChatGroup> groups = new ArrayList<>();

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isVerified = false;
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isCredentialsExpired = false;
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAccountLocked = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime lastUpdatedAt;

    /**
     * @param relations the relations to set
     */
    public void setRelations(List<UserRelations> relations) {
        this.relations = relations;
        for (UserRelations relation : relations)
            relation.setUser(this);
    }

    public void addRelation(UserRelations relation) {
        this.relations.add(relation);
        relation.setUser(this);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            throw new IllegalStateException("Role is not assigned to the user.");
        }
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.isAccountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !this.isCredentialsExpired;
    }

}
