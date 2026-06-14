package br.senai.sc.communitex.service;

import br.senai.sc.communitex.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PracaGeometryService {

    private static final int MAX_VERTICES = 200;
    private static final double EARTH_RADIUS_METERS = 6_371_008.8;
    private static final double EPSILON = 1e-10;

    private final ObjectMapper objectMapper;

    public GeometryResult process(JsonNode polygon, Double latitude, Double longitude, Double metragemM2) {
        if (polygon == null || polygon.isNull()) {
            validatePoint(latitude, longitude, metragemM2);
            return new GeometryResult(latitude, longitude, metragemM2, null);
        }

        var vertices = readVertices(polygon);
        validateSimplePolygon(vertices);
        var center = centroid(vertices);
        var area = areaSquareMeters(vertices);

        try {
            return new GeometryResult(center.latitude(), center.longitude(), area, objectMapper.writeValueAsString(polygon));
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Nao foi possivel processar o poligono da praca", ex);
        }
    }

    public JsonNode readGeoJson(String geoJson) {
        if (geoJson == null || geoJson.isBlank()) return null;
        try {
            return objectMapper.readTree(geoJson);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Poligono armazenado em formato invalido", ex);
        }
    }

    private void validatePoint(Double latitude, Double longitude, Double metragemM2) {
        if (latitude == null || longitude == null) {
            throw new BusinessException("Informe um ponto ou desenhe a area da praca");
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new BusinessException("Latitude ou longitude fora dos limites permitidos");
        }
        if (metragemM2 == null || metragemM2 <= 0) {
            throw new BusinessException("A metragem deve ser informada para pracas marcadas por ponto");
        }
    }

    private List<Point> readVertices(JsonNode polygon) {
        if (!polygon.isObject() || !"Polygon".equals(polygon.path("type").asText())) {
            throw new BusinessException("O poligono deve ser um GeoJSON do tipo Polygon");
        }

        var coordinates = polygon.path("coordinates");
        if (!coordinates.isArray() || coordinates.size() != 1 || !coordinates.get(0).isArray()) {
            throw new BusinessException("O poligono deve possuir exatamente um contorno, sem areas internas");
        }

        var ring = coordinates.get(0);
        if (ring.size() < 4) {
            throw new BusinessException("O poligono deve possuir ao menos 3 vertices");
        }
        if (ring.size() - 1 > MAX_VERTICES) {
            throw new BusinessException("O poligono deve possuir no maximo " + MAX_VERTICES + " vertices");
        }

        var vertices = new ArrayList<Point>();
        for (JsonNode coordinate : ring) {
            if (!coordinate.isArray() || coordinate.size() != 2
                    || !coordinate.get(0).isNumber() || !coordinate.get(1).isNumber()) {
                throw new BusinessException("Cada vertice deve conter longitude e latitude numericas");
            }
            double longitude = coordinate.get(0).asDouble();
            double latitude = coordinate.get(1).asDouble();
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                throw new BusinessException("O poligono possui coordenadas fora dos limites permitidos");
            }
            vertices.add(new Point(latitude, longitude));
        }

        if (!samePoint(vertices.get(0), vertices.get(vertices.size() - 1))) {
            throw new BusinessException("O primeiro e o ultimo vertice do poligono devem ser iguais");
        }
        vertices.remove(vertices.size() - 1);
        if (vertices.stream().distinct().count() < 3) {
            throw new BusinessException("O poligono deve possuir ao menos 3 vertices distintos");
        }
        return vertices;
    }

    private void validateSimplePolygon(List<Point> vertices) {
        for (int i = 0; i < vertices.size(); i++) {
            var a1 = vertices.get(i);
            var a2 = vertices.get((i + 1) % vertices.size());
            if (samePoint(a1, a2)) {
                throw new BusinessException("O poligono nao pode possuir vertices consecutivos iguais");
            }
            for (int j = i + 1; j < vertices.size(); j++) {
                if (j == i || j == (i + 1) % vertices.size() || (j + 1) % vertices.size() == i) continue;
                var b1 = vertices.get(j);
                var b2 = vertices.get((j + 1) % vertices.size());
                if (segmentsIntersect(a1, a2, b1, b2)) {
                    throw new BusinessException("O poligono nao pode possuir auto-intersecoes");
                }
            }
        }
        if (Math.abs(signedArea(vertices)) < EPSILON) {
            throw new BusinessException("O poligono deve delimitar uma area valida");
        }
    }

    private Point centroid(List<Point> vertices) {
        var origin = vertices.get(0);
        double signedArea = 0;
        double latitude = 0;
        double longitude = 0;
        for (int i = 0; i < vertices.size(); i++) {
            var current = vertices.get(i);
            var next = vertices.get((i + 1) % vertices.size());
            double currentLongitude = current.longitude() - origin.longitude();
            double currentLatitude = current.latitude() - origin.latitude();
            double nextLongitude = next.longitude() - origin.longitude();
            double nextLatitude = next.latitude() - origin.latitude();
            double cross = currentLongitude * nextLatitude - nextLongitude * currentLatitude;
            signedArea += cross;
            longitude += (currentLongitude + nextLongitude) * cross;
            latitude += (currentLatitude + nextLatitude) * cross;
        }
        double factor = 1 / (3 * signedArea);
        return new Point(origin.latitude() + latitude * factor, origin.longitude() + longitude * factor);
    }

    private double areaSquareMeters(List<Point> vertices) {
        double meanLatitudeRadians = Math.toRadians(vertices.stream().mapToDouble(Point::latitude).average().orElse(0));
        double area = 0;
        for (int i = 0; i < vertices.size(); i++) {
            var current = vertices.get(i);
            var next = vertices.get((i + 1) % vertices.size());
            double currentX = EARTH_RADIUS_METERS * Math.toRadians(current.longitude()) * Math.cos(meanLatitudeRadians);
            double currentY = EARTH_RADIUS_METERS * Math.toRadians(current.latitude());
            double nextX = EARTH_RADIUS_METERS * Math.toRadians(next.longitude()) * Math.cos(meanLatitudeRadians);
            double nextY = EARTH_RADIUS_METERS * Math.toRadians(next.latitude());
            area += currentX * nextY - nextX * currentY;
        }
        return Math.abs(area) / 2;
    }

    private double signedArea(List<Point> vertices) {
        double area = 0;
        for (int i = 0; i < vertices.size(); i++) {
            var current = vertices.get(i);
            var next = vertices.get((i + 1) % vertices.size());
            area += current.longitude() * next.latitude() - next.longitude() * current.latitude();
        }
        return area / 2;
    }

    private boolean segmentsIntersect(Point a, Point b, Point c, Point d) {
        double o1 = orientation(a, b, c);
        double o2 = orientation(a, b, d);
        double o3 = orientation(c, d, a);
        double o4 = orientation(c, d, b);
        if (o1 * o2 < -EPSILON && o3 * o4 < -EPSILON) return true;
        return Math.abs(o1) < EPSILON && onSegment(a, c, b)
                || Math.abs(o2) < EPSILON && onSegment(a, d, b)
                || Math.abs(o3) < EPSILON && onSegment(c, a, d)
                || Math.abs(o4) < EPSILON && onSegment(c, b, d);
    }

    private double orientation(Point a, Point b, Point c) {
        return (b.longitude() - a.longitude()) * (c.latitude() - a.latitude())
                - (b.latitude() - a.latitude()) * (c.longitude() - a.longitude());
    }

    private boolean onSegment(Point a, Point point, Point b) {
        return point.longitude() >= Math.min(a.longitude(), b.longitude()) - EPSILON
                && point.longitude() <= Math.max(a.longitude(), b.longitude()) + EPSILON
                && point.latitude() >= Math.min(a.latitude(), b.latitude()) - EPSILON
                && point.latitude() <= Math.max(a.latitude(), b.latitude()) + EPSILON;
    }

    private boolean samePoint(Point a, Point b) {
        return Math.abs(a.latitude() - b.latitude()) < EPSILON
                && Math.abs(a.longitude() - b.longitude()) < EPSILON;
    }

    public record GeometryResult(Double latitude, Double longitude, Double metragemM2, String polygonGeoJson) {}

    private record Point(double latitude, double longitude) {}
}
