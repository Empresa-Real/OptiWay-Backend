// nav.js - Componente compartido de navegación, autenticación y control de roles

(function () {
    // Inicializar cliente de Supabase
    const supabaseClient = window.supabase.createClient(
        window.APP_CONFIG.supabaseUrl,
        window.APP_CONFIG.supabaseAnonKey
    );

    // Obtener token JWT de sesión
    async function getToken() {
        const { data } = await supabaseClient.auth.getSession();
        const token = data.session?.access_token;
        if (!token) {
            window.location.href = '/';
            return null;
        }
        return token;
    }

    // Cerrar sesión
    async function cerrarSesion() {
        await supabaseClient.auth.signOut();
        window.location.href = '/';
    }

    // Ruta por defecto según rol
    function getRutaInicioPorRol(rol) {
        switch (rol) {
            case 'ADMINISTRADOR': return '/admin.html';
            case 'ENCARGADO_TIENDA': return '/inventario.html';
            case 'ENCARGADO_CD': return '/ingreso-mercancia.html';
            case 'PLANIFICADOR': return '/main.html';
            default: return '/main.html';
        }
    }

    // Inicializar navegación y control de acceso
    async function initNav(options = {}) {
        const token = await getToken();
        if (!token) return null;

        let usuario = null;
        try {
            const res = await fetch('/api/usuarios/me', {
                headers: { Authorization: 'Bearer ' + token }
            });

            if (!res.ok) {
                // Usuario no registrado en BD local aún
                return { usuario: null, token };
            }

            usuario = await res.json();
        } catch (err) {
            console.error('Error obteniendo perfil de usuario:', err);
            return null;
        }

        // Validación de permisos para la página actual
        const rolesPermitidos = options.rolesPermitidos;
        if (rolesPermitidos && rolesPermitidos.length > 0 && !rolesPermitidos.includes(usuario.rol)) {
            alert(`[Acceso Denegado] Tu rol (${usuario.rol}) no tiene permisos para acceder a esta página.`);
            window.location.href = getRutaInicioPorRol(usuario.rol);
            return null;
        }

        // Renderizar barra de navegación según rol
        const navContainer = document.getElementById('nav-bar') || document.getElementById('app-nav');
        if (navContainer) {
            const enlaces = [];

            // Inicio
            enlaces.push('<a href="/main.html">Inicio</a>');

            // Módulo de Usuarios (solo Administrador)
            if (usuario.rol === 'ADMINISTRADOR') {
                enlaces.push('<a href="/admin.html">Usuarios</a>');
            }

            // Tiendas (Admin, Planificador y Encargado de Tienda)
            if (usuario.rol === 'ADMINISTRADOR' || usuario.rol === 'PLANIFICADOR' || usuario.rol === 'ENCARGADO_TIENDA') {
                enlaces.push('<a href="/tiendas.html">Tiendas</a>');
            }

            // Centros de Distribución (Admin, Planificador y Encargado de CD)
            if (usuario.rol === 'ADMINISTRADOR' || usuario.rol === 'PLANIFICADOR' || usuario.rol === 'ENCARGADO_CD') {
                enlaces.push('<a href="/centros-distribucion.html">Centros de Distribución</a>');
            }

            // Inventario Tienda (Admin, Planificador y Encargado de Tienda)
            if (usuario.rol === 'ADMINISTRADOR' || usuario.rol === 'PLANIFICADOR' || usuario.rol === 'ENCARGADO_TIENDA') {
                enlaces.push('<a href="/inventario.html">Inventario Tienda</a>');
            }

            // Ingreso Mercancía CD (Admin, Planificador y Encargado de CD)
            if (usuario.rol === 'ADMINISTRADOR' || usuario.rol === 'PLANIFICADOR' || usuario.rol === 'ENCARGADO_CD') {
                enlaces.push('<a href="/ingreso-mercancia.html">Ingreso Mercancía CD</a>');
            }

            navContainer.innerHTML = enlaces.join(' | ') +
                ' &nbsp;&nbsp;&nbsp; <button id="logout" type="button">Cerrar sesión</button>';
        }

        // Asegurar que el botón de cerrar sesión siempre tenga el evento asignado
        document.getElementById('logout')?.addEventListener('click', cerrarSesion);

        return { usuario, token, supabaseClient };
    }

    // Exportar al objeto global window.OptiWay
    window.OptiWay = {
        supabase: supabaseClient,
        getToken,
        cerrarSesion,
        getRutaInicioPorRol,
        initNav
    };
})();
