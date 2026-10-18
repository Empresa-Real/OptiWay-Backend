# HU-08 — Desactivar Tienda

**Épica 2:** Catálogo y red de distribución  
**Feature:** Gestión de tiendas y CDs  
**Estado:** `Pendiente de Desarrollo (Backlog)`

---

## Historia de Usuario

> **Como** Administrador del sistema  
> **Quiero** desactivar una tienda  
> **Para** mantener actualizada la plataforma cuando esa tienda cierra o deja de operar.  

---

## Criterios de Aceptación

1. **Suspensión de operaciones:** Al desactivar una tienda, deja de recibir nuevas asignaciones de usuarios o inventario, y su historial se conserva íntegro en el sistema.
2. **Advertencia de órdenes en tránsito:** Si la tienda tiene una orden de reposición en tránsito, el sistema advierte antes de desactivar y permite confirmar o cancelar la acción desde la interfaz de usuario.
3. **Restricción de acceso:** Solo el Administrador del sistema posee permisos para desactivar una tienda (`403 Forbidden` para otros roles).

---

## Especificación Tentativa del Endpoint

* **Método:** `PATCH`
* **Ruta:** `/api/tiendas/{id}/estado`
* **Seguridad:** Requiere header `Authorization: Bearer <token_jwt_supabase>` con rol `ADMINISTRADOR`.
* **Cuerpo de la petición (JSON):**

```json
{
  "activo": false
}
```

---

## Verificación Previa (Patrón Frontend)

* **Ruta recomendada:** `GET /api/tiendas/{id}/ordenes-transito`
* Permite al Frontend verificar si existen órdenes activas en camino para presentar el modal de confirmación antes del envío del cambio de estado.
