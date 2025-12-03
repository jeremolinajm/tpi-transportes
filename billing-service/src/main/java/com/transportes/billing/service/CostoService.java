package com.transportes.billing.service;

import com.transportes.billing.entity.*;
import com.transportes.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CostoService {

    private final TarifaBaseRepository tarifaBaseRepository;
    private final TarifaCombustibleRepository tarifaCombustibleRepository;
    private final TarifaEstadiaRepository tarifaEstadiaRepository;
    private final TarifaPesoVolumenRepository tarifaPesoVolumenRepository;
    private final CostoSolicitudRepository costoSolicitudRepository;
    private final CostoTramoRepository costoTramoRepository;

    /**
     * Calcula el costo estimado de una solicitud y lo PERSISTE en la base de datos
     */
    @Transactional
    public CostoSolicitud calcularCostoEstimado(
            Long solicitudId,
            List<TramoInfo> tramos,
            BigDecimal pesoTotalKg,
            BigDecimal volumenTotalM3,
            Integer diasEstadiaEstimados) {

        log.info("Calculando costo ESTIMADO para solicitud {}", solicitudId);

        // Obtener tarifas vigentes
        TarifaBase tarifaBase = obtenerTarifaBaseVigente();
        TarifaCombustible tarifaCombustible = obtenerTarifaCombustibleVigente();
        TarifaEstadia tarifaEstadia = obtenerTarifaEstadiaVigente();

        // Obtener multiplicador por peso y volumen
        BigDecimal multiplicador = obtenerMultiplicadorPesoVolumen(pesoTotalKg, volumenTotalM3);

        // Crear entidad CostoSolicitud
        CostoSolicitud costoSolicitud = CostoSolicitud.builder()
                .solicitudId(solicitudId)
                .tipo(CostoSolicitud.TipoCosto.ESTIMADO)
                .tarifaBase(tarifaBase)
                .tarifaCombustible(tarifaCombustible)
                .tarifaEstadia(tarifaEstadia)
                .build();

        // Calcular costo de gestión
        BigDecimal costoGestion = tarifaBase.getCostoFijoGestion();

        // Calcular costos de tramos
        BigDecimal costoTransporte = BigDecimal.ZERO;
        BigDecimal costoCombustible = BigDecimal.ZERO;

        for (TramoInfo tramoInfo : tramos) {
            CostoTramo costoTramo = calcularCostoTramo(
                    tramoInfo,
                    tarifaBase,
                    tarifaCombustible,
                    multiplicador,
                    CostoTramo.TipoCostoTramo.ESTIMADO
            );

            costoSolicitud.agregarCostoTramo(costoTramo);
            costoTransporte = costoTransporte.add(costoTramo.getCostoPorKm().multiply(costoTramo.getDistanciaKm()));
            costoCombustible = costoCombustible.add(costoTramo.getCostoCombustible());
        }

        // Calcular costo de estadía
        BigDecimal costoEstadia = calcularCostoEstadia(diasEstadiaEstimados, tarifaEstadia);

        // Asignar costos desglosados
        costoSolicitud.setCostoGestion(costoGestion);
        costoSolicitud.setCostoTransporte(costoTransporte);
        costoSolicitud.setCostoCombustible(costoCombustible);
        costoSolicitud.setCostoEstadia(costoEstadia);
        costoSolicitud.setCostoAdicionales(BigDecimal.ZERO);

        // Calcular costo total
        BigDecimal costoTotal = costoGestion
                .add(costoTransporte)
                .add(costoCombustible)
                .add(costoEstadia)
                .setScale(2, RoundingMode.HALF_UP);

        costoSolicitud.setCostoTotal(costoTotal);

        // PERSISTIR en base de datos
        CostoSolicitud costoGuardado = costoSolicitudRepository.save(costoSolicitud);

        log.info("Costo ESTIMADO guardado con ID: {} para solicitud {}", costoGuardado.getId(), solicitudId);

        return costoGuardado;
    }

    /**
     * Calcula el costo REAL/FINAL de una solicitud basado en datos reales y lo PERSISTE
     */
    @Transactional
    public CostoSolicitud calcularCostoReal(
            Long solicitudId,
            List<TramoInfo> tramosReales,
            BigDecimal pesoRealKg,
            BigDecimal volumenRealM3,
            BigDecimal horasEstadiaTotales,
            BigDecimal costosAdicionales) {

        log.info("Calculando costo REAL/FINAL para solicitud {}", solicitudId);

        // Obtener tarifas vigentes
        TarifaBase tarifaBase = obtenerTarifaBaseVigente();
        TarifaCombustible tarifaCombustible = obtenerTarifaCombustibleVigente();
        TarifaEstadia tarifaEstadia = obtenerTarifaEstadiaVigente();

        // Obtener multiplicador por peso y volumen
        BigDecimal multiplicador = obtenerMultiplicadorPesoVolumen(pesoRealKg, volumenRealM3);

        // Crear entidad CostoSolicitud FINAL
        CostoSolicitud costoSolicitud = CostoSolicitud.builder()
                .solicitudId(solicitudId)
                .tipo(CostoSolicitud.TipoCosto.FINAL)
                .tarifaBase(tarifaBase)
                .tarifaCombustible(tarifaCombustible)
                .tarifaEstadia(tarifaEstadia)
                .build();

        // Calcular costo de gestión
        BigDecimal costoGestion = tarifaBase.getCostoFijoGestion();

        // Calcular costos de tramos REALES
        BigDecimal costoTransporte = BigDecimal.ZERO;
        BigDecimal costoCombustible = BigDecimal.ZERO;

        for (TramoInfo tramoInfo : tramosReales) {
            CostoTramo costoTramo = calcularCostoTramo(
                    tramoInfo,
                    tarifaBase,
                    tarifaCombustible,
                    multiplicador,
                    CostoTramo.TipoCostoTramo.REAL
            );

            costoSolicitud.agregarCostoTramo(costoTramo);
            costoTransporte = costoTransporte.add(costoTramo.getCostoPorKm().multiply(costoTramo.getDistanciaKm()));
            costoCombustible = costoCombustible.add(costoTramo.getCostoCombustible());
        }

        // Calcular costo de estadía REAL basado en horas
        BigDecimal costoEstadia = BigDecimal.ZERO;
        if (horasEstadiaTotales != null && horasEstadiaTotales.compareTo(BigDecimal.ZERO) > 0) {
            costoEstadia = tarifaEstadia.getCostoPorHora()
                    .multiply(horasEstadiaTotales)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Asignar costos desglosados
        costoSolicitud.setCostoGestion(costoGestion);
        costoSolicitud.setCostoTransporte(costoTransporte);
        costoSolicitud.setCostoCombustible(costoCombustible);
        costoSolicitud.setCostoEstadia(costoEstadia);
        costoSolicitud.setCostoAdicionales(costosAdicionales != null ? costosAdicionales : BigDecimal.ZERO);

        // Calcular costo total
        BigDecimal costoTotal = costoGestion
                .add(costoTransporte)
                .add(costoCombustible)
                .add(costoEstadia)
                .add(costoSolicitud.getCostoAdicionales())
                .setScale(2, RoundingMode.HALF_UP);

        costoSolicitud.setCostoTotal(costoTotal);

        // PERSISTIR en base de datos
        CostoSolicitud costoGuardado = costoSolicitudRepository.save(costoSolicitud);

        log.info("Costo FINAL guardado con ID: {} para solicitud {}", costoGuardado.getId(), solicitudId);

        return costoGuardado;
    }

    /**
     * Obtiene el costo estimado de una solicitud
     */
    public CostoSolicitud obtenerCostoEstimado(Long solicitudId) {
        return costoSolicitudRepository.findBySolicitudIdAndTipo(solicitudId, CostoSolicitud.TipoCosto.ESTIMADO)
                .orElseThrow(() -> new RuntimeException("No se encontró costo estimado para la solicitud " + solicitudId));
    }

    /**
     * Obtiene el costo final de una solicitud
     */
    public CostoSolicitud obtenerCostoFinal(Long solicitudId) {
        return costoSolicitudRepository.findBySolicitudIdAndTipo(solicitudId, CostoSolicitud.TipoCosto.FINAL)
                .orElseThrow(() -> new RuntimeException("No se encontró costo final para la solicitud " + solicitudId));
    }

    /**
     * Obtiene todos los costos de una solicitud
     */
    public List<CostoSolicitud> obtenerCostosSolicitud(Long solicitudId) {
        return costoSolicitudRepository.findBySolicitudIdOrderByFechaCalculoDesc(solicitudId);
    }

    // ============= METODOS PRIVADOS AUXILIARES =============

    private CostoTramo calcularCostoTramo(
            TramoInfo tramoInfo,
            TarifaBase tarifaBase,
            TarifaCombustible tarifaCombustible,
            BigDecimal multiplicador,
            CostoTramo.TipoCostoTramo tipo) {

        // Calcular costo por km del tramo (con multiplicador)
        BigDecimal costoPorKm = tramoInfo.getCostoBasePorKm().multiply(multiplicador);

        // Calcular costo de combustible del tramo
        BigDecimal litrosNecesarios = tramoInfo.getDistanciaKm()
                .divide(tramoInfo.getConsumoKmLitro(), 2, RoundingMode.HALF_UP);
        BigDecimal costoCombustible = litrosNecesarios.multiply(tarifaCombustible.getPrecioPorLitro());

        // Calcular costo adicional por tramo
        BigDecimal costoAdicionalTramo = tarifaBase.getCostoAdicionalPorTramo() != null
                ? tarifaBase.getCostoAdicionalPorTramo()
                : BigDecimal.ZERO;

        // Calcular costo total del tramo
        BigDecimal costoTotalTramo = costoPorKm.multiply(tramoInfo.getDistanciaKm())
                .add(costoCombustible)
                .add(costoAdicionalTramo)
                .setScale(2, RoundingMode.HALF_UP);

        return CostoTramo.builder()
                .tramoId(tramoInfo.getTramoId())
                .tipo(tipo)
                .distanciaKm(tramoInfo.getDistanciaKm())
                .costoPorKm(costoPorKm)
                .costoCombustible(costoCombustible)
                .costoEstadia(BigDecimal.ZERO)
                .horasEstadia(BigDecimal.ZERO)
                .costoTotalTramo(costoTotalTramo)
                .build();
    }

    private BigDecimal calcularCostoEstadia(Integer diasEstimados, TarifaEstadia tarifaEstadia) {
        if (diasEstimados == null || diasEstimados <= 0) {
            return BigDecimal.ZERO;
        }

        return tarifaEstadia.getCostoPorDia()
                .multiply(BigDecimal.valueOf(diasEstimados))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal obtenerMultiplicadorPesoVolumen(BigDecimal peso, BigDecimal volumen) {
        if (peso == null || volumen == null) {
            return BigDecimal.ONE;
        }

        return tarifaPesoVolumenRepository.findByPesoYVolumen(peso, volumen)
                .map(TarifaPesoVolumen::getMultiplicadorCosto)
                .orElse(BigDecimal.ONE);
    }

    private TarifaBase obtenerTarifaBaseVigente() {
        return tarifaBaseRepository.findFirstByActivaTrueOrderByFechaVigenciaDesdeDesc()
                .orElseThrow(() -> new RuntimeException("No hay tarifa base vigente"));
    }

    private TarifaCombustible obtenerTarifaCombustibleVigente() {
        return tarifaCombustibleRepository.findFirstByActivaTrueOrderByFechaVigenciaDesdeDesc()
                .orElseThrow(() -> new RuntimeException("No hay tarifa de combustible vigente"));
    }

    private TarifaEstadia obtenerTarifaEstadiaVigente() {
        return tarifaEstadiaRepository.findFirstByActivaTrueOrderByFechaVigenciaDesdeDesc()
                .orElseThrow(() -> new RuntimeException("No hay tarifa de estadía vigente"));
    }

    // ============= CLASE AUXILIAR =============

    /**
     * Información de un tramo para calcular su costo
     */
    public static class TramoInfo {
        private Long tramoId;
        private BigDecimal distanciaKm;
        private BigDecimal costoBasePorKm;
        private BigDecimal consumoKmLitro;

        public TramoInfo(Long tramoId, BigDecimal distanciaKm, BigDecimal costoBasePorKm, BigDecimal consumoKmLitro) {
            this.tramoId = tramoId;
            this.distanciaKm = distanciaKm;
            this.costoBasePorKm = costoBasePorKm;
            this.consumoKmLitro = consumoKmLitro;
        }

        public Long getTramoId() {
            return tramoId;
        }

        public BigDecimal getDistanciaKm() {
            return distanciaKm;
        }

        public BigDecimal getCostoBasePorKm() {
            return costoBasePorKm;
        }

        public BigDecimal getConsumoKmLitro() {
            return consumoKmLitro;
        }
    }
}
