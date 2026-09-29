(function () {
    function iniciarBusquedaYFiltro() {
        var input = document.getElementById('buscarProducto');
        var botonesCategoria = document.querySelectorAll('.cat');
        var tarjetas = document.querySelectorAll('.product');
        var sinResultados = document.getElementById('sinResultadosProductos');
        var categoriaActiva = 'Todas';

        function aplicarFiltro() {
            var texto = (input ? input.value : '').trim().toLowerCase();
            var visibles = 0;
            tarjetas.forEach(function (tarjeta) {
                var nombre = (tarjeta.dataset.nombre || '').toLowerCase();
                var sku = (tarjeta.dataset.sku || '').toLowerCase();
                var categoria = tarjeta.dataset.categoria || '';
                var coincideTexto = texto === '' || nombre.indexOf(texto) !== -1 || sku.indexOf(texto) !== -1;
                var coincideCategoria = categoriaActiva === 'Todas' || categoria === categoriaActiva;
                var visible = coincideTexto && coincideCategoria;
                tarjeta.style.display = visible ? '' : 'none';
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
        botonesCategoria.forEach(function (boton) {
            boton.addEventListener('click', function () {
                botonesCategoria.forEach(function (b) { b.classList.remove('active'); });
                boton.classList.add('active');
                categoriaActiva = boton.dataset.cat;
                aplicarFiltro();
            });
        });
    }

    function iniciarPagoYCambio() {
        var radiosPago = document.querySelectorAll('input[name="metodoPago"]');
        var filaEfectivo = document.getElementById('filaEfectivo');
        var montoRecibido = document.getElementById('montoRecibido');
        var totalElemento = document.getElementById('totalVenta');
        var cambioElemento = document.getElementById('montoCambio');

        function actualizarVisibilidadEfectivo() {
            var seleccionado = document.querySelector('input[name="metodoPago"]:checked');
            var esEfectivo = seleccionado && seleccionado.value === 'EFECTIVO';
            if (filaEfectivo) {
                filaEfectivo.style.display = esEfectivo ? '' : 'none';
            }
        }

        function actualizarCambio() {
            if (!totalElemento || !cambioElemento || !montoRecibido) {
                return;
            }
            var total = parseFloat(totalElemento.dataset.total || '0') || 0;
            var recibido = parseFloat(montoRecibido.value) || 0;
            var cambio = recibido - total;
            cambioElemento.textContent = '$' + Math.max(0, cambio).toFixed(2);
            cambioElemento.classList.toggle('negativo', cambio < 0);
        }

        radiosPago.forEach(function (radio) {
            radio.addEventListener('change', actualizarVisibilidadEfectivo);
        });
        if (montoRecibido) {
            montoRecibido.addEventListener('input', actualizarCambio);
        }
        actualizarVisibilidadEfectivo();
        actualizarCambio();
    }

    function iniciarConfirmacionCancelar() {
        var boton = document.getElementById('botonCancelarVenta');
        var modal = document.getElementById('modalCancelarVenta');
        var botonConfirmar = document.getElementById('botonConfirmarCancelar');
        var botonCerrar = document.getElementById('botonCerrarCancelar');
        var formVaciar = document.getElementById('formVaciarVenta');
        if (!boton || !modal || !botonConfirmar || !formVaciar) {
            return;
        }
        boton.addEventListener('click', function () { modal.showModal(); });
        botonCerrar.addEventListener('click', function () { modal.close(); });
        botonConfirmar.addEventListener('click', function () {
            modal.close();
            formVaciar.submit();
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        iniciarBusquedaYFiltro();
        iniciarPagoYCambio();
        iniciarConfirmacionCancelar();
    });
})();
