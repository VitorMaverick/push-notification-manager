package br.edu.acad.ifma.adapters.repository;

import br.edu.acad.ifma.adapters.model.DeviceJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceJpaRepository extends JpaRepository<DeviceJpaEntity, Long> {
    Optional<DeviceJpaEntity> findByFcmToken(String fcmToken);

    boolean existsByFcmToken(String fcmToken);
}
