# Guía Técnica: Análisis del Error en HU-11 y Flujo Estándar de Trabajo con Git (Ramas y Forks)

Este documento detalla el diagnóstico del problema ocurrido en la rama `origin/OptiWay-Backend-feature-HU-11-registrar-venta`, por qué impidió un merge automático en Git y GitKraken, y la guía paso a paso de cómo debe trabajar cualquier desarrollador del equipo al crear ramas o utilizar forks.

---

## 1. ¿Qué fue lo que hizo Cristian tan raro en HU-11?

Al inspeccionar el commit `39c781c` y la estructura del árbol en el repositorio remoto, se identificaron **tres anomalías críticas**:

### A. Repositorio huérfano sin historia común (`fatal: no merge base`)
* **Qué sucedió:** En lugar de pararse en su clon local del repositorio principal y ejecutar `git checkout -b feature/HU-11-registrar-venta`, Cristian abrió una carpeta nueva o descargó el código y ejecutó `git init`.
* **Consecuencia:** La rama quedó con un **único commit inicial huérfano**, sin padre (`parent commit`). Para Git, `main` y esa rama eran dos proyectos completamente distintos en el tiempo. Cualquier intento de `git merge` estándar arroja:
  ```text
  fatal: refusing to merge unrelated histories
  ```

### B. Carpeta anidada con duplicación de proyecto completo
* **Qué sucedió:** Cristian probablemente descargó un archivo `.zip` de GitHub de la rama de HU-07 (`OptiWay-Backend-feature-HU-07-Crear-Tienda.zip`), lo descomprimió dentro de su carpeta de trabajo, y cometió la carpeta completa en Git.
* **Consecuencia:** En vez de que los archivos nuevos estuvieran en la raíz:
  * ❌ `OptiWay-Backend-feature-HU-07-Crear-Tienda/pom.xml`
  * ❌ `OptiWay-Backend-feature-HU-07-Crear-Tienda/mvnw`
  * ❌ `OptiWay-Backend-feature-HU-07-Crear-Tienda/src/main/java/...`
  Si se hacía un merge directo, Git hubiese contaminado la raíz del repositorio con una subcarpeta redundante de un proyecto completo viejo de HU-07.

### C. Dependencia con código desactualizado
* Cristian programó el servicio de ventas basándose en una versión obsoleta del modelo de inventario (cuando existía `InventarioRepositoryPort` con métodos viejos) y usando `jakarta.validation` que ni siquiera estaba agregado en el `pom.xml` oficial.

---

## 2. Flujo de Trabajo Estándar: Trabajar con una Rama Hija (Recomendado para el equipo)

Cuando todos los desarrolladores tienen acceso directo de colaboradores al repositorio central:

```
 main (producción / integración)
   │
   ├── git checkout -b feature/HU-XX-nombre-tarea
   │   (desarrollas, haces commits limpios en la raíz)
   │
   └── Pull Request / Merge a main
```

### Paso a paso:

1. **Sincronizar `main` antes de empezar:**
   ```bash
   git checkout main
   git pull origin main
   ```
2. **Crear la rama hija a partir de `main` actualizado:**
   ```bash
   git checkout -b feature/HU-11-registrar-venta
   ```
   *(Nota: Siempre verificar con `git status` que estás en la rama creada y en la raíz del proyecto).*

3. **Desarrollar y probar en la raíz del proyecto:**
   * No crear carpetas intermedias ni clonar repositorios dentro del repositorio.
   * Compilar localmente para asegurar que no se rompe nada:
     ```powershell
     .\mvnw.cmd test-compile
     ```

4. **Hacer commits atómicos y claros:**
   ```bash
   git add src/main/java/...
   git commit -m "feat(ventas): implementar caso de uso RegistrarVenta"
   ```

5. **Subir la rama al repositorio remoto:**
   ```bash
   git push -u origin feature/HU-11-registrar-venta
   ```

6. **Crear Pull Request en GitHub:**
   * Abrir el PR de `feature/HU-11-registrar-venta` hacia `main`.
   * El revisor valida que no haya conflictos y mergea.

---

## 3. Flujo de Trabajo Estándar: Trabajar con un Fork (Bifurcación)

Si un compañero no tiene permisos directos para subir ramas al repositorio de la organización y debe trabajar en su propio Fork:

```
[Repositorio Central - upstream] (ema28pro/OptiWay-Backend)
       ▲
       │ Pull Request
       ▼
[Fork Personal - origin] (cristian/OptiWay-Backend)
       ▲
       │ push / pull
       ▼
[Máquina Local]
```

### Paso a paso:

1. **Hacer Fork en GitHub:**
   * En la página de `OptiWay-Backend`, hacer clic en el botón **Fork**.

2. **Clonar tu propio Fork en tu máquina:**
   ```bash
   git clone https://github.com/TU_USUARIO/OptiWay-Backend.git
   cd OptiWay-Backend
   ```

3. **Vincular el repositorio central oficial como `upstream`:**
   ```bash
   git remote add upstream https://github.com/ema28pro/OptiWay-Backend.git
   git remote -v
   ```
   *(Deberías ver `origin` apuntando a tu fork y `upstream` apuntando al repo del equipo).*

4. **Mantener tu `main` sincronizado con el equipo:**
   ```bash
   git checkout main
   git fetch upstream
   git merge upstream/main
   ```

5. **Crear una rama para tu historia:**
   ```bash
   git checkout -b feature/HU-11-registrar-venta
   ```

6. **Desarrollar, compilar y commitear:**
   ```bash
   git add .
   git commit -m "feat(ventas): registro de ventas"
   ```

7. **Subir la rama a tu fork (`origin`):**
   ```bash
   git push -u origin feature/HU-11-registrar-venta
   ```

8. **Crear Pull Request cruzado hacia `upstream`:**
   * En GitHub aparecerá el botón **"Contribute" -> "Open pull request"**.
   * Base repository: `ema28pro/OptiWay-Backend` (base: `main`).
   * Head repository: `TU_USUARIO/OptiWay-Backend` (compare: `feature/HU-11-registrar-venta`).

---

## 4. Reglas de Oro para Evitar estos Problemas

1. **Nunca usar ZIPs para pasar código:** Todo debe viajar por Git mediante ramas bien nombradas.
2. **Nunca hacer `git init` dentro de un repositorio ya existente.**
3. **Verificar siempre `git status` y la ruta actual antes de commitear.**
4. **Verificar `git log --graph --oneline`** para asegurarse de que tu rama desciende del `main` actual antes de pedir un merge.
