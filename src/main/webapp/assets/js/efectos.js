/**
 * CAPA 1: motor de efectos visuales y motion UX de /tienda/*.
 * JavaScript Vanilla (ES6+), sin dependencias externas. Cada modulo se
 * inicializa por separado y con try/catch propio: si uno falla, el resto
 * de la pagina (formularios, navegacion) sigue funcionando igual.
 */
(function () {
    'use strict';

    var ES_PUNTERO_TOSCO = window.matchMedia('(pointer: coarse)').matches;

    function obtenerContextPath() {
        var header = document.querySelector('.encabezado-tienda');
        return header ? (header.dataset.contextPath || '') : '';
    }

    // ---------- Parallax 3D de fondo ----------
    function iniciarParallax() {
        var elementos = document.querySelectorAll('.bg-coffee-parallax');
        if (!elementos.length) {
            return;
        }
        var actualizando = false;

        function actualizar() {
            var scrollY = window.scrollY || 0;
            elementos.forEach(function (el) {
                var traslado = scrollY * 0.25;
                var rotX = Math.min(scrollY * 0.05, 25);
                var rotZ = scrollY * 0.02;
                el.style.transform = 'translateY(' + traslado + 'px) rotateX(' + rotX + 'deg) rotateZ(' + rotZ + 'deg)';
            });
            actualizando = false;
        }

        window.addEventListener('scroll', function () {
            if (!actualizando) {
                actualizando = true;
                requestAnimationFrame(actualizar);
            }
        }, { passive: true });

        actualizar();
    }

    // ---------- Vapor: particulas de fondo en el hero ----------
    function iniciarVaporCanvas() {
        var canvas = document.getElementById('vaporCanvas');
        if (!canvas) {
            return;
        }
        var hero = canvas.closest('.hero-tienda') || canvas.parentElement;
        var ctx = canvas.getContext('2d');
        var particulas = [];
        var MAX_PARTICULAS = 45;
        var mouseX = 0;

        function ajustarTamano() {
            canvas.width = hero.clientWidth;
            canvas.height = hero.clientHeight;
            mouseX = canvas.width / 2;
        }
        ajustarTamano();
        window.addEventListener('resize', ajustarTamano);

        hero.addEventListener('mousemove', function (evento) {
            var rect = hero.getBoundingClientRect();
            mouseX = evento.clientX - rect.left;
        });

        function crearParticula() {
            return {
                x: Math.random() * canvas.width,
                y: canvas.height + 10,
                radio: 2 + Math.random() * 3,
                velocidadY: 0.35 + Math.random() * 0.55,
                deriva: (Math.random() - 0.5) * 0.5,
                opacidad: 0.05 + Math.random() * 0.12
            };
        }

        function animar() {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            if (particulas.length < MAX_PARTICULAS && Math.random() > 0.6) {
                particulas.push(crearParticula());
            }
            particulas.forEach(function (p) {
                var atraccion = (mouseX - p.x) * 0.0015;
                p.x += p.deriva + atraccion;
                p.y -= p.velocidadY;
                ctx.beginPath();
                ctx.arc(p.x, p.y, p.radio, 0, Math.PI * 2);
                ctx.fillStyle = 'rgba(248, 246, 240, ' + p.opacidad + ')';
                ctx.fill();
            });
            particulas = particulas.filter(function (p) {
                return p.y + p.radio > -10;
            });
            requestAnimationFrame(animar);
        }
        requestAnimationFrame(animar);
    }

    // ---------- Tarjetas: spotlight + 3D tilt ----------
    function iniciarSpotlightYTilt(raiz) {
        if (ES_PUNTERO_TOSCO) {
            return;
        }
        var tarjetas = (raiz || document).querySelectorAll('.tarjeta-producto, .tarjeta-categoria');
        tarjetas.forEach(function (tarjeta) {
            tarjeta.addEventListener('mousemove', function (evento) {
                var rect = tarjeta.getBoundingClientRect();
                var x = evento.clientX - rect.left;
                var y = evento.clientY - rect.top;

                tarjeta.style.setProperty('--mouse-x', (x / rect.width) * 100 + '%');
                tarjeta.style.setProperty('--mouse-y', (y / rect.height) * 100 + '%');

                var tiltX = ((y / rect.height) - 0.5) * -8;
                var tiltY = ((x / rect.width) - 0.5) * 8;
                tarjeta.style.setProperty('--tilt-x', tiltX.toFixed(2) + 'deg');
                tarjeta.style.setProperty('--tilt-y', tiltY.toFixed(2) + 'deg');

                tarjeta.classList.add('spotlight-activo', 'tilt-activo');
            });

            tarjeta.addEventListener('mouseleave', function () {
                tarjeta.classList.remove('spotlight-activo', 'tilt-activo');
                tarjeta.style.setProperty('--tilt-x', '0deg');
                tarjeta.style.setProperty('--tilt-y', '0deg');
            });
        });
    }

    // ---------- Cursor magnetico ----------
    function iniciarCursorMagnetico() {
        if (ES_PUNTERO_TOSCO) {
            return;
        }
        var cursor = document.createElement('div');
        cursor.className = 'cursor-magnetico';
        document.body.appendChild(cursor);

        var x = window.innerWidth / 2;
        var y = window.innerHeight / 2;
        var cx = x;
        var cy = y;

        document.addEventListener('mousemove', function (evento) {
            x = evento.clientX;
            y = evento.clientY;
            cursor.classList.add('cursor-listo');
            var sobreBoton = evento.target.closest ? evento.target.closest('.boton, .carrito-boton') : null;
            cursor.classList.toggle('cursor-sobre-boton', !!sobreBoton);
        });

        document.addEventListener('mouseleave', function () {
            cursor.classList.remove('cursor-listo');
        });

        function animar() {
            cx += (x - cx) * 0.2;
            cy += (y - cy) * 0.2;
            cursor.style.transform = 'translate(' + cx + 'px, ' + cy + 'px) translate(-50%, -50%)';
            requestAnimationFrame(animar);
        }
        requestAnimationFrame(animar);
    }

    // ---------- Toast notifications (estilo espresso) ----------
    var toastContenedor = null;

    function obtenerContenedorToast() {
        if (!toastContenedor) {
            toastContenedor = document.createElement('div');
            toastContenedor.className = 'toast-contenedor';
            toastContenedor.setAttribute('role', 'status');
            toastContenedor.setAttribute('aria-live', 'polite');
            document.body.appendChild(toastContenedor);
        }
        return toastContenedor;
    }

    function mostrarToast(mensaje, tipo) {
        var contenedor = obtenerContenedorToast();
        var toast = document.createElement('div');
        toast.className = 'toast-espresso' + (tipo === 'error' ? ' toast-error' : '');

        var texto = document.createElement('span');
        texto.textContent = mensaje;

        var barra = document.createElement('div');
        barra.className = 'toast-barra';

        toast.appendChild(texto);
        toast.appendChild(barra);
        contenedor.appendChild(toast);

        var quitar = function () {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        };
        toast.addEventListener('click', quitar);
        setTimeout(quitar, 3000);
    }

    // ---------- Odometro: cuenta desde 0 hasta el valor real ya renderizado ----------
    /**
     * Si el precio cambia varias veces seguidas (ej. el usuario alterna
     * opciones de personalizacion rapido), cada llamada arranca su propio
     * requestAnimationFrame; sin coordinacion, la animacion mas vieja podia
     * seguir escribiendo texto despues de que una mas nueva ya habia
     * terminado, dejando el numero final equivocado. Un contador de
     * ejecucion por elemento hace que solo la ultima animacion iniciada
     * sobre ese elemento pueda seguir escribiendo.
     */
    var contadorOdometro = 0;

    function animarOdometroElemento(el) {
        var textoOriginal = el.textContent.trim();
        var coincidencia = textoOriginal.match(/\d[\d,]*\.?\d*/);
        if (!coincidencia) {
            return;
        }
        var crudo = coincidencia[0];
        var valorFinal = parseFloat(crudo.replace(/,/g, ''));
        if (isNaN(valorFinal)) {
            return;
        }
        var decimales = crudo.indexOf('.') >= 0 ? (crudo.split('.')[1] || '').length : 0;
        var prefijo = textoOriginal.slice(0, coincidencia.index);
        var sufijo = textoOriginal.slice(coincidencia.index + crudo.length);
        var duracionMs = 700;
        var inicio = null;
        var miEjecucion = String(++contadorOdometro);
        el.dataset.odometroEjecucion = miEjecucion;

        function paso(marca) {
            if (el.dataset.odometroEjecucion !== miEjecucion) {
                return;
            }
            if (inicio === null) {
                inicio = marca;
            }
            var progreso = Math.min((marca - inicio) / duracionMs, 1);
            var facilitado = 1 - Math.pow(1 - progreso, 3);
            el.textContent = prefijo + (valorFinal * facilitado).toFixed(decimales) + sufijo;
            if (progreso < 1) {
                requestAnimationFrame(paso);
            } else {
                el.textContent = textoOriginal;
            }
        }
        requestAnimationFrame(paso);
    }

    function iniciarOdometros(raiz) {
        (raiz || document).querySelectorAll('.odometro').forEach(animarOdometroElemento);
    }

    // ---------- Agregar al carrito por AJAX (sin recargar la pagina) ----------
    /**
     * Lee del propio form los mismos campos que el CarritoViewServlet
     * espera en el POST tradicional (productoId, cantidad, y las opciones
     * de personalizacion: radios "opcionId_g{grupoId}" para grupos de
     * seleccion unica, checkboxes "opcionId" para grupos multiples), para
     * poder enviarlos como JSON al endpoint REST /api/carrito que ya
     * existia pero ningun frontend consumia todavia.
     */
    function extraerDatosFormulario(form) {
        var productoIdInput = form.querySelector('input[name="productoId"]');
        var cantidadInput = form.querySelector('input[name="cantidad"]');
        var opcionIds = [];
        form.querySelectorAll('input[type="radio"]:checked, input[type="checkbox"]:checked').forEach(function (input) {
            if (input.name === 'opcionId' || input.name.indexOf('opcionId_g') === 0) {
                var valor = parseInt(input.value, 10);
                if (!isNaN(valor)) {
                    opcionIds.push(valor);
                }
            }
        });
        return {
            productoId: productoIdInput ? parseInt(productoIdInput.value, 10) : null,
            cantidad: cantidadInput ? (parseInt(cantidadInput.value, 10) || 1) : 1,
            opcionIds: opcionIds
        };
    }

    /** Actualiza el contador del carrito en el encabezado con el dato real que acaba de confirmar el servidor. */
    function actualizarBadgeCarrito(cantidadUnidades) {
        var boton = document.querySelector('.carrito-boton');
        if (!boton) {
            return;
        }
        var badge = boton.querySelector('.contador');
        if (cantidadUnidades > 0) {
            if (!badge) {
                badge = document.createElement('span');
                badge.className = 'contador odometro';
                boton.appendChild(badge);
            }
            badge.textContent = String(cantidadUnidades);
            badge.classList.add('cart-badge-bounce');
            badge.addEventListener('animationend', function () {
                badge.classList.remove('cart-badge-bounce');
            }, { once: true });
        } else if (badge) {
            badge.remove();
        }
    }

    function marcarBotonComoAgregado(boton) {
        if (!boton) {
            return;
        }
        var textoOriginal = boton.textContent;
        boton.classList.add('boton-agregado');
        boton.textContent = '✓ Agregado';
        setTimeout(function () {
            boton.classList.remove('boton-agregado');
            boton.textContent = textoOriginal;
        }, 1400);
    }

    /**
     * Envia el alta al carrito por fetch en vez de un POST de pagina
     * completa. El backend vuelve a validar todo (producto, precio,
     * opciones, stock) exactamente igual que en el flujo por formulario;
     * esta funcion solo cambia COMO se transporta esa misma peticion. Si
     * la red falla (no la validacion: eso responde 4xx con JSON, no
     * lanza una excepcion de fetch) se recurre al envio real del form
     * para no dejar al cliente sin forma de comprar.
     */
    function enviarAgregarCarrito(form, boton) {
        var datos = extraerDatosFormulario(form);
        var contextPath = obtenerContextPath();

        fetch(contextPath + '/api/carrito', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
            body: JSON.stringify(datos)
        }).then(function (respuesta) {
            return respuesta.json().then(function (cuerpo) {
                return { ok: respuesta.ok, cuerpo: cuerpo };
            });
        }).then(function (resultado) {
            form.dataset.volando = '';
            if (boton) {
                boton.disabled = false;
            }
            if (resultado.ok && resultado.cuerpo && resultado.cuerpo.datos) {
                actualizarBadgeCarrito(resultado.cuerpo.datos.cantidadUnidades);
                marcarBotonComoAgregado(boton);
                mostrarToast(resultado.cuerpo.mensaje || 'Se agrego al carrito');
            } else {
                mostrarToast((resultado.cuerpo && resultado.cuerpo.mensaje) || 'No se pudo agregar el producto.', 'error');
            }
        }).catch(function (error) {
            console.error('[efectos] fallo el alta por AJAX, se reenvia el formulario', error);
            try {
                sessionStorage.setItem('cdb_pulso_carrito', '1');
            } catch (almacenamientoNoDisponible) {
                // Sin sessionStorage (modo privado, etc.): se omite solo el pulso del badge tras recargar.
            }
            form.submit();
        });
    }

    // ---------- Fly-to-cart ----------
    /** Sube por los ancestros del form hasta hallar uno que contenga un icono de producto visible. */
    function buscarOrigenDeVuelo(form) {
        var nodo = form.parentElement;
        while (nodo && nodo !== document.body) {
            var candidato = nodo.querySelector('.imagen-producto, .detalle-imagen, .beverage-layer-container, .detalle-imagen-foto');
            if (candidato) {
                return candidato;
            }
            nodo = nodo.parentElement;
        }
        return null;
    }

    function iniciarFlyToCart(raiz) {
        var formularios = (raiz || document).querySelectorAll('form.form-agregar-carrito');
        var iconoCarrito = document.querySelector('.carrito-boton');

        formularios.forEach(function (form) {
            form.addEventListener('submit', function (evento) {
                if (form.dataset.volando === '1') {
                    evento.preventDefault();
                    return;
                }
                if (!iconoCarrito) {
                    return;
                }
                var origenEl = buscarOrigenDeVuelo(form);
                if (!origenEl) {
                    return;
                }

                evento.preventDefault();
                form.dataset.volando = '1';
                var boton = form.querySelector('button[type="submit"]');
                if (boton) {
                    boton.disabled = true;
                }

                var rectOrigen = origenEl.getBoundingClientRect();
                var rectDestino = iconoCarrito.getBoundingClientRect();

                var clon = document.createElement('div');
                clon.className = 'fly-item';
                clon.textContent = (origenEl.textContent || '').trim() || String.fromCodePoint(9749);
                clon.style.left = rectOrigen.left + 'px';
                clon.style.top = rectOrigen.top + 'px';
                clon.style.width = rectOrigen.width + 'px';
                clon.style.height = rectOrigen.height + 'px';
                document.body.appendChild(clon);

                requestAnimationFrame(function () {
                    requestAnimationFrame(function () {
                        var deltaX = (rectDestino.left + rectDestino.width / 2) - (rectOrigen.left + rectOrigen.width / 2);
                        var deltaY = (rectDestino.top + rectDestino.height / 2) - (rectOrigen.top + rectOrigen.height / 2);
                        clon.style.transform = 'translate(' + deltaX + 'px, ' + deltaY + 'px) scale(0.15)';
                        clon.classList.add('fly-en-vuelo');
                    });
                });

                setTimeout(function () {
                    if (clon.parentNode) {
                        clon.parentNode.removeChild(clon);
                    }
                    enviarAgregarCarrito(form, boton);
                }, 550);
            });
        });
    }

    // ---------- Ripple (onda concentrica al hacer clic) ----------
    function iniciarRipple() {
        document.querySelectorAll('.boton-ripple').forEach(function (boton) {
            boton.addEventListener('click', function (evento) {
                var rect = boton.getBoundingClientRect();
                var diametro = Math.max(rect.width, rect.height);
                var circulo = document.createElement('span');
                circulo.className = 'ripple-circulo';
                circulo.style.width = diametro + 'px';
                circulo.style.height = diametro + 'px';
                circulo.style.left = (evento.clientX - rect.left - diametro / 2) + 'px';
                circulo.style.top = (evento.clientY - rect.top - diametro / 2) + 'px';
                boton.appendChild(circulo);
                circulo.addEventListener('animationend', function () {
                    circulo.remove();
                });
            });
        });
    }

    // ---------- Pulso del badge tras la recarga real ----------
    function iniciarPulsoBadgeSiCorresponde() {
        var pulsar = false;
        try {
            pulsar = sessionStorage.getItem('cdb_pulso_carrito') === '1';
            if (pulsar) {
                sessionStorage.removeItem('cdb_pulso_carrito');
            }
        } catch (almacenamientoNoDisponible) {
            return;
        }
        if (!pulsar) {
            return;
        }
        var badge = document.querySelector('.carrito-boton .contador');
        if (!badge) {
            return;
        }
        badge.classList.add('cart-badge-bounce');
        badge.addEventListener('animationend', function () {
            badge.classList.remove('cart-badge-bounce');
        }, { once: true });
    }

    document.addEventListener('DOMContentLoaded', function () {
        var modulos = [
            iniciarParallax,
            iniciarVaporCanvas,
            iniciarSpotlightYTilt,
            iniciarCursorMagnetico,
            iniciarOdometros,
            iniciarFlyToCart,
            iniciarRipple,
            iniciarPulsoBadgeSiCorresponde
        ];
        modulos.forEach(function (modulo) {
            try {
                modulo();
            } catch (error) {
                console.error('[efectos] fallo al iniciar ' + modulo.name, error);
            }
        });
    });

    /**
     * API publica minima para que scripts especificos de una pagina (ej.
     * el live search del catalogo) puedan reutilizar el motor de CAPA 1
     * sobre contenido que insertan dinamicamente despues de la carga
     * inicial, sin duplicar logica ni volver a enganchar lo que ya existia.
     */
    window.CafeEfectos = {
        mostrarToast: mostrarToast,
        animarOdometro: animarOdometroElemento,
        reengancharTarjetas: function (contenedor) {
            var raiz = contenedor || document;
            try {
                iniciarSpotlightYTilt(raiz);
            } catch (error) {
                console.error('[efectos] fallo al reenganchar spotlight/tilt', error);
            }
            try {
                iniciarOdometros(raiz);
            } catch (error) {
                console.error('[efectos] fallo al reenganchar odometro', error);
            }
            try {
                iniciarFlyToCart(raiz);
            } catch (error) {
                console.error('[efectos] fallo al reenganchar fly-to-cart', error);
            }
        }
    };
})();
