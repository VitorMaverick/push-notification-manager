package br.edu.acad.ifma.adapters.api.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.acad.ifma.app.domain.device.Device;
import br.edu.acad.ifma.app.domain.device.DeviceStatus;
import br.edu.acad.ifma.app.domain.device.DeviceType;
import br.edu.acad.ifma.app.domain.shared.FcmToken;
import br.edu.acad.ifma.app.domain.shared.exception.DuplicateDeviceTokenException;
import br.edu.acad.ifma.app.usecase.device.GetDeviceByTokenUseCase;
import br.edu.acad.ifma.app.usecase.device.ListDevicesUseCase;
import br.edu.acad.ifma.app.usecase.device.RegisterDeviceUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DeviceController.class)
@WithMockUser
class DeviceControllerTest {

    private static final String VALID_TOKEN = "abcdefghijklmnopqrstuvwxyz";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterDeviceUseCase registerUseCase;

    @MockBean
    private ListDevicesUseCase listUseCase;

    @MockBean
    private GetDeviceByTokenUseCase getByTokenUseCase;

    private Device buildDevice() {
        return Device.builder()
            .withId(1L)
            .withFcmToken(new FcmToken(VALID_TOKEN))
            .withDeviceName("Samsung")
            .withType(DeviceType.ANDROID)
            .withStatus(DeviceStatus.ACTIVE)
            .withRegisteredAt(Instant.now())
            .build();
    }

    @Test
    void post_returns_201() throws Exception {
        when(registerUseCase.execute(any())).thenReturn(buildDevice());
        String body = "{\"fcmToken\":\"" + VALID_TOKEN + "\",\"platform\":\"ANDROID\",\"userAgent\":\"Samsung\"}";
        mockMvc
            .perform(post("/api/v1/devices").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());
    }

    @Test
    void post_duplicate_returns_409() throws Exception {
        when(registerUseCase.execute(any())).thenThrow(new DuplicateDeviceTokenException("Token already registered"));
        String body = "{\"fcmToken\":\"" + VALID_TOKEN + "\"}";
        mockMvc
            .perform(post("/api/v1/devices").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict());
    }

    @Test
    void get_list_returns_200() throws Exception {
        when(listUseCase.execute(any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/devices")).andExpect(status().isOk());
    }
}
