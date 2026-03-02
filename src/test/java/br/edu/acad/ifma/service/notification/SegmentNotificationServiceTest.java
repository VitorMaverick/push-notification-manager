package br.edu.acad.ifma.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.edu.acad.ifma.domain.Cliente;
import br.edu.acad.ifma.domain.NotificationChannel;
import br.edu.acad.ifma.domain.NotificationMessage;
import br.edu.acad.ifma.domain.Segmento;
import br.edu.acad.ifma.repository.ClienteRepository;
import br.edu.acad.ifma.repository.NotificationMessageRepository;
import br.edu.acad.ifma.repository.SegmentoRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SegmentNotificationServiceTest {

    private SegmentoRepository segmentoRepository;
    private ClienteRepository clienteRepository;
    private NotificationMessageRepository notificationRepository;
    private NotificationService notificationService;
    private SegmentNotificationService service;

    @BeforeEach
    void setUp() {
        segmentoRepository = Mockito.mock(SegmentoRepository.class);
        clienteRepository = Mockito.mock(ClienteRepository.class);
        notificationRepository = Mockito.mock(NotificationMessageRepository.class);
        notificationService = Mockito.mock(NotificationService.class);

        service = new SegmentNotificationService(segmentoRepository, clienteRepository, notificationRepository, notificationService);
    }

    @Test
    void testEnviarParaSegmento_notFound() {
        when(segmentoRepository.findByNome(any())).thenReturn(Optional.empty());
        int sent = service.enviarParaSegmento("nope", NotificationChannel.EMAIL, MensagemEnviada.builder().titulo("T").corpo("C").build());
        assertThat(sent).isEqualTo(0);
    }

    @Test
    void testEnviarParaSegmento_found() {
        Segmento s = new Segmento("vip");
        Cliente c1 = new Cliente("A");
        Cliente c2 = new Cliente("B");
        s.setClientes(Set.of(c1, c2));
        when(segmentoRepository.findByNome("vip")).thenReturn(Optional.of(s));
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(notificationService.createAndSend(any())).thenAnswer(i -> i.getArgument(0));

        int sent = service.enviarParaSegmento("vip", NotificationChannel.SMS, MensagemEnviada.builder().titulo("T").corpo("C").build());
        assertThat(sent).isEqualTo(2);
        verify(notificationService, times(2)).createAndSend(any(NotificationMessage.class));
    }
}
