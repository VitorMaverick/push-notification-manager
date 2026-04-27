package br.edu.acad.ifma.adapters.api.rest;

import br.edu.acad.ifma.adapters.api.rest.inbound.SendNotificationRequest;
import br.edu.acad.ifma.adapters.api.rest.outbound.NotificationDetailResponse;
import br.edu.acad.ifma.adapters.api.rest.outbound.NotificationResponse;
import br.edu.acad.ifma.adapters.api.rest.outbound.NotificationSummaryResponse;
import br.edu.acad.ifma.adapters.api.rest.presenter.NotificationPresenter;
import br.edu.acad.ifma.adapters.inbound.FcmAckRequest;
import br.edu.acad.ifma.app.domain.notification.NotificationStatus;
import br.edu.acad.ifma.app.domain.notification.PushNotification;
import br.edu.acad.ifma.app.port.NotificationRepositoryPort;
import br.edu.acad.ifma.app.usecase.notification.GetNotificationByIdUseCase;
import br.edu.acad.ifma.app.usecase.notification.GetNotificationHistoryUseCase;
import br.edu.acad.ifma.app.usecase.notification.NotificationFilter;
import br.edu.acad.ifma.app.usecase.notification.NotificationHistoryQuery;
import br.edu.acad.ifma.app.usecase.notification.SendPushNotificationCommand;
import br.edu.acad.ifma.app.usecase.notification.SendPushNotificationUseCase;
import jakarta.validation.Valid;

import java.io.Console;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final SendPushNotificationUseCase sendUseCase;
    private final GetNotificationHistoryUseCase historyUseCase;
    private final GetNotificationByIdUseCase getByIdUseCase;
    private final NotificationRepositoryPort notificationRepository;

    public NotificationController(
        SendPushNotificationUseCase sendUseCase,
        GetNotificationHistoryUseCase historyUseCase,
        GetNotificationByIdUseCase getByIdUseCase,
        NotificationRepositoryPort notificationRepository
    ) {
        this.sendUseCase = sendUseCase;
        this.historyUseCase = historyUseCase;
        this.getByIdUseCase = getByIdUseCase;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody SendNotificationRequest req) {
        SendPushNotificationCommand cmd = new SendPushNotificationCommand(req.getDeviceToken(), req.getTitle(), req.getBody());
        PushNotification result = sendUseCase.execute(cmd);
        return ResponseEntity.accepted().body(NotificationPresenter.toResponse(result));
    }

    @GetMapping
    public ResponseEntity<Page<NotificationSummaryResponse>> list(
        @RequestParam(required = false) NotificationStatus status,
        @RequestParam(required = false) String deviceToken,
        @RequestParam(required = false) Instant fromDate,
        @RequestParam(required = false) Instant toDate,
        Pageable pageable
    ) {
        NotificationFilter filter = new NotificationFilter();
        filter.setStatus(status);
        filter.setDeviceToken(deviceToken);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        NotificationHistoryQuery query = new NotificationHistoryQuery(pageable, filter);
        return ResponseEntity.ok(historyUseCase.execute(query).map(NotificationPresenter::toSummary));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDetailResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(NotificationPresenter.toDetail(getByIdUseCase.execute(id)));
    }

    @PostMapping("/internal/fcm/ack")
    public ResponseEntity<Void> ack(@RequestBody FcmAckRequest request) {
        logger.info("ACK received notificationId={} messageId={}", request.getNotificationId(), request.getMessageId());
        var notificationOpt = resolveNotification(request);
        notificationOpt.ifPresent(n -> {
            logger.info("Noification status: {}", n.getStatus());
            if (n.getStatus() != NotificationStatus.DELIVERED) {
                n.markDelivered();
                notificationRepository.save(n);
                logger.info("Notification {} marked as delivered", n.getId());
            }
        });
        return ResponseEntity.ok().build();
    }

    private java.util.Optional<PushNotification> resolveNotification(FcmAckRequest request) {
        if (request.getNotificationId() != null) {
            return notificationRepository.findById(request.getNotificationId());
        }
        if (request.getMessageId() != null) {
            return notificationRepository.findByFcmMessageId(request.getMessageId());
        }
        logger.warn("ACK received with no notificationId or messageId");
        return java.util.Optional.empty();
    }
}
