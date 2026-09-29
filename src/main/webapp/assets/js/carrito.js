/**
 * MODULO 3: carrito. Confirma antes de vaciar todo el carrito (una
 * accion destructiva e irreversible) con un dialog nativo en vez de
 * enviar el formulario directo.
 */
(function () {
    'use strict';

    function iniciarConfirmacionVaciado() {
        var boton = document.getElementById('botonVaciarCarrito');
        var dialog = document.getElementById('modalConfirmarVaciado');
        var form = document.getElementById('formVaciarCarrito');
        if (!boton || !dialog || !form) {
            return;
        }
        var botonCancelar = document.getElementById('botonCancelarVaciado');
        var botonConfirmar = document.getElementById('botonConfirmarVaciado');

        boton.addEventListener('click', function () {
            dialog.showModal();
        });
        if (botonCancelar) {
            botonCancelar.addEventListener('click', function () {
                dialog.close();
            });
        }
        if (botonConfirmar) {
            botonConfirmar.addEventListener('click', function () {
                form.submit();
            });
        }
        dialog.addEventListener('click', function (evento) {
            if (evento.target === dialog) {
                dialog.close();
            }
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        try {
            iniciarConfirmacionVaciado();
        } catch (error) {
            console.error('[carrito] fallo al iniciar confirmacion de vaciado', error);
        }
    });
})();
