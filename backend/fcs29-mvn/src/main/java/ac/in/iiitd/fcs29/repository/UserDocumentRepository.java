package ac.in.iiitd.fcs29.repository;

import ac.in.iiitd.fcs29.entity.UserDocument;
import ac.in.iiitd.fcs29.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserDocumentRepository extends JpaRepository<UserDocument, UUID> {
    Optional<UserDocument> findByUser(User user);
}