package br.senai.sc.communitex.service;

import br.senai.sc.communitex.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PracaGeometryServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PracaGeometryService service = new PracaGeometryService(objectMapper);

    @Test
    void givenPoint_whenProcess_thenKeepsPointAndArea() {
        var result = service.process(null, -27.6, -48.5, 1200.0);

        assertThat(result.latitude()).isEqualTo(-27.6);
        assertThat(result.longitude()).isEqualTo(-48.5);
        assertThat(result.metragemM2()).isEqualTo(1200.0);
        assertThat(result.polygonGeoJson()).isNull();
    }

    @Test
    void givenPolygon_whenProcess_thenCalculatesCenterAndArea() throws Exception {
        var polygon = objectMapper.readTree("""
                {"type":"Polygon","coordinates":[[
                  [-48.5000,-27.6000],[-48.4990,-27.6000],
                  [-48.4990,-27.5990],[-48.5000,-27.5990],
                  [-48.5000,-27.6000]
                ]]}
                """);

        var result = service.process(polygon, null, null, null);

        assertThat(result.latitude()).isCloseTo(-27.5995, org.assertj.core.data.Offset.offset(0.000001));
        assertThat(result.longitude()).isCloseTo(-48.4995, org.assertj.core.data.Offset.offset(0.000001));
        assertThat(result.metragemM2()).isGreaterThan(9_000);
        assertThat(result.polygonGeoJson()).contains("\"Polygon\"");
    }

    @Test
    void givenSelfIntersectingPolygon_whenProcess_thenRejects() throws Exception {
        var polygon = objectMapper.readTree("""
                {"type":"Polygon","coordinates":[[
                  [-48.50,-27.60],[-48.49,-27.59],
                  [-48.50,-27.59],[-48.49,-27.60],
                  [-48.50,-27.60]
                ]]}
                """);

        assertThrows(BusinessException.class, () -> service.process(polygon, null, null, null));
    }

    @Test
    void givenOutOfBoundsPolygon_whenProcess_thenRejects() throws Exception {
        var polygon = objectMapper.readTree("""
                {"type":"Polygon","coordinates":[[
                  [-48.50,-91],[-48.49,-27.59],
                  [-48.50,-27.59],[-48.50,-91]
                ]]}
                """);

        assertThrows(BusinessException.class, () -> service.process(polygon, null, null, null));
    }
}
