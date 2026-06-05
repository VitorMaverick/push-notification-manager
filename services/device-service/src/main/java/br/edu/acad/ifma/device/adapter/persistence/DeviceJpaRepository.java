package br.edu.acad.ifma.device.adapter.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceJpaRepository extends JpaRepository<DeviceJpaEntity, Long> {
    Optional<DeviceJpaEntity> findByFcmToken(String fcmToken);
    boolean existsByFcmToken(String fcmToken);
}
