package br.edu.acad.ifma.device.port;

import br.edu.acad.ifma.device.domain.Device;

public interface DeviceEventPublisherPort {
    void publishDeviceRegistered(Device device);
}
