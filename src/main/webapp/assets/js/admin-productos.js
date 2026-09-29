(function () {
    var contextPath = document.body.dataset.contextPath || '';

    function iniciarBusquedaYFiltro() {
        var input = document.getElementById('buscarProducto');
        var selectCategoria = document.getElementById('filtroCategoria');
        var filas = document.querySelectorAll('.fila-producto');
        var sinResultados = document.getElementById('sinResultadosTabla');

        function aplicarFiltro() {
            var texto = (input ? input.value : '').trim().toLowerCase();
            var categoria = selectCategoria ? selectCategoria.value : 'todas';
            var visibles = 0;
            filas.forEach(function (fila) {
                var nombre = (fila.dataset.nombre || '').toLowerCase();
                var codigo = (fila.dataset.codigo || '').toLowerCase();
                var coincideTexto = texto === '' || nombre.indexOf(texto) !== -1 || codigo.indexOf(texto) !== -1;
                var coincideCategoria = categoria === 'todas' || fila.dataset.categoriaId === categoria;
                var visible = coincideTexto && coincideCategoria;
                fila.style.display = visible ? '' : 'none';
                if (visible) {
                    visibles++;
                }
            });
            if (sinResultados) {
                sinResultados.style.display = visibles === 0 ? '' : 'none';
            }
        }

        if (input) {
            input.addEventListener('input', aplicarFiltro);
        }
        if (selectCategoria) {
            selectCategoria.addEventListener('change', aplicarFiltro);
        }
    }

    function mostrarErrorFormulario(dialogo, mensaje) {
        var error = dialogo.querySelector('.error-formulario');
        if (error) {
            error.textContent = mensaje;
            error.style.display = 'block';
        }
    }

    function ocultarErrorFormulario(dialogo) {
        var error = dialogo.querySelector('.error-formulario');
        if (error) {
            error.style.display = 'none';
        }
    }

    function leerFormularioProducto(form) {
        return {
            categoriaId: parseInt(form.categoriaId.value, 10),
            nombre: form.nombre.value.trim(),
            descripcion: form.descripcion.value.trim(),
            precio: parseFloat(form.precio.value),
            imagen: form.imagen.value.trim() || null,
            tiempoPreparacionMinutos: form.tiempoPreparacionMinutos.value ? parseInt(form.tiempoPreparacionMinutos.value, 10) : null,
            stockInicial: form.stockInicial ? (form.stockInicial.value ? parseInt(form.stockInicial.value, 10) : null) : undefined,
            stockMinimo: form.stockMinimo.value ? parseInt(form.stockMinimo.value, 10) : null,
            activo: form.activo.checked
        };
    }

    function iniciarModalNuevo() {
        var boton = document.getElementById('botonNuevoProducto');
        var dialogo = document.getElementById('modalNuevoProducto');
        var form = document.getElementById('formNuevoProducto');
        var botonCancelar = document.getElementById('botonCancelarNuevo');
        if (!boton || !dialogo || !form) {
            return;
        }
        boton.addEventListener('click', function () {
            form.reset();
            form.activo.checked = true;
            ocultarErrorFormulario(dialogo);
            dialogo.showModal();
        });
        botonCancelar.addEventListener('click', function () { dialogo.close(); });
        form.addEventListener('submit', function (evento) {
            evento.preventDefault();
            var cuerpo = leerFormularioProducto(form);
            fetch(contextPath + '/api/admin/productos', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(cuerpo)
            }).then(function (respuesta) {
                return respuesta.json().then(function (json) { return { ok: respuesta.ok, json: json }; });
            }).then(function (resultado) {
                if (!resultado.ok) {
                    mostrarErrorFormulario(dialogo, resultado.json.mensaje || 'No se pudo crear el producto.');
                    return;
                }
                window.location.reload();
            }).catch(function () {
                mostrarErrorFormulario(dialogo, 'Error de conexion. Intenta de nuevo.');
            });
        });
    }

    function iniciarModalEditar() {
        var dialogo = document.getElementById('modalEditarProducto');
        var form = document.getElementById('formEditarProducto');
        var botonCancelar = document.getElementById('botonCancelarEditar');
        if (!dialogo || !form) {
            return;
        }
        document.querySelectorAll('.boton-editar').forEach(function (boton) {
            boton.addEventListener('click', function () {
                form.dataset.productoId = boton.dataset.id;
                form.nombre.value = boton.dataset.nombre;
                form.categoriaId.value = boton.dataset.categoriaId;
                form.descripcion.value = boton.dataset.descripcion || '';
                form.precio.value = boton.dataset.precio;
                form.imagen.value = boton.dataset.imagen || '';
                form.tiempoPreparacionMinutos.value = boton.dataset.tiempo || '';
                form.stockMinimo.value = boton.dataset.stockMinimo;
                form.activo.checked = boton.dataset.activo === 'true';
                ocultarErrorFormulario(dialogo);
                dialogo.showModal();
            });
        });
        botonCancelar.addEventListener('click', function () { dialogo.close(); });
        form.addEventListener('submit', function (evento) {
            evento.preventDefault();
            var cuerpo = leerFormularioProducto(form);
            delete cuerpo.stockInicial;
            fetch(contextPath + '/api/admin/productos/' + form.dataset.productoId, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(cuerpo)
            }).then(function (respuesta) {
                return respuesta.json().then(function (json) { return { ok: respuesta.ok, json: json }; });
            }).then(function (resultado) {
                if (!resultado.ok) {
                    mostrarErrorFormulario(dialogo, resultado.json.mensaje || 'No se pudo actualizar el producto.');
                    return;
                }
                window.location.reload();
            }).catch(function () {
                mostrarErrorFormulario(dialogo, 'Error de conexion. Intenta de nuevo.');
            });
        });
    }

    function iniciarConfirmacionEstado() {
        var dialogo = document.getElementById('modalConfirmarEstado');
        var mensaje = document.getElementById('mensajeConfirmarEstado');
        var botonConfirmar = document.getElementById('botonConfirmarEstado');
        var botonCancelar = document.getElementById('botonCancelarEstado');
        if (!dialogo || !botonConfirmar) {
            return;
        }
        var productoId = null;
        var nuevoEstado = null;

        document.querySelectorAll('.boton-estado').forEach(function (boton) {
            boton.addEventListener('click', function () {
                productoId = boton.dataset.id;
                var activoActual = boton.dataset.activo === 'true';
                nuevoEstado = !activoActual;
                mensaje.textContent = activoActual
                    ? '¿Desactivar "' + boton.dataset.nombre + '"? Ya no aparecera en el catalogo ni en el punto de venta.'
                    : '¿Activar "' + boton.dataset.nombre + '" de nuevo?';
                dialogo.showModal();
            });
        });

        botonCancelar.addEventListener('click', function () { dialogo.close(); });
        botonConfirmar.addEventListener('click', function () {
            fetch(contextPath + '/api/admin/productos/' + productoId + '/estado', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ activo: nuevoEstado })
            }).then(function () {
                window.location.reload();
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        iniciarBusquedaYFiltro();
        iniciarModalNuevo();
        iniciarModalEditar();
        iniciarConfirmacionEstado();
    });
})();
