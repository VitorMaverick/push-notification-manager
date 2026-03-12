package br.edu.acad.ifma.service.notification;

import br.edu.acad.ifma.domain.Cliente;
import br.edu.acad.ifma.domain.Notification;
import br.edu.acad.ifma.domain.NotificationChannel;
import br.edu.acad.ifma.domain.Segmento;
import br.edu.acad.ifma.repository.ClienteRepository;
import br.edu.acad.ifma.repository.NotificationMessageRepository;
import br.edu.acad.ifma.repository.SegmentoRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SegmentNotificationService {

    private final Logger log = LoggerFactory.getLogger(SegmentNotificationService.class);

    private final SegmentoRepository segmentoRepository;
    private final ClienteRepository clienteRepository;
    private final NotificationMessageRepository notificationRepository;
    private final NotificationService notificationService;

    public SegmentNotificationService(
        SegmentoRepository segmentoRepository,
        ClienteRepository clienteRepository,
        NotificationMessageRepository notificationRepository,
        NotificationService notificationService
    ) {
        this.segmentoRepository = segmentoRepository;
        this.clienteRepository = clienteRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public int enviarParaSegmento(String nomeSegmento, NotificationChannel canal, NotificationMessageTO mensagem) {
        Optional<Segmento> segmentoOpt = segmentoRepository.findByNome(nomeSegmento);
        if (segmentoOpt.isEmpty()) {
            log.warn("Segmento {} não encontrado", nomeSegmento);
            return 0;
        }
        Segmento segmento = segmentoOpt.get();
        List<Cliente> clientes = segmento.getClientes().stream().collect(Collectors.toList());
        int count = 0;
        for (Cliente c : clientes) {
            Notification nm = new Notification(mensagem.getTitulo(), mensagem.getCorpo(), canal);
            // Here you could set recipient info (e.g., token) from Cliente; for now we let NotificationService persist and notify observers
            notificationService.createAndSend(nm);
            count++;
        }
        log.info("Enviado para {} clientes do segmento {}", count, nomeSegmento);
        return count;
    }
}
