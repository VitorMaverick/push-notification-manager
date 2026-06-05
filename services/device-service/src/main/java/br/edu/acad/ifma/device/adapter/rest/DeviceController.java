package br.edu.acad.ifma.device.adapter.rest;

import br.edu.acad.ifma.device.domain.Device;
import br.edu.acad.ifma.device.usecase.GetDeviceByTokenUseCase;
import br.edu.acad.ifma.device.usecase.ListDevicesUseCase;
import br.edu.acad.ifma.device.usecase.RegisterDeviceCommand;
import br.edu.acad.ifma.device.usecase.RegisterDeviceUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
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
        Device device = registerUseCase.execute(
            new RegisterDeviceCommand(req.getFcmToken(), req.getPlatform(), req.getUserAgent())
        );
        return ResponseEntity
            .created(URI.create("/api/v1/devices/" + device.getFcmToken().value()))
            .body(toResponse(device));
    }

    @GetMapping
    public ResponseEntity<Page<DeviceResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(listUseCase.execute(pageable).map(this::toResponse));
    }

    @GetMapping("/{token}")
    public ResponseEntity<DeviceResponse> getByToken(@PathVariable String token) {
        return ResponseEntity.ok(toResponse(getByTokenUseCase.execute(token)));
    }

    private DeviceResponse toResponse(Device d) {
        return new DeviceResponse(
            d.getId(),
            d.getFcmToken().value(),
            d.getType(),
            d.getDeviceName(),
            d.getStatus(),
            d.getRegisteredAt(),
            d.getLastUsedAt()
        );
    }
}
