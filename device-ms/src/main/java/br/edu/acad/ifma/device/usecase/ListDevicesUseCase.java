package br.edu.acad.ifma.device.usecase;

import br.edu.acad.ifma.device.domain.Device;
import br.edu.acad.ifma.device.port.DeviceRepositoryPort;
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
