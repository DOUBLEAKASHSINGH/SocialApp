package ac.in.iiitd.fcs29.repository;

import ac.in.iiitd.fcs29.entity.UserPairKeys;
import ac.in.iiitd.fcs29.entity.embeddable.UserPairId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPairKeysRepository extends JpaRepository<UserPairKeys, UserPairId> {
}