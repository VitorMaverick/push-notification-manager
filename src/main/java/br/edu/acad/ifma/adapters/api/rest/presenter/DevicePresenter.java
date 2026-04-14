package br.edu.acad.ifma.adapters.api.rest.presenter;

import br.edu.acad.ifma.adapters.api.rest.outbound.DeviceResponse;
import br.edu.acad.ifma.app.domain.device.Device;

public abstract class DevicePresenter {

    private DevicePresenter() {}

    public static DeviceResponse toResponse(Device d) {
        DeviceResponse r = new DeviceResponse();
        r.setId(d.getId());
        r.setFcmToken(d.getFcmToken().value());
        r.setPlatform(d.getType() != null ? d.getType().name() : null);
        r.setUserAgent(d.getDeviceName());
        r.setRegisteredAt(d.getRegisteredAt());
        r.setLastUsedAt(d.getLastUsedAt());
        return r;
    }
}
