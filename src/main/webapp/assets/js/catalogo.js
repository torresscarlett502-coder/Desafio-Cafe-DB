/**
 * MODULO 1: catalogo. Vive encima del motor de CAPA 1 (efectos.js) y solo
 * se activa donde encuentra sus elementos: el buscador en vivo
 * (#buscadorEnVivo + #resultadosCatalogo, solo en menu.jsp) y el modal de
 * vista rapida (#modalVistaRapida, en cualquier pagina que incluya
 * _modal-vista-rapida.jspf junto con tarjetas de producto).
 */
(function () {
    'use strict';

    function obtenerContextPath() {
        var header = document.querySelector('.encabezado-tienda');
        return header ? (header.dataset.contextPath || '') : '';
    }

    /**
     * La API solo serializa los campos reales del DTO (Gson no llama a
     * getters calculados como precioFormateado), asi que el precio se
     * formatea aqui a partir del numero crudo "precio".
     */
    function formatearPrecio(valor) {
        var numero = typeof valor === 'number' ? valor : parseFloat(valor);
        return isNaN(numero) ? '0.00' : numero.toFixed(2);
    }

    function resaltarCoincidencia(contenedor, texto, consulta) {
        contenedor.textContent = '';
        if (!consulta) {
            contenedor.textContent = texto;
            return;
        }
        var indice = texto.toLowerCase().indexOf(consulta.toLowerCase());
        if (indice === -1) {
            contenedor.textContent = texto;
            return;
        }
        contenedor.appendChild(document.createTextNode(texto.slice(0, indice)));
        var marca = document.createElement('mark');
        marca.textContent = texto.slice(indice, indice + consulta.length);
        contenedor.appendChild(marca);
        contenedor.appendChild(document.createTextNode(texto.slice(indice + consulta.length)));
    }

    function crearCampoOculto(form, nombre, valor) {
        var input = document.createElement('input');
        input.type = 'hidden';
        input.name = nombre;
        input.value = valor;
        form.appendChild(input);
    }

    function crearTarjetaProducto(producto, consulta) {
        var contextPath = obtenerContextPath();
        var tarjeta = document.createElement('div');
        tarjeta.className = 'tarjeta-producto' + (producto.disponible ? '' : ' agotada');

        var botonVista = document.createElement('button');
        botonVista.type = 'button';
        botonVista.className = 'boton-vista-rapida';
        botonVista.dataset.productoId = producto.id;
        botonVista.title = 'Vista rapida';
        botonVista.setAttribute('aria-label', 'Vista rapida de ' + producto.nombre);
        botonVista.textContent = '👁';
        tarjeta.appendChild(botonVista);

        if (!producto.disponible) {
            var badge = document.createElement('span');
            badge.className = 'badge-agotado';
            badge.textContent = 'Agotado';
            tarjeta.appendChild(badge);
        }

        var imagen = document.createElement('div');
        imagen.className = 'imagen-producto';
        if (producto.imagen) {
            imagen.style.backgroundImage = "url('" + contextPath + producto.imagen + "')";
            imagen.style.backgroundSize = 'cover';
            imagen.style.backgroundPosition = 'center';
        } else {
            imagen.textContent = '☕';
        }
        tarjeta.appendChild(imagen);

        var etiqueta = document.createElement('span');
        etiqueta.className = 'etiqueta';
        etiqueta.textContent = producto.categoriaNombre || '';
        tarjeta.appendChild(etiqueta);

        var titulo = document.createElement('h3');
        resaltarCoincidencia(titulo, producto.nombre || '', consulta);
        tarjeta.appendChild(titulo);

        var descripcion = document.createElement('p');
        descripcion.className = 'ayuda';
        descripcion.textContent = producto.descripcion || '';
        tarjeta.appendChild(descripcion);

        var precio = document.createElement('span');
        precio.className = 'precio odometro';
        precio.textContent = '$' + formatearPrecio(producto.precio);
        tarjeta.appendChild(precio);

        var estado = document.createElement('span');
        estado.className = producto.disponible ? 'disponible' : 'no-disponible';
        estado.textContent = producto.disponible ? 'Disponible' : 'Agotado';
        tarjeta.appendChild(estado);

        var verDetalle = document.createElement('a');
        verDetalle.className = 'boton secundario';
        verDetalle.href = contextPath + '/tienda/producto?id=' + producto.id;
        verDetalle.textContent = 'Ver detalle';
        tarjeta.appendChild(verDetalle);

        var form = document.createElement('form');
        form.method = 'post';
        form.action = contextPath + '/tienda/carrito';
        form.className = 'form-agregar-carrito';
        crearCampoOculto(form, 'accion', 'agregar');
        crearCampoOculto(form, 'productoId', producto.id);
        crearCampoOculto(form, 'cantidad', '1');
        crearCampoOculto(form, 'volver', window.location.pathname + window.location.search);

        var botonAgregar = document.createElement('button');
        botonAgregar.type = 'submit';
        botonAgregar.className = 'boton';
        botonAgregar.style.width = '100%';
        botonAgregar.textContent = '🛒 Agregar al carrito';
        botonAgregar.disabled = !producto.disponible;
        form.appendChild(botonAgregar);
        tarjeta.appendChild(form);

        return tarjeta;
    }

    function crearTarjetaEsqueleto() {
        var tarjeta = document.createElement('div');
        tarjeta.className = 'tarjeta-producto-skeleton';
        for (var i = 0; i < 3; i++) {
            var bloque = document.createElement('div');
            bloque.className = 'skeleton-bloque';
            tarjeta.appendChild(bloque);
        }
        return tarjeta;
    }

    function mostrarEsqueletos(contenedor, cantidad) {
        contenedor.innerHTML = '';
        var grid = document.createElement('div');
        grid.className = 'grid-productos';
        for (var i = 0; i < cantidad; i++) {
            grid.appendChild(crearTarjetaEsqueleto());
        }
        contenedor.appendChild(grid);
    }

    function renderizarResultados(contenedor, productos, consulta) {
        contenedor.innerHTML = '';
        if (!productos || !productos.length) {
            var vacio = document.createElement('p');
            vacio.className = 'estado-vacio';
            vacio.textContent = 'No encontramos productos con esos filtros.';
            contenedor.appendChild(vacio);
            return;
        }
        var grid = document.createElement('div');
        grid.className = 'grid-productos';
        productos.forEach(function (producto) {
            grid.appendChild(crearTarjetaProducto(producto, consulta));
        });
        contenedor.appendChild(grid);
        if (window.CafeEfectos) {
            window.CafeEfectos.reengancharTarjetas(grid);
        }
        iniciarPaginacion(contenedor);
    }

    // ---------- Paginacion (sobre las tarjetas ya renderizadas) ----------
    var PRODUCTOS_POR_PAGINA = 8;

    function iniciarPaginacion(contenedor) {
        var grid = contenedor.querySelector('.grid-productos');
        if (!grid) {
            return;
        }
        var tarjetas = Array.prototype.slice.call(grid.children);
        var totalPaginas = Math.max(1, Math.ceil(tarjetas.length / PRODUCTOS_POR_PAGINA));

        var pager = contenedor.querySelector('.paginacion-catalogo');
        if (!pager) {
            pager = document.createElement('div');
            pager.className = 'paginacion-catalogo';
            contenedor.appendChild(pager);
        }

        if (totalPaginas <= 1) {
            pager.innerHTML = '';
            return;
        }

        var pagina = 1;

        function mostrarPagina() {
            var inicio = (pagina - 1) * PRODUCTOS_POR_PAGINA;
            tarjetas.forEach(function (tarjeta, indice) {
                tarjeta.style.display = (indice >= inicio && indice < inicio + PRODUCTOS_POR_PAGINA) ? '' : 'none';
            });
            renderizarBotones();
            grid.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }

        function renderizarBotones() {
            pager.innerHTML = '';
            var anterior = document.createElement('button');
            anterior.type = 'button';
            anterior.className = 'boton secundario pequeno';
            anterior.textContent = '‹ Anterior';
            anterior.disabled = pagina === 1;
            anterior.addEventListener('click', function () { pagina--; mostrarPagina(); });
            pager.appendChild(anterior);

            for (var i = 1; i <= totalPaginas; i++) {
                (function (numero) {
                    var boton = document.createElement('button');
                    boton.type = 'button';
                    boton.className = 'boton pequeno' + (numero === pagina ? '' : ' secundario');
                    boton.textContent = String(numero);
                    boton.addEventListener('click', function () { pagina = numero; mostrarPagina(); });
                    pager.appendChild(boton);
                })(i);
            }

            var siguiente = document.createElement('button');
            siguiente.type = 'button';
            siguiente.className = 'boton secundario pequeno';
            siguiente.textContent = 'Siguiente ›';
            siguiente.disabled = pagina === totalPaginas;
            siguiente.addEventListener('click', function () { pagina++; mostrarPagina(); });
            pager.appendChild(siguiente);
        }

        mostrarPagina();
    }

    function construirUrlBusqueda(consulta) {
        var actuales = new URLSearchParams(window.location.search);
        var params = new URLSearchParams();
        if (consulta) {
            params.set('buscar', consulta);
        }
        var categoria = actuales.get('categoria');
        if (categoria) {
            params.set('categoria', categoria);
        }
        var orden = actuales.get('orden');
        if (orden) {
            params.set('orden', orden);
        }
        var qs = params.toString();
        return obtenerContextPath() + '/api/productos' + (qs ? '?' + qs : '');
    }

    // ---------- Live Search ----------
    function iniciarBusquedaEnVivo() {
        var input = document.getElementById('buscadorEnVivo');
        var contenedor = document.getElementById('resultadosCatalogo');
        if (!input || !contenedor) {
            return;
        }
        var temporizador = null;
        var controladorEnCurso = null;

        function ejecutarBusqueda(valor) {
            if (controladorEnCurso) {
                controladorEnCurso.abort();
            }
            controladorEnCurso = new AbortController();
            mostrarEsqueletos(contenedor, 4);

            fetch(construirUrlBusqueda(valor), { signal: controladorEnCurso.signal, headers: { Accept: 'application/json' } })
                .then(function (respuesta) {
                    if (!respuesta.ok) {
                        throw new Error('HTTP ' + respuesta.status);
                    }
                    return respuesta.json();
                })
                .then(function (cuerpo) {
                    renderizarResultados(contenedor, cuerpo.datos, valor);
                })
                .catch(function (error) {
                    if (error.name === 'AbortError') {
                        return;
                    }
                    console.error('[catalogo] fallo la busqueda en vivo', error);
                    contenedor.innerHTML = '';
                    var mensaje = document.createElement('p');
                    mensaje.className = 'estado-vacio';
                    mensaje.textContent = 'No se pudo cargar el catalogo. Intenta de nuevo.';
                    contenedor.appendChild(mensaje);
                });
        }

        input.addEventListener('input', function () {
            clearTimeout(temporizador);
            var valor = input.value.trim();
            temporizador = setTimeout(function () {
                ejecutarBusqueda(valor);
            }, 300);
        });

        input.addEventListener('keydown', function (evento) {
            if (evento.key === 'Enter') {
                evento.preventDefault();
                clearTimeout(temporizador);
                ejecutarBusqueda(input.value.trim());
            }
        });
    }

    // ---------- Quick View ----------
    function iniciarVistaRapida() {
        var dialog = document.getElementById('modalVistaRapida');
        if (!dialog) {
            return;
        }
        var contextPath = obtenerContextPath();
        var botonCerrar = document.getElementById('qvCerrar');

        function cerrar() {
            if (dialog.open) {
                dialog.close();
            }
        }

        if (botonCerrar) {
            botonCerrar.addEventListener('click', cerrar);
        }
        dialog.addEventListener('click', function (evento) {
            if (evento.target === dialog) {
                cerrar();
            }
        });

        function abrir(id) {
            var nombreEl = document.getElementById('qvNombre');
            var descripcionEl = document.getElementById('qvDescripcion');
            var categoriaEl = document.getElementById('qvCategoria');
            var precioEl = document.getElementById('qvPrecio');
            var disponibilidadEl = document.getElementById('qvDisponibilidad');
            var personalizacionEl = document.getElementById('qvPersonalizacion');
            var botonAgregar = document.getElementById('qvBotonAgregar');
            var imagenEl = document.getElementById('qvImagen');

            nombreEl.textContent = 'Cargando...';
            descripcionEl.textContent = '';
            categoriaEl.textContent = '';
            precioEl.textContent = '';
            disponibilidadEl.textContent = '';
            personalizacionEl.textContent = '';
            botonAgregar.disabled = true;
            if (imagenEl) {
                imagenEl.style.backgroundImage = '';
                imagenEl.textContent = '☕';
            }
            dialog.showModal();

            Promise.all([
                fetch(contextPath + '/api/productos/' + id, { headers: { Accept: 'application/json' } }).then(function (r) { return r.json(); }),
                fetch(contextPath + '/api/productos/' + id + '/opciones', { headers: { Accept: 'application/json' } }).then(function (r) { return r.json(); })
            ]).then(function (resultados) {
                var producto = resultados[0].datos;
                var grupos = resultados[1].datos || [];
                if (!producto) {
                    throw new Error('Producto no encontrado');
                }

                nombreEl.textContent = producto.nombre;
                descripcionEl.textContent = producto.descripcion || '';
                categoriaEl.textContent = producto.categoriaNombre || '';
                precioEl.textContent = '$' + formatearPrecio(producto.precio);
                if (window.CafeEfectos) {
                    window.CafeEfectos.animarOdometro(precioEl);
                }
                disponibilidadEl.textContent = producto.disponible ? '🟢 Disponible' : '🔴 Agotado';
                if (imagenEl) {
                    if (producto.imagen) {
                        imagenEl.style.backgroundImage = "url('" + contextPath + producto.imagen + "')";
                        imagenEl.style.backgroundSize = 'cover';
                        imagenEl.style.backgroundPosition = 'center';
                        imagenEl.textContent = '';
                    } else {
                        imagenEl.style.backgroundImage = '';
                        imagenEl.textContent = '☕';
                    }
                    imagenEl.style.filter = producto.disponible ? '' : 'grayscale(100%) opacity(0.6)';
                }

                if (grupos.length) {
                    var nombres = grupos.map(function (g) { return g.nombre; }).join(', ');
                    personalizacionEl.textContent = 'Personalizable: ' + nombres + ' (elige las opciones en la ficha del producto).';
                }

                var form = document.getElementById('qvForm');
                form.dataset.volando = '';
                document.getElementById('qvProductoId').value = producto.id;
                document.getElementById('qvVolver').value = window.location.pathname + window.location.search;
                document.getElementById('qvVerDetalle').href = contextPath + '/tienda/producto?id=' + producto.id;
                botonAgregar.disabled = !producto.disponible;
            }).catch(function (error) {
                console.error('[catalogo] fallo la vista rapida', error);
                nombreEl.textContent = 'No se pudo cargar el producto.';
            });
        }

        document.addEventListener('click', function (evento) {
            var boton = evento.target.closest ? evento.target.closest('.boton-vista-rapida') : null;
            if (!boton) {
                return;
            }
            var id = boton.dataset.productoId;
            if (id) {
                abrir(id);
            }
        });
    }

    function iniciarPaginacionInicial() {
        var contenedor = document.getElementById('resultadosCatalogo');
        if (contenedor) {
            iniciarPaginacion(contenedor);
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        [iniciarBusquedaEnVivo, iniciarVistaRapida, iniciarPaginacionInicial].forEach(function (modulo) {
            try {
                modulo();
            } catch (error) {
                console.error('[catalogo] fallo al iniciar ' + modulo.name, error);
            }
        });
    });
})();
