package br.edu.acad.ifma.notification.adapter.rest;

import br.edu.acad.ifma.notification.domain.NotificationStatus;
import br.edu.acad.ifma.notification.domain.PushNotification;
import br.edu.acad.ifma.notification.usecase.GetNotificationByIdUseCase;
import br.edu.acad.ifma.notification.usecase.GetNotificationHistoryUseCase;
import br.edu.acad.ifma.notification.usecase.MarkNotificationDeliveredUseCase;
import br.edu.acad.ifma.notification.usecase.NotificationFilter;
import br.edu.acad.ifma.notification.usecase.SendPushNotificationCommand;
import br.edu.acad.ifma.notification.usecase.SendPushNotificationUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SendPushNotificationUseCase sendUseCase;
    private final GetNotificationByIdUseCase getByIdUseCase;
    private final GetNotificationHistoryUseCase historyUseCase;
    private final MarkNotificationDeliveredUseCase markDeliveredUseCase;

    public NotificationController(
            SendPushNotificationUseCase sendUseCase,
            GetNotificationByIdUseCase getByIdUseCase,
            GetNotificationHistoryUseCase historyUseCase,
            MarkNotificationDeliveredUseCase markDeliveredUseCase) {
        this.sendUseCase = sendUseCase;
        this.getByIdUseCase = getByIdUseCase;
        this.historyUseCase = historyUseCase;
        this.markDeliveredUseCase = markDeliveredUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse send(@Valid @RequestBody SendNotificationRequest request) {
        SendPushNotificationCommand command = new SendPushNotificationCommand(
                request.recipientToken(), request.title(), request.body(), request.data());
        return NotificationResponse.from(sendUseCase.execute(command));
    }

    @GetMapping("/{id}")
    public NotificationResponse getById(@PathVariable Long id) {
        return NotificationResponse.from(getByIdUseCase.execute(id));
    }

    @GetMapping
    public Page<NotificationSummaryResponse> list(
            Pageable pageable,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) String fcmToken) {
        NotificationFilter filter = new NotificationFilter(status, fcmToken);
        return historyUseCase.execute(pageable, filter).map(NotificationSummaryResponse::from);
    }

    @PostMapping("/ack")
    public NotificationResponse acknowledge(@Valid @RequestBody FcmAckRequest request) {
        PushNotification updated = markDeliveredUseCase.execute(request.fcmMessageId());
        return NotificationResponse.from(updated);
    }
}
