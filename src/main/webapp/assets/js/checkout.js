/**
 * MODULO 4: checkout. Reparte el formulario (que sigue siendo UNO solo,
 * el submit real sigue enviando todos los campos de siempre al mismo
 * CheckoutViewServlet) en una baraja de dos pasos con transicion 3D, y
 * agrega validacion visual en tiempo real. La validacion aqui es solo
 * una ayuda de UX: la autoridad sigue siendo el servidor.
 */
(function () {
    'use strict';

    var VALIDADORES = {
        nombreCompleto: function (valor) {
            return valor.trim().length >= 3;
        },
        correo: function (valor) {
            return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor.trim());
        },
        telefono: function (valor) {
            return valor.replace(/\D/g, '').length >= 8;
        },
        direccion: function (valor) {
            var domicilio = document.getElementById('tipoEntregaDomicilio');
            if (domicilio && domicilio.checked) {
                return valor.trim().length >= 5;
            }
            return true;
        }
    };

    function actualizarIndicador(contenedor) {
        var nombre = contenedor.dataset.validar;
        var input = contenedor.querySelector('input');
        var indicador = contenedor.querySelector('.indicador-validacion');
        var validador = VALIDADORES[nombre];
        if (!input || !validador) {
            return true;
        }
        var esValido = validador(input.value);
        contenedor.classList.remove('campo-valido', 'campo-invalido');
        contenedor.classList.add(esValido ? 'campo-valido' : 'campo-invalido');
        if (indicador) {
            indicador.textContent = esValido ? '✓' : '✗';
        }
        return esValido;
    }

    function iniciarValidacionEnVivo() {
        var campos = document.querySelectorAll('.campo[data-validar]');
        campos.forEach(function (contenedor) {
            var input = contenedor.querySelector('input');
            if (!input) {
                return;
            }
            input.addEventListener('blur', function () {
                actualizarIndicador(contenedor);
            });
            input.addEventListener('input', function () {
                if (contenedor.classList.contains('campo-valido') || contenedor.classList.contains('campo-invalido')) {
                    actualizarIndicador(contenedor);
                }
            });
        });

        document.querySelectorAll('input[name="tipoEntrega"]').forEach(function (radio) {
            radio.addEventListener('change', function () {
                var campoDireccion = document.querySelector('.campo[data-validar="direccion"]');
                if (campoDireccion && (campoDireccion.classList.contains('campo-valido') || campoDireccion.classList.contains('campo-invalido'))) {
                    actualizarIndicador(campoDireccion);
                }
            });
        });
    }

    function validarPaso1() {
        var campos = document.querySelectorAll('#pasoCheckout1 .campo[data-validar]');
        var todoValido = true;
        campos.forEach(function (contenedor) {
            if (!actualizarIndicador(contenedor)) {
                todoValido = false;
            }
        });
        return todoValido;
    }

    function iniciarPasos() {
        var paso1 = document.getElementById('pasoCheckout1');
        var paso2 = document.getElementById('pasoCheckout2');
        var botonSiguiente = document.getElementById('botonSiguientePaso');
        var botonVolver = document.getElementById('botonVolverPaso');
        var indicador1 = document.getElementById('indicadorPaso1');
        var indicador2 = document.getElementById('indicadorPaso2');
        if (!paso1 || !paso2 || !botonSiguiente) {
            return;
        }

        botonSiguiente.addEventListener('click', function () {
            if (!validarPaso1()) {
                return;
            }
            paso1.classList.remove('activo');
            paso1.classList.add('completado');
            paso2.classList.add('activo');
            if (indicador1) { indicador1.classList.remove('activo'); }
            if (indicador2) { indicador2.classList.add('activo'); }
            paso2.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        });

        if (botonVolver) {
            botonVolver.addEventListener('click', function () {
                paso2.classList.remove('activo');
                paso1.classList.remove('completado');
                paso1.classList.add('activo');
                if (indicador2) { indicador2.classList.remove('activo'); }
                if (indicador1) { indicador1.classList.add('activo'); }
            });
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        [iniciarValidacionEnVivo, iniciarPasos].forEach(function (modulo) {
            try {
                modulo();
            } catch (error) {
                console.error('[checkout] fallo al iniciar ' + modulo.name, error);
            }
        });
    });
})();
