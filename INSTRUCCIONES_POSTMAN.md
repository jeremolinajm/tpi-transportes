# Instrucciones - Colección Postman TPI Transportes

## 🚨 IMPORTANTE - Leer Antes de Empezar

⚠️ **Si los requests 6.x (Costos) o 7.x (Transportista) están fallando**, consultá el archivo:
📖 **`TROUBLESHOOTING_POSTMAN.md`**

Contiene diagnóstico detallado de:
- Por qué 6.1 y 6.2 devuelven 404 (costos no calculados)
- Por qué 7.2 devuelve lista vacía (problema de keycloakUserId)
- Por qué 7.3/7.4 devuelven 403/500 (validaciones de estado y autenticación)
- Cómo leer los logs para identificar el problema exacto

---

## 📥 Importar la Colección

1. Abrir Postman
2. Click en **Import**
3. Seleccionar el archivo `TPI_Transportes_Postman_Collection.json`
4. La colección aparecerá en el panel izquierdo

## 🚀 Orden de Ejecución (Caminito Feliz)

### PASO 1: Autenticación de Operador
Ejecutar **carpeta 1** en orden:
1. **1.1 Login Operador** → Guarda el token automáticamente

### PASO 2: Configuración Inicial (Operador)
Ejecutar **carpetas 1-3** en orden:

#### Tarifas (Carpeta 1):
2. **1.2 Crear Tarifa Base**
3. **1.3 Crear Tarifa Combustible**
4. **1.4 Crear Tarifa Estadía**
5. **1.5 Listar Tarifas Base** (opcional)
6. **1.6 Obtener Tarifa Base Vigente** (opcional)

#### Transportistas y Camiones (Carpeta 2):
7. **2.1 Crear Transportista** → Guarda ID y email del transportista
8. **2.2 Crear Camión** → Guarda ID del camión (usa el transportista_id automáticamente)
9. **2.3 Listar Camiones** (opcional)
10. **2.4 Obtener Camiones Disponibles** (opcional) → Filtra por peso y volumen
11. **2.5 Actualizar Camión** (opcional) → Permite cambiar transportista asignado
12. **2.6 Listar Transportistas** (⚠️ requiere implementación en backend)

#### Depósitos (Carpeta 3):
11. **3.1 Crear Depósito** (opcional - ya hay depósitos pre-cargados)
12. **3.2 Actualizar Depósito** (opcional)

### PASO 3: Cliente Crea Solicitud
Ejecutar **carpeta 4** en orden:
1. **4.1 Crear Solicitud (sin autenticación)** → Guarda solicitud_id y cliente_email
   - ✅ Verifica que el estado sea **BORRADOR**
2. **4.2 Login Cliente** → Guarda token del cliente
3. **4.3 Consultar Mis Solicitudes**
4. **4.4 Consultar Estado/Seguimiento**

### PASO 4: Operador Asigna Ruta
Ejecutar **carpeta 5** en orden:
1. **5.1 Generar Rutas Alternativas**
   - Devuelve 3 rutas:
     - índice 0: ruta directa (sin depósitos)
     - índice 1: ruta con 1 depósito
     - índice 2: ruta con 2 depósitos
   - ⚠️ Las rutas NO tienen ID (son temporales)
   - ✅ Los tramos NO tienen ID aún

2. **5.2 Asignar Ruta (índice 0 - directa)** → Guarda ruta_id y tramo_id
   - ✅ Ahora la ruta tiene ID (fue guardada)
   - ✅ Los tramos tienen IDs
   - ✅ Estado de solicitud cambia a **PROGRAMADA** automáticamente

3. **5.3 Asignar Camión a Tramo**
   - Usa el tramo_id y camion_id guardados automáticamente
   - ✅ Estado del tramo cambia a **ASIGNADO**
   - ✅ Estado del camión cambia a **OCUPADO**

### PASO 5: Operador Consulta Costos
Ejecutar **carpeta 6**:
1. **6.1 Obtener Costo Estimado**
2. **6.2 Calcular Costo Final** (después de finalizar tramos)

### PASO 6: Transportista Ejecuta el Viaje
Ejecutar **carpeta 7** en orden:
1. **7.1 Login Transportista** → Guarda token
2. **7.2 Ver Tramos Asignados** → Muestra tramos asignados al transportista + actualiza tramo_id automáticamente
3. **7.3 Iniciar Tramo**
   - ✅ Estado del tramo cambia a **INICIADO**
   - ✅ Registra fecha/hora de inicio real
   - ✅ Si es el primer tramo, solicitud cambia a **EN_TRANSITO**

4. **7.4 Finalizar Tramo**
   - ✅ Estado del tramo cambia a **FINALIZADO**
   - ✅ Registra fecha/hora de fin real
   - ✅ Si es el último tramo:
     - Calcula costo real
     - Solicitud cambia a **ENTREGADA**
     - Libera el camión (DISPONIBLE)

## 📊 Variables de Colección

Las siguientes variables se guardan automáticamente mediante los scripts de test:

| Variable | Se Guarda En | Se Usa En |
|----------|--------------|-----------|
| `operador_token` | Login Operador | Todos los endpoints de operador |
| `cliente_token` | Login Cliente | Endpoints de cliente |
| `transportista_token` | Login Transportista | Endpoints de transportista |
| `solicitud_id` | Crear Solicitud | Generar rutas, asignar ruta, costos |
| `cliente_email` | Crear Solicitud | Login Cliente |
| `transportista_id` | Crear Transportista | Crear Camión |
| `transportista_email` | Crear Transportista | Login Transportista |
| `camion_id` | Crear Camión | Asignar Camión a Tramo |
| `tramo_id` | Asignar Ruta | Iniciar/Finalizar Tramo |
| `ruta_id` | Asignar Ruta | (Referencia) |

## ⚠️ Notas Importantes

### Estados de Solicitud (Cambios Automáticos)
- `BORRADOR` → Al crear la solicitud
- `PROGRAMADA` → Al asignar una ruta (automático)
- `EN_TRANSITO` → Al iniciar el primer tramo (automático)
- `ENTREGADA` → Al finalizar el último tramo (automático)

### Rutas Alternativas
- Las rutas generadas NO tienen ID (son temporales, no se guardan en BD)
- Los tramos de rutas alternativas NO tienen ID
- Al asignar una ruta, se regenera y se guarda con IDs

### Datos Pre-cargados
El sistema tiene datos pre-cargados:
- ✅ 3 Depósitos activos
- ✅ 3 Camiones
- ✅ 3 Transportistas
- ✅ Tarifas base

Puedes usar los datos pre-cargados o crear nuevos con la colección.

### Usuario Operador
El usuario operador debe existir en Keycloak:
- **Username**: `operador.demo@mail.com`
- **Password**: `Demo123!`
- **Rol**: OPERADOR

Si no existe, ejecutar el script:
```bash
./test_endpoints_fixed.sh
```
El script incluye la creación del operador en Keycloak.

## 🔄 Reiniciar el Flujo

Para probar nuevamente desde cero:
1. Cambiar el email en **4.1 Crear Solicitud** (ej: `maria.cliente2@demo.com`)
2. Ejecutar desde el **PASO 3** en adelante
3. El sistema creará un nuevo cliente y solicitud

## ✅ Verificaciones en Cada Paso

### Después de Crear Solicitud (4.1):
```json
{
  "estado": "BORRADOR",  // ✅ Debe ser BORRADOR
  "id": 1,
  "numeroSolicitud": "SOL-2025-XXXXX"
}
```

### Después de Asignar Ruta (5.2):
```json
{
  "id": 1,  // ✅ La ruta ahora tiene ID
  "tramos": [
    {
      "id": 1,  // ✅ Los tramos tienen IDs
      "estado": "ESTIMADO"
    }
  ]
}
```

### Después de Asignar Camión (5.3):
- Verificar en **7.2 Ver Tramos Asignados** que el transportista ve el tramo

### Después de Iniciar Tramo (7.3):
```json
{
  "estado": "INICIADO",  // ✅ Debe ser INICIADO
  "fechaHoraInicioReal": "2025-12-03T10:30:00"  // ✅ Tiene fecha real
}
```

### Después de Finalizar Tramo (7.4):
```json
{
  "estado": "FINALIZADO",  // ✅ Debe ser FINALIZADO
  "fechaHoraFinReal": "2025-12-03T14:30:00"  // ✅ Tiene fecha real
}
```

Verificar en **4.4 Consultar Estado** que la solicitud esté en estado `ENTREGADA`.

## 📞 Soporte

Para dudas o problemas:
1. Verificar logs de Docker: `docker compose logs -f`
2. Verificar que todos los servicios estén activos: `docker compose ps`
3. Revisar la documentación Swagger: `http://localhost:8080/swagger-ui.html`
