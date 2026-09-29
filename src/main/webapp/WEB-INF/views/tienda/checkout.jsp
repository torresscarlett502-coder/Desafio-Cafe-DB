<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cafe Don Bosco - Finalizar compra</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tienda.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/efectos.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@600;700&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap">
</head>
<body>
<%@ include file="_header.jspf" %>

<div class="contenedor-tienda">
    <h2>Finalizar compra</h2>
    <p>Completa tus datos para procesar tu pedido.</p>

    <div class="pasos-checkout">
        <div class="paso activo" id="indicadorPaso1">
            <span class="numero">1</span>
            <span>Datos de envio</span>
        </div>
        <div class="paso" id="indicadorPaso2">
            <span class="numero">2</span>
            <span>Metodo de pago</span>
        </div>
        <div class="paso" id="indicadorPaso3">
            <span class="numero">3</span>
            <span>Confirmacion</span>
        </div>
    </div>

    <c:if test="${not empty error}">
        <div class="mensaje-error">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/tienda/checkout" id="formCheckout">
        <div class="layout-checkout">
            <div class="checkout-stack">
                <div class="paso-checkout-tarjeta tarjeta activo" id="pasoCheckout1">
                    <div class="paso-checkout-encabezado">
                        <span class="numero">1</span>
                        <h3 style="margin:0;">Datos de envio y entrega</h3>
                    </div>
                    <div class="paso-checkout-contenido">
                        <div class="campo" data-validar="nombreCompleto">
                            <label for="nombreCompleto">Nombre completo * <span class="indicador-validacion"></span></label>
                            <input type="text" id="nombreCompleto" name="nombreCompleto" value="${fn:escapeXml(datos.nombreCompleto)}" required>
                        </div>
                        <div class="campo" data-validar="correo">
                            <label for="correo">Correo electronico * <span class="indicador-validacion"></span></label>
                            <input type="email" id="correo" name="correo" value="${fn:escapeXml(datos.correo)}" required>
                        </div>
                        <div class="campo" data-validar="telefono">
                            <label for="telefono">Telefono * <span class="indicador-validacion"></span></label>
                            <input type="tel" id="telefono" name="telefono" value="${fn:escapeXml(datos.telefono)}" required>
                        </div>
                        <div class="campo">
                            <label>Tipo de entrega *</label>
                            <div class="opciones-radio">
                                <label>
                                    <input type="radio" name="tipoEntrega" value="RECOGER" id="tipoEntregaRecoger" ${empty datos.tipoEntrega || datos.tipoEntrega == 'RECOGER' ? 'checked' : ''}>
                                    Recoger en la cafeteria (envio gratis)
                                </label>
                                <label>
                                    <input type="radio" name="tipoEntrega" value="DOMICILIO" id="tipoEntregaDomicilio" ${datos.tipoEntrega == 'DOMICILIO' ? 'checked' : ''}>
                                    Entrega a domicilio
                                </label>
                            </div>
                        </div>
                        <div class="campo" data-validar="direccion">
                            <label for="direccion">Direccion <span class="indicador-validacion"></span></label>
                            <input type="text" id="direccion" name="direccion" value="${fn:escapeXml(datos.direccion)}" placeholder="Obligatoria solo para entrega a domicilio">
                        </div>
                        <div class="campo">
                            <label for="notas">Notas adicionales (opcional)</label>
                            <textarea id="notas" name="notas" placeholder="Ej. Sin azucar, sin hielo, etc.">${fn:escapeXml(datos.notas)}</textarea>
                        </div>
                        <button type="button" class="boton" id="botonSiguientePaso">Siguiente: metodo de pago &rarr;</button>
                    </div>
                </div>

                <div class="paso-checkout-tarjeta tarjeta" id="pasoCheckout2">
                    <div class="paso-checkout-encabezado">
                        <span class="numero">2</span>
                        <h3 style="margin:0;">Metodo de pago</h3>
                    </div>
                    <div class="paso-checkout-contenido">
                        <div class="opciones-radio">
                            <label>
                                <input type="radio" name="metodoPago" value="TARJETA" ${empty datos.metodoPago || datos.metodoPago == 'TARJETA' ? 'checked' : ''}>
                                <span class="opcion-radio-texto">Tarjeta de credito / debito</span>
                                <span class="opcion-radio-icono">&#128179;</span>
                            </label>
                            <label>
                                <input type="radio" name="metodoPago" value="TRANSFERENCIA" ${datos.metodoPago == 'TRANSFERENCIA' ? 'checked' : ''}>
                                <span class="opcion-radio-texto">Transferencia bancaria</span>
                                <span class="opcion-radio-icono">&#127974;</span>
                            </label>
                            <label>
                                <input type="radio" name="metodoPago" value="CONTRA_ENTREGA" ${datos.metodoPago == 'CONTRA_ENTREGA' ? 'checked' : ''}>
                                <span class="opcion-radio-texto">Pago contra entrega</span>
                                <span class="opcion-radio-icono">&#128181;</span>
                            </label>
                        </div>
                        <p class="ayuda" style="margin-top:10px;">&#9888; Este sistema no procesa pagos reales: no hay integracion con ningun banco ni pasarela de pago (Stripe, PayPal, etc.). "Tarjeta de credito/debito" es un pago SIMULADO solo para fines de prueba; no se te pedira ni se guardara ningun dato de tarjeta.</p>
                        <button type="button" class="boton secundario" id="botonVolverPaso" style="margin-top:14px;">&larr; Volver a datos de envio</button>
                    </div>
                </div>
            </div>

            <div>
                <div class="tarjeta resumen-pedido border-beam">
                    <h3>Resumen del pedido</h3>
                    <dl>
                        <dt>Subtotal</dt>
                        <dd>$${resumen.subtotalFormateado}</dd>
                        <dt>Envio</dt>
                        <dd>$${resumen.envioFormateado}</dd>
                        <dt class="total-final">Total</dt>
                        <dd class="total-final odometro">$${resumen.totalFormateado}</dd>
                    </dl>
                    <button type="submit" class="boton boton-ripple" style="width:100%; margin-top:14px;">Confirmar compra</button>
                </div>
            </div>
        </div>
    </form>
</div>

<%@ include file="_footer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/js/efectos.js" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/checkout.js" defer></script>
</body>
</html>
