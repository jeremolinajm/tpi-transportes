package com.transportes.logistics.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OsrmClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${osrm.service.url:http://localhost:5000}")
    private String osrmUrl;

    public RouteResponse calcularRuta(BigDecimal origenLat, BigDecimal origenLon,
                                      BigDecimal destinoLat, BigDecimal destinoLon) {
        try {
            // OSRM usa formato: lon,lat (invertido)
            String url = String.format("%s/route/v1/driving/%s,%s;%s,%s?overview=false",
                    osrmUrl,
                    origenLon, origenLat,
                    destinoLon, destinoLat);

            log.debug("Llamando a OSRM: {}", url);

            OsrmResponse response = restTemplate.getForObject(url, OsrmResponse.class);

            if (response != null && "Ok".equals(response.getCode()) && 
                !response.getRoutes().isEmpty()) {
                
                OsrmRoute route = response.getRoutes().get(0);
                
                return RouteResponse.builder()
                        .distanciaMetros(route.getDistance())
                        .distanciaKm(BigDecimal.valueOf(route.getDistance() / 1000.0))
                        .duracionSegundos(route.getDuration().intValue())
                        .duracionHoras((int) Math.ceil(route.getDuration() / 3600.0))
                        .build();
            } else {
                log.warn("OSRM no devolvió ruta válida");
                return calcularRutaFallback(origenLat, origenLon, destinoLat, destinoLon);
            }

        } catch (Exception e) {
            log.error("Error al consultar OSRM", e);
            return calcularRutaFallback(origenLat, origenLon, destinoLat, destinoLon);
        }
    }

    private RouteResponse calcularRutaFallback(BigDecimal lat1, BigDecimal lon1,
                                               BigDecimal lat2, BigDecimal lon2) {
        // Cálculo aproximado usando fórmula de Haversine
        double distanciaKm = calcularDistanciaHaversine(
                lat1.doubleValue(), lon1.doubleValue(),
                lat2.doubleValue(), lon2.doubleValue());

        // Estimación: 60 km/h promedio
        int duracionHoras = (int) Math.ceil(distanciaKm / 60.0);

        log.warn("Usando cálculo fallback de distancia: {} km", distanciaKm);

        return RouteResponse.builder()
                .distanciaKm(BigDecimal.valueOf(distanciaKm))
                .distanciaMetros(distanciaKm * 1000)
                .duracionHoras(duracionHoras)
                .duracionSegundos(duracionHoras * 3600)
                .build();
    }

    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Radio de la Tierra en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @Data
    public static class OsrmResponse {
        private String code;
        private List<OsrmRoute> routes;
    }

    @Data
    public static class OsrmRoute {
        private Double distance; // en metros
        private Double duration; // en segundos
    }

    @Data
    @lombok.Builder
    public static class RouteResponse {
        private BigDecimal distanciaKm;
        private Double distanciaMetros;
        private Integer duracionHoras;
        private Integer duracionSegundos;
    }
}
