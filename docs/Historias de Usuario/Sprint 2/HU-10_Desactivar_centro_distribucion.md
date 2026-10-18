# HU-10 — Desactivar Centro de Distribución

**Épica 2:** Catálogo y red de distribución  
**Feature:** Gestión de tiendas y CDs  
**Estado:** `Pendiente de Desarrollo (Backlog)`

---

## Historia de Usuario

> **Como** Administrador del sistema  
> **Quiero** desactivar un centro de distribución  
> **Para** reflejar que ya no está operativo y evitar que se le sigan asignando tiendas o recomendaciones de reposición.  

---

## Criterios de Aceptación

1. **Suspensión operativa:** Al desactivar un CD, deja de poder recibir tiendas asignadas o nuevas recomendaciones de reposición, y su historial se conserva íntegro en el sistema.
2. **Advertencia de tiendas asignadas:** Si el CD tiene tiendas asignadas en su zona de cobertura, el sistema advierte antes de desactivar y permite confirmar o cancelar la acción desde la interfaz de usuario.
3. **Restricción de acceso:** Solo el Administrador del sistema posee permisos para desactivar un centro de distribución (`403 Forbidden` para otros roles).

---

## Especificación Tentativa del Endpoint

* **Método:** `PATCH`
* **Ruta:** `/api/centros-distribucion/{id}/estado`
* **Seguridad:** Requiere header `Authorization: Bearer <token_jwt_supabase>` con rol `ADMINISTRADOR`.
* **Cuerpo de la petición (JSON):**

```json
{
  "activo": false
}
```

---

## Verificación Previa (Patrón Frontend)

* **Ruta recomendada:** `GET /api/centros-distribucion/{id}/tiendas-asignadas`
* Permite al Frontend verificar si existen tiendas dependientes de su zona de cobertura para mostrar el modal de confirmación antes del envío definitivo de la desactivación.
