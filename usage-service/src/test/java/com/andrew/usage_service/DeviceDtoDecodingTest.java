package com.andrew.usage_service;

import com.andrew.usage_service.dto.DeviceDto;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * device-service never sends energyConsumed - that value only exists once
 * usage-service has aggregated it from Influx. Jackson 3 enables
 * FAIL_ON_NULL_FOR_PRIMITIVES by default, so an absent primitive is an error
 * rather than 0.0 like it was under Jackson 2.
 */
class DeviceDtoDecodingTest {

    // live response of GET http://localhost:8081/api/v1/device/user/8
    private static final String DEVICE_SERVICE_JSON = """
            [{"id":2,"name":"name_x8kak","type":"THERMOSTAT","location":"location_8vpur","userId":8}]
            """;

    @Test
    void decodesDeviceThatCarriesNoEnergyConsumed() {
        DeviceDto[] devices = JsonMapper.builder().build()
                .readValue(DEVICE_SERVICE_JSON, DeviceDto[].class);

        assertEquals(2L, devices[0].id());
    }
}
