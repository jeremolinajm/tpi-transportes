# 🔧 TROUBLESHOOTING - Colección Postman TPI Transportes

## 📋 Índice
- [Problema 1: 6.1 "Obtener Costo Estimado" devuelve 404](#problema-1-61-obtener-costo-estimado-devuelve-404)
- [Problema 2: 6.2 "Calcular Costo Final" devuelve 404](#problema-2-62-calcular-costo-final-devuelve-404)
- [Problema 3: 7.2 "Ver Tramos Asignados" devuelve lista vacía](#problema-3-72-ver-tramos-asignados-devuelve-lista-vacía)
- [Problema 4: 7.3/7.4 "Iniciar/Finalizar Tramo" devuelve 403/500](#problema-4-7374-iniciarfinalizar-tramo-devuelve-403500)
- [Problema 5: Costos aparecen como null en Solicitud](#problema-5-costos-aparecen-como-null-en-solicitud)
- [Cómo leer los logs para diagnosticar](#cómo-leer-los-logs-para-diagnosticar)

---

## Problema 1: 6.1 "Obtener Costo Estimado" devuelve 404

### ❌ Error típico:
```json
{
  "status": 404,
  "message": "Costo estimado no encontrado para solicitud X"
}
```

### 🔍 Causa raíz:

El endpoint `GET /api/costos/solicitud/{id}/estimado` **NO calcula nada**. Solo **lee** un registro de la tabla `CostoSolicitud` que debe haber sido creado anteriormente.

El costo estimado se calcula en **5.2 "Asignar Ruta a Solicitud"**, dentro del método:
```
logistics-service → RutaService.calcularYAsignarCostoEstimado()
```

Este método llama a:
```
billingClient.calcularCostoEstimado(request) → billing-service
```

**⚠️ PROBLEMA DE DISEÑO (línea 513-515 de RutaService.java):**
```java
} catch (Exception e) {
    log.error("Error al calcular costo estimado para ruta {}: {}", ruta.getId(), e.getMessage());
    // NO RELANZA LA EXCEPCIÓN → La ruta se asigna igual pero sin costo
}
```

**Resultado:**
- Si la llamada a billing-service falla (servicio caído, timeout, error de red, etc.)
- El paso 5.2 devuelve **200 OK** (la ruta se asigna correctamente)
- Pero **nunca se crea** el registro `CostoSolicitud` tipo ESTIMADO
- Cuando ejecutás 6.1 → **404 Not Found**

### ✅ Soluciones:

#### Opción 1: Verificar logs de logistics-service
```bash
docker compose logs -f logistics-service | grep "Error al calcular costo estimado"
```

Si ves este error, billing-service está fallando.

#### Opción 2: Verificar que billing-service esté funcionando
```bash
docker compose ps
# billing-service debe estar "Up" y "healthy"

docker compose logs billing-service | grep -i error
```

#### Opción 3: Verificar conectividad entre microservicios
```bash
# Entrar al contenedor de logistics-service
docker compose exec logistics-service sh

# Intentar hacer ping/curl a billing-service
curl http://billing-service:8082/actuator/health
```

#### Opción 4: Verificar que existan tarifas vigentes
El cálculo de costo requiere tarifas activas:
- Ejecutar **1.2 Crear Tarifa Base**
- Ejecutar **1.3 Crear Tarifa Combustible**
- Ejecutar **1.4 Crear Tarifa Estadía**

Verificar que las fechas de vigencia incluyan la fecha actual.

#### Opción 5: Re-ejecutar el flujo completo
1. Reiniciar todos los servicios: `docker compose restart`
2. Ejecutar desde **1.1** hasta **5.2** sin saltear pasos
3. Después de ejecutar 5.2, verificar:
   ```bash
   docker compose logs billing-service | grep "Costo estimado calculado"
   ```
4. Si ves el log, el costo se guardó → ejecutar 6.1

---

## Problema 2: 6.2 "Calcular Costo Final" devuelve 404

### ❌ Error típico:
```json
{
  "status": 404,
  "message": "Costo final no encontrado para solicitud X"
}
```

### 🔍 Causa raíz:

Similar a 6.1, el endpoint `GET /api/costos/solicitud/{id}/final` **NO calcula**, solo **lee**.

El costo final se calcula en **7.4 "Finalizar Tramo"**, cuando se finaliza **EL ÚLTIMO TRAMO** de la ruta.

En `TramoService.verificarYFinalizarRuta()` (línea 164-165):
```java
boolean todosFinalizados = ruta.getTramos().stream()
    .allMatch(t -> t.getEstado() == Tramo.EstadoTramo.FINALIZADO);
```

Solo cuando **TODOS** los tramos están finalizados:
```java
if (todosFinalizados) {
    billingClient.calcularCostoReal(costoRequest);
    // ... actualiza solicitud a ENTREGADA
}
```

**⚠️ PROBLEMA DE DISEÑO (línea 212-214 de TramoService.java):**
```java
} catch (Exception e) {
    log.error("Error al verificar y finalizar ruta: {}", e.getMessage(), e);
    // NO RELANZA LA EXCEPCIÓN → El tramo se finaliza pero no se calcula costo
}
```

### ✅ Soluciones:

#### Verificar que TODOS los tramos estén finalizados:
```bash
# Consultar estado de tramos desde logistics-service
curl -H "Authorization: Bearer {{operador_token}}" \
     http://localhost:8080/api/rutas/{{ruta_id}}
```

Todos los tramos deben tener `"estado": "FINALIZADO"`.

#### Verificar que la solicitud esté en estado ENTREGADA:
```bash
curl -H "Authorization: Bearer {{cliente_token}}" \
     http://localhost:8080/api/solicitudes/{{solicitud_id}}/seguimiento
```

Si el estado NO es `ENTREGADA`, significa que `verificarYFinalizarRuta()` falló.

#### Verificar logs:
```bash
docker compose logs -f logistics-service | grep "Error al verificar y finalizar ruta"
docker compose logs -f billing-service | grep "Costo real calculado"
```

#### Si la ruta tiene múltiples tramos:
Asegurate de ejecutar **7.4 Finalizar Tramo** para **CADA** tramo, no solo uno.

El script de test en 7.2 captura el primer tramo ASIGNADO. Si tenés 3 tramos, necesitás:
1. Ejecutar 7.3 (iniciar primer tramo)
2. Ejecutar 7.4 (finalizar primer tramo)
3. Ejecutar 7.2 nuevamente → actualiza `tramo_id` al segundo tramo
4. Ejecutar 7.3 (iniciar segundo tramo)
5. Ejecutar 7.4 (finalizar segundo tramo)
6. Repetir para el tercer tramo

Recién cuando finalizás el último, se dispara el cálculo del costo real.

---

## Problema 3: 7.2 "Ver Tramos Asignados" devuelve lista vacía

### ❌ Resultado:
```json
[]
```

### 🔍 Causa raíz:

El query en `TramoRepository.findTramosAsignadosPorTransportista()` filtra por:
```sql
WHERE t.camion.transportista.keycloakUserId = :keycloakUserId
```

**Condiciones que deben cumplirse:**

1. **El transportista logueado en 7.1 debe ser el mismo creado en 2.1**
   - Variable `{{transportista_email}}` debe coincidir exactamente
   - El usuario debe existir en Keycloak con el mismo email

2. **El camión creado en 2.2 debe estar asignado a ese transportista**
   - Body de 2.2 debe incluir: `"transportistaId": "{{transportista_id}}"`
   - Este ID se guarda automáticamente en el test de 2.1

3. **El camión debe haber sido asignado al tramo en 5.3**
   - Request: `POST /api/tramos/{{tramo_id}}/asignar-camion?camionId={{camion_id}}`
   - Variables `{{tramo_id}}` y `{{camion_id}}` deben ser correctas

4. **El keycloakUserId del transportista en BD debe coincidir con el del JWT**
   - Al crear transportista (2.1), se guarda `keycloakUserId` en la tabla
   - Al hacer login (7.1), `authentication.getName()` devuelve el sub del JWT
   - Estos dos valores **DEBEN SER EXACTAMENTE IGUALES**

### ✅ Soluciones:

#### Verificar el transportista en BD:
```sql
SELECT id, email, keycloak_user_id FROM transportista
WHERE email = 'juan.trans@demo.com';
```

#### Verificar el token JWT:
Copiar el valor de `{{transportista_token}}` y decodificarlo en https://jwt.io
Buscar el claim `sub` → debe coincidir con `keycloak_user_id` de la tabla.

#### Verificar el camión:
```sql
SELECT id, dominio, transportista_id, estado
FROM camion
WHERE id = {{camion_id}};
```
El `transportista_id` debe coincidir con el ID del transportista logueado.

#### Verificar el tramo:
```sql
SELECT id, estado, camion_id
FROM tramo
WHERE id = {{tramo_id}};
```
- `camion_id` debe ser el camión del transportista
- `estado` debe ser `ASIGNADO`, `INICIADO` o `EN_TRANSITO`

#### Reiniciar flujo de autenticación:
Si Keycloak tiene problemas de sincronización:
```bash
# Eliminar el transportista de Keycloak manualmente
# Re-ejecutar 2.1 Crear Transportista
# Re-ejecutar 2.2 Crear Camión
# Re-ejecutar 5.3 Asignar Camión a Tramo
# Re-ejecutar 7.1 Login Transportista
# Ejecutar 7.2
```

---

## Problema 4: 7.3/7.4 "Iniciar/Finalizar Tramo" devuelve 403/500

### ❌ Errores típicos:

**403 Forbidden:**
```json
{
  "error": "Access Denied"
}
```

**500 Internal Server Error:**
```json
{
  "message": "El tramo no está asignado al transportista autenticado"
}
```
o
```json
{
  "message": "El tramo debe estar en estado ASIGNADO para ser iniciado"
}
```

### 🔍 Causa raíz:

En `TramoService.iniciarTramo()` y `finalizarTramo()` hay validaciones estrictas:

**Validación 1: Propiedad del camión (línea 67-70 de TramoService.java)**
```java
if (tramo.getCamion() == null ||
    tramo.getCamion().getTransportista() == null ||
    !tramo.getCamion().getTransportista().getKeycloakUserId().equals(keycloakUserId)) {
    throw new BusinessException("El tramo no está asignado al transportista autenticado");
}
```

**Validación 2: Estado del tramo**
- Para **7.3 Iniciar**: el tramo debe estar en estado `ASIGNADO`
- Para **7.4 Finalizar**: el tramo debe estar en estado `INICIADO`

### ✅ Soluciones:

#### Para 403 Forbidden:
Verificar que el token tenga el rol `TRANSPORTISTA`:
```bash
# Decodificar {{transportista_token}} en jwt.io
# Verificar que en "realm_access": { "roles": [...] }
# contenga "TRANSPORTISTA"
```

#### Para validación de propiedad:
- Ejecutar **7.2 Ver Tramos Asignados** primero
- Si devuelve lista vacía → ver [Problema 3](#problema-3-72-ver-tramos-asignados-devuelve-lista-vacía)
- Solo podés iniciar/finalizar tramos que aparezcan en tu lista

#### Para validación de estado:
Verificar el estado actual del tramo:
```bash
curl -H "Authorization: Bearer {{transportista_token}}" \
     http://localhost:8080/api/tramos/{{tramo_id}}
```

**Flujo correcto:**
1. Estado inicial: `ESTIMADO` (después de 5.2)
2. Después de 5.3: `ASIGNADO`
3. Después de 7.3: `INICIADO`
4. Después de 7.4: `FINALIZADO`

No podés saltear estados ni ejecutar dos veces el mismo endpoint.

---

## Problema 5: Costos aparecen como null en Solicitud

### ❌ Al consultar 4.4 "Consultar Estado/Seguimiento":
```json
{
  "id": 1,
  "estado": "ENTREGADA",
  "costoEstimado": null,
  "costoFinal": null,
  "tiempoEstimadoHoras": null,
  "tiempoRealHoras": null
}
```

### 🔍 Causa raíz:

**Los microservicios NO sincronizan datos automáticamente.**

- `billing-service` calcula y guarda en su tabla `CostoSolicitud`
- `logistics-service` guarda en `Ruta.costoEstimadoTotal` y `Ruta.costoRealTotal`
- `clients-service` tiene los campos `Solicitud.costoEstimado` y `Solicitud.costoFinal`

**Pero nadie actualiza los campos de `Solicitud`.**

### ✅ Soluciones:

#### Opción 1: Consultar costos desde billing-service
En lugar de usar 4.4, usar directamente:
- **6.1** para ver costo estimado
- **6.2** para ver costo final

Estos endpoints devuelven el cálculo completo con todos los detalles.

#### Opción 2: Consultar la ruta desde logistics-service
```bash
curl -H "Authorization: Bearer {{operador_token}}" \
     http://localhost:8080/api/rutas/{{ruta_id}}
```

La respuesta incluye:
```json
{
  "id": 1,
  "costoEstimadoTotal": 125000.50,
  "costoRealTotal": 132400.75,
  "tramos": [...]
}
```

#### Opción 3: Implementar sincronización (requiere cambios en backend)
Crear endpoint en `clients-service`:
```java
@PutMapping("/api/solicitudes/{id}/costos")
public void actualizarCostos(@PathVariable Long id, @RequestBody CostosDTO costos) {
    solicitud.setCostoEstimado(costos.costoEstimado);
    solicitud.setCostoFinal(costos.costoFinal);
    solicitud.setTiempoEstimadoHoras(costos.tiempoEstimadoHoras);
    solicitud.setTiempoRealHoras(costos.tiempoRealHoras);
}
```

Y llamarlo desde `logistics-service` después de calcular cada costo.

---

## Cómo leer los logs para diagnosticar

### Ver todos los logs en tiempo real:
```bash
docker compose logs -f
```

### Ver logs de un servicio específico:
```bash
docker compose logs -f logistics-service
docker compose logs -f billing-service
docker compose logs -f clients-service
docker compose logs -f gateway-service
```

### Buscar errores específicos:
```bash
# Errores en cálculo de costo estimado
docker compose logs logistics-service | grep "Error al calcular costo estimado"

# Errores en cálculo de costo real
docker compose logs logistics-service | grep "Error al verificar y finalizar ruta"

# Verificar que billing recibió el request
docker compose logs billing-service | grep "Costo estimado calculado"
docker compose logs billing-service | grep "Costo real calculado"

# Errores de conectividad entre microservicios
docker compose logs | grep -i "connection refused"
docker compose logs | grep -i "timeout"

# Errores de autenticación
docker compose logs | grep -i "unauthorized"
docker compose logs | grep -i "access denied"
```

### Ver estado de los servicios:
```bash
docker compose ps
```

Todos deben estar en estado `Up` y `healthy`.

### Reiniciar servicios problemáticos:
```bash
# Reiniciar billing-service si está fallando
docker compose restart billing-service

# Reiniciar todos
docker compose restart

# Rebuild si cambiaste código
docker compose up -d --build
```

---

## Checklist de verificación antes de ejecutar la colección

- [ ] Todos los servicios están UP: `docker compose ps`
- [ ] Keycloak está funcionando: http://localhost:8180
- [ ] Gateway está funcionando: http://localhost:8080/actuator/health
- [ ] No hay errores en logs: `docker compose logs | grep -i error`
- [ ] Variables de colección vacías (para empezar desde cero):
  - `operador_token`: ""
  - `cliente_token`: ""
  - `transportista_token`: ""
  - `solicitud_id`: ""
  - `tramo_id`: ""
  - etc.
- [ ] Email de cliente es único (cambiar si re-ejecutas): `maria.cliente2@demo.com`
- [ ] Tarifas están creadas y vigentes

---

## Resumen: ¿Por qué fallan los tests?

### 6.1 y 6.2 (Costos):
✅ **Los endpoints están bien.**
❌ **El problema:** Las excepciones se tragan silenciosamente en el backend.
🔧 **Solución:** Verificar logs y asegurar que billing-service funcione correctamente.

### 7.2, 7.3, 7.4 (Transportista):
✅ **Los endpoints están bien.**
❌ **El problema:** Validaciones estrictas de keycloakUserId y estados de tramos.
🔧 **Solución:** Asegurar que el flujo completo 2.1 → 2.2 → 5.3 → 7.1 se ejecute correctamente.

### Costos null en Solicitud:
✅ **Es comportamiento esperado por diseño.**
❌ **No es un error, es que no hay sincronización entre microservicios.**
🔧 **Solución:** Consultar costos desde 6.1/6.2 en lugar de 4.4.

---

**🎯 Si seguís teniendo problemas después de aplicar estas soluciones, pegá los logs completos del error y te ayudamos a diagnosticar.**
