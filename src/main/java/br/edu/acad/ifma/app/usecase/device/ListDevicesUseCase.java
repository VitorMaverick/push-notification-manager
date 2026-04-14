package br.edu.acad.ifma.app.usecase.device;

import br.edu.acad.ifma.app.domain.device.Device;
import br.edu.acad.ifma.app.port.DeviceRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListDevicesUseCase {

    private final DeviceRepositoryPort deviceRepository;

    public ListDevicesUseCase(DeviceRepositoryPort deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Page<Device> execute(Pageable pageable) {
        return deviceRepository.findAll(pageable);
    }
}
