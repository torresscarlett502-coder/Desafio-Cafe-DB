/**
 * MODULO 2: ficha de producto. Recalcula el precio mostrado segun las
 * opciones de personalizacion elegidas (usando los datos reales que ya
 * trae el HTML: precio base + precio adicional de cada opcion) y tinta
 * el simulador SVG de capas segun el grupo/opcion seleccionados.
 */
(function () {
    'use strict';

    function iniciarPersonalizacion() {
        var contenedor = document.querySelector('.personalizacion-producto');
        var precioEl = document.getElementById('detallePrecio');
        if (!contenedor || !precioEl) {
            return;
        }
        var precioBase = parseFloat(precioEl.dataset.precioBase) || 0;

        function seleccionadas() {
            return contenedor.querySelectorAll('input[type="radio"]:checked, input[type="checkbox"]:checked');
        }

        function recalcularPrecio() {
            var total = precioBase;
            seleccionadas().forEach(function (input) {
                total += parseFloat(input.dataset.precioAdicional) || 0;
            });
            precioEl.textContent = '$' + total.toFixed(2);
            if (window.CafeEfectos) {
                window.CafeEfectos.animarOdometro(precioEl);
            }
        }

        function actualizarSimulador() {
            var layerMilk = document.getElementById('layerMilk');
            var layerSyrup = document.getElementById('layerSyrup');
            if (!layerMilk && !layerSyrup) {
                return;
            }

            seleccionadas().forEach(function (input) {
                var grupoNombre = (input.dataset.grupoNombre || '').toLowerCase();
                var opcionNombre = (input.dataset.opcionNombre || '').toLowerCase();

                if (layerMilk && grupoNombre.indexOf('leche') !== -1) {
                    if (opcionNombre.indexOf('almendra') !== -1) {
                        layerMilk.setAttribute('fill', '#E8D2A6');
                    } else if (opcionNombre.indexOf('avena') !== -1) {
                        layerMilk.setAttribute('fill', '#E0C9A0');
                    } else {
                        layerMilk.setAttribute('fill', '#F4EFE6');
                    }
                }

                if (layerSyrup && grupoNombre.indexOf('azucar') !== -1) {
                    if (opcionNombre.indexOf('sin') !== -1) {
                        layerSyrup.style.opacity = '0';
                    } else if (opcionNombre.indexOf('extra') !== -1) {
                        layerSyrup.style.opacity = '1';
                    } else {
                        layerSyrup.style.opacity = '0.5';
                    }
                }
            });
        }

        contenedor.addEventListener('change', function (evento) {
            if (evento.target.type !== 'radio' && evento.target.type !== 'checkbox') {
                return;
            }
            recalcularPrecio();
            actualizarSimulador();
        });

        // El precio que ya trae el HTML es solo el precio base del producto:
        // si algun grupo de seleccion unica quedo con una opcion marcada por
        // defecto (la primera en orden alfabetico), hay que sumarla ahora
        // mismo o el precio mostrado no coincidiria con lo realmente elegido.
        recalcularPrecio();
        actualizarSimulador();
    }

    function iniciarSelectorCantidad() {
        var input = document.getElementById('cantidad');
        var botonMenos = document.querySelector('[data-cantidad-decrementar]');
        var botonMas = document.querySelector('[data-cantidad-incrementar]');
        if (!input || !botonMenos || !botonMas) {
            return;
        }
        var minimo = parseInt(input.min, 10) || 1;
        var maximo = parseInt(input.max, 10) || 20;

        function ajustar(delta) {
            var valor = parseInt(input.value, 10);
            if (isNaN(valor)) {
                valor = minimo;
            }
            valor = Math.min(maximo, Math.max(minimo, valor + delta));
            input.value = valor;
        }

        botonMenos.addEventListener('click', function () { ajustar(-1); });
        botonMas.addEventListener('click', function () { ajustar(1); });
    }

    document.addEventListener('DOMContentLoaded', function () {
        [iniciarPersonalizacion, iniciarSelectorCantidad].forEach(function (modulo) {
            try {
                modulo();
            } catch (error) {
                console.error('[producto] fallo al iniciar ' + modulo.name, error);
            }
        });
    });
})();
