package br.edu.acad.ifma.repository;

import br.edu.acad.ifma.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationMessageRepository extends JpaRepository<Notification, Long> {}
