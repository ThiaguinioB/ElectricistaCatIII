# Ejemplos de API

## Crear proyecto

```http
POST /api/projects
Content-Type: application/json

{
  "name": "Edificio Nueva Córdoba",
  "clientName": "Cooperativa Luz",
  "address": "Córdoba Capital",
  "description": "Instalación residencial",
  "voltageSystemType": "SINGLE_PHASE"
}
```

Respuesta:

```json
{
  "id": 1,
  "name": "Edificio Nueva Córdoba",
  "voltageSystemType": "SINGLE_PHASE",
  "calcProfileCode": "AEA-90364-7",
  "createdAt": "2024-10-01T12:00:00Z"
}
```

## Agregar circuito

```http
POST /api/panels/1/circuits
Content-Type: application/json

{
  "name": "Circuito iluminación",
  "demandPowerKw": 3.0,
  "voltage": 230,
  "cosPhi": 0.9,
  "efficiency": 0.9,
  "phaseType": "SINGLE_PHASE",
  "lengthMeters": 25,
  "conductorCrossSection": 2.5,
  "conductorMaterial": "CU",
  "installationMethod": "concealed_conduit",
  "groupingFactorCount": 1,
  "ambientTemperature": 30,
  "lighting": true
}
```

Respuesta:

```json
{
  "id": 5,
  "name": "Circuito iluminación",
  "designCurrent": 7.22,
  "breakerCurrent": 10.0,
  "iz": 24.0,
  "voltageDrop": 1.50
}
```

## Reporte técnico

```http
POST /api/projects/1/report
Accept: application/pdf
```

Devuelve PDF con expediente técnico, checklist, BOM y mediciones.
