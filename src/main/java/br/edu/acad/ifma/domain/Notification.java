package br.edu.acad.ifma.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(length = 2000)
    private String body;

    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(name = "recipient_token")
    private String recipientToken;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Notification() {}

    public Notification(String subject, String body, NotificationChannel channel) {
        this.subject = subject;
        this.body = body;
        this.channel = channel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public String getRecipientToken() {
        return recipientToken;
    }

    public void setRecipientToken(String recipientToken) {
        this.recipientToken = recipientToken;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
