package ac.in.iiitd.fcs29.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Setter
@Getter
@Embeddable
public class UserPairId implements Serializable {

    @Column(name = "user1_id")
    private String user1Id;

    @Column(name = "user2_id")
    private String user2Id;

    public UserPairId() {}

    public UserPairId(String userA, String userB) {
        if (userA.compareTo(userB) < 0) {
            this.user1Id = userA;
            this.user2Id = userB;
        } else {
            this.user1Id = userB;
            this.user2Id = userA;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPairId that)) return false;
        return Objects.equals(user1Id, that.user1Id) &&
                Objects.equals(user2Id, that.user2Id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user1Id, user2Id);
    }
}
