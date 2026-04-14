package br.edu.acad.ifma.app.usecase.notification;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.edu.acad.ifma.app.domain.shared.exception.DomainException;
import br.edu.acad.ifma.app.port.NotificationRepositoryPort;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class GetNotificationHistoryUseCaseTest {

    private NotificationRepositoryPort notificationRepository;
    private GetNotificationHistoryUseCase useCase;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepositoryPort.class);
        useCase = new GetNotificationHistoryUseCase(notificationRepository);
        when(notificationRepository.findAll(any(), any())).thenReturn(Page.empty());
    }

    @Test
    void page_size_over_500_throws() {
        NotificationFilter filter = new NotificationFilter();
        NotificationHistoryQuery query = new NotificationHistoryQuery(PageRequest.of(0, 501), filter);

        assertThatThrownBy(() -> useCase.execute(query))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("500");
    }

    @Test
    void from_date_after_to_date_throws() {
        NotificationFilter filter = new NotificationFilter();
        filter.setFromDate(Instant.parse("2024-01-10T00:00:00Z"));
        filter.setToDate(Instant.parse("2024-01-01T00:00:00Z"));
        NotificationHistoryQuery query = new NotificationHistoryQuery(PageRequest.of(0, 20), filter);

        assertThatThrownBy(() -> useCase.execute(query))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("fromDate");
    }
}
