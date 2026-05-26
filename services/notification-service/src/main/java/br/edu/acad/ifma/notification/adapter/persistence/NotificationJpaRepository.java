package br.edu.acad.ifma.notification.adapter.persistence;

import br.edu.acad.ifma.notification.domain.NotificationStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {

    Optional<NotificationJpaEntity> findByFcmMessageId(String fcmMessageId);

    @Query("SELECT n FROM NotificationJpaEntity n WHERE " +
           "(:status IS NULL OR n.status = :status) AND " +
           "(:fcmToken IS NULL OR n.recipientToken = :fcmToken)")
    Page<NotificationJpaEntity> findAllFiltered(
            @Param("status") NotificationStatus status,
            @Param("fcmToken") String fcmToken,
            Pageable pageable);
}
