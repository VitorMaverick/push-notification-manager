package br.edu.acad.ifma.app.usecase.notification;

import org.springframework.data.domain.Pageable;

public class NotificationHistoryQuery {

    private final Pageable pageable;
    private final NotificationFilter filter;

    public NotificationHistoryQuery(Pageable pageable, NotificationFilter filter) {
        this.pageable = pageable;
        this.filter = filter;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public NotificationFilter getFilter() {
        return filter;
    }
}
