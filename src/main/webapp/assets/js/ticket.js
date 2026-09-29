/**
 * MODULO 5: ticket. Solo agrega el boton "Compartir por WhatsApp", que
 * pide al backend un enlace real de wa.me (con el resumen del pedido ya
 * armado por el servidor) y lo abre en una pestana nueva. El PDF oficial
 * y el boton de imprimir no necesitan JS: son un <a> real y window.print().
 */
(function () {
    'use strict';

    function iniciarCompartirWhatsApp() {
        var boton = document.getElementById('botonCompartirWhatsApp');
        if (!boton) {
            return;
        }
        var contextPath = boton.dataset.contextPath || '';
        var token = boton.dataset.token;

        boton.addEventListener('click', function () {
            boton.disabled = true;
            fetch(contextPath + '/api/tickets/' + token + '/whatsapp-link', { headers: { Accept: 'application/json' } })
                .then(function (respuesta) {
                    if (!respuesta.ok) {
                        throw new Error('HTTP ' + respuesta.status);
                    }
                    return respuesta.json();
                })
                .then(function (cuerpo) {
                    var enlace = cuerpo.datos && cuerpo.datos.whatsappUrl;
                    if (enlace) {
                        window.open(enlace, '_blank', 'noopener');
                    }
                })
                .catch(function (error) {
                    console.error('[ticket] fallo al generar el enlace de WhatsApp', error);
                })
                .finally(function () {
                    boton.disabled = false;
                });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        try {
            iniciarCompartirWhatsApp();
        } catch (error) {
            console.error('[ticket] fallo al iniciar', error);
        }
    });
})();
