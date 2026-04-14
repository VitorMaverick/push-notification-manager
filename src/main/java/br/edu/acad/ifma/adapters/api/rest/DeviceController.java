package br.edu.acad.ifma.adapters.api.rest;

import br.edu.acad.ifma.adapters.api.rest.inbound.RegisterDeviceRequest;
import br.edu.acad.ifma.adapters.api.rest.outbound.DeviceResponse;
import br.edu.acad.ifma.adapters.api.rest.presenter.DevicePresenter;
import br.edu.acad.ifma.app.domain.device.Device;
import br.edu.acad.ifma.app.usecase.device.GetDeviceByTokenUseCase;
import br.edu.acad.ifma.app.usecase.device.ListDevicesUseCase;
import br.edu.acad.ifma.app.usecase.device.RegisterDeviceCommand;
import br.edu.acad.ifma.app.usecase.device.RegisterDeviceUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
@Validated
public class DeviceController {

    private final RegisterDeviceUseCase registerUseCase;
    private final ListDevicesUseCase listUseCase;
    private final GetDeviceByTokenUseCase getByTokenUseCase;

    public DeviceController(
        RegisterDeviceUseCase registerUseCase,
        ListDevicesUseCase listUseCase,
        GetDeviceByTokenUseCase getByTokenUseCase
    ) {
        this.registerUseCase = registerUseCase;
        this.listUseCase = listUseCase;
        this.getByTokenUseCase = getByTokenUseCase;
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> register(@Valid @RequestBody RegisterDeviceRequest req) {
        RegisterDeviceCommand cmd = new RegisterDeviceCommand(req.getFcmToken(), req.getPlatform(), req.getUserAgent());
        Device device = registerUseCase.execute(cmd);
        return ResponseEntity.created(URI.create("/api/v1/devices/" + device.getId())).body(DevicePresenter.toResponse(device));
    }

    @GetMapping
    public ResponseEntity<Page<DeviceResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(listUseCase.execute(pageable).map(DevicePresenter::toResponse));
    }

    @GetMapping("/{token}")
    public ResponseEntity<DeviceResponse> getByToken(@PathVariable String token) {
        return ResponseEntity.ok(DevicePresenter.toResponse(getByTokenUseCase.execute(token)));
    }
}
