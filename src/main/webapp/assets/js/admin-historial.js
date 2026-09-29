(function () {
    var FILAS_POR_PAGINA = 10;

    function iniciarBusquedaYFiltros() {
        var input = document.getElementById('buscarVenta');
        var selectMetodo = document.getElementById('filtroMetodo');
        var selectEstado = document.getElementById('filtroEstado');
        var filas = Array.prototype.slice.call(document.querySelectorAll('.fila-venta'));
        var sinResultados = document.getElementById('sinResultadosTabla');
        var contador = document.getElementById('contadorRegistros');
        var paginacion = document.getElementById('paginacion');
        var paginaActual = 1;

        function filasVisiblesPorFiltro() {
            var texto = (input ? input.value : '').trim().toLowerCase();
            var metodo = selectMetodo ? selectMetodo.value : 'todos';
            var estado = selectEstado ? selectEstado.value : 'todos';
            return filas.filter(function (fila) {
                var coincideTexto = texto === '' || (fila.dataset.busqueda || '').indexOf(texto) !== -1;
                var coincideMetodo = metodo === 'todos' || fila.dataset.metodo === metodo;
                var coincideEstado = estado === 'todos' || fila.dataset.estado === estado;
                return coincideTexto && coincideMetodo && coincideEstado;
            });
        }

        function renderizar() {
            var visibles = filasVisiblesPorFiltro();
            filas.forEach(function (fila) { fila.style.display = 'none'; });

            var totalPaginas = Math.max(1, Math.ceil(visibles.length / FILAS_POR_PAGINA));
            if (paginaActual > totalPaginas) {
                paginaActual = totalPaginas;
            }
            var inicio = (paginaActual - 1) * FILAS_POR_PAGINA;
            var paginaDeFilas = visibles.slice(inicio, inicio + FILAS_POR_PAGINA);
            paginaDeFilas.forEach(function (fila) { fila.style.display = ''; });

            if (sinResultados) {
                sinResultados.style.display = visibles.length === 0 ? '' : 'none';
            }
            if (contador) {
                contador.textContent = visibles.length === 0
                    ? 'No hay registros que coincidan'
                    : 'Mostrando ' + (inicio + 1) + ' a ' + Math.min(inicio + FILAS_POR_PAGINA, visibles.length) + ' de ' + visibles.length + ' registros';
            }
            renderizarPaginacion(totalPaginas);
        }

        function renderizarPaginacion(totalPaginas) {
            if (!paginacion) {
                return;
            }
            paginacion.innerHTML = '';
            var anterior = document.createElement('button');
            anterior.textContent = 'Anterior';
            anterior.disabled = paginaActual === 1;
            anterior.addEventListener('click', function () { paginaActual--; renderizar(); });
            paginacion.appendChild(anterior);

            for (var i = 1; i <= totalPaginas; i++) {
                var boton = document.createElement('button');
                boton.textContent = String(i);
                if (i === paginaActual) {
                    boton.classList.add('active');
                }
                (function (numero) {
                    boton.addEventListener('click', function () { paginaActual = numero; renderizar(); });
                })(i);
                paginacion.appendChild(boton);
            }

            var siguiente = document.createElement('button');
            siguiente.textContent = 'Siguiente';
            siguiente.disabled = paginaActual === totalPaginas;
            siguiente.addEventListener('click', function () { paginaActual++; renderizar(); });
            paginacion.appendChild(siguiente);
        }

        if (input) {
            input.addEventListener('input', function () { paginaActual = 1; renderizar(); });
        }
        if (selectMetodo) {
            selectMetodo.addEventListener('change', function () { paginaActual = 1; renderizar(); });
        }
        if (selectEstado) {
            selectEstado.addEventListener('change', function () { paginaActual = 1; renderizar(); });
        }

        renderizar();

        return filasVisiblesPorFiltro;
    }

    function iniciarExportar(obtenerFilasVisibles) {
        var boton = document.getElementById('botonExportar');
        if (!boton) {
            return;
        }
        boton.addEventListener('click', function () {
            var filas = obtenerFilasVisibles();
            var encabezado = ['Ticket', 'Fecha', 'Hora', 'Origen', 'Cliente', 'Metodo de pago', 'Items', 'Total', 'Estado'];
            var lineas = [encabezado.join(',')];
            filas.forEach(function (fila) {
                var celdas = [
                    fila.dataset.ticket,
                    fila.dataset.fecha,
                    fila.dataset.hora,
                    fila.dataset.origen,
                    '"' + (fila.dataset.cliente || '').replace(/"/g, '""') + '"',
                    fila.dataset.metodo,
                    '"' + (fila.dataset.items || '').replace(/"/g, '""') + '"',
                    fila.dataset.total,
                    fila.dataset.estado
                ];
                lineas.push(celdas.join(','));
            });
            var blob = new Blob([lineas.join('\n')], { type: 'text/csv;charset=utf-8' });
            var enlace = document.createElement('a');
            enlace.href = URL.createObjectURL(blob);
            enlace.download = 'historial-ventas-cafe-don-bosco.csv';
            enlace.click();
            URL.revokeObjectURL(enlace.href);
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        var obtenerFilasVisibles = iniciarBusquedaYFiltros();
        iniciarExportar(obtenerFilasVisibles);
    });
})();
