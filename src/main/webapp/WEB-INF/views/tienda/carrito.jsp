<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cafe Don Bosco - Carrito</title>
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
    <div class="seccion-titulo" style="margin-top:0;">
        <div>
            <h2>Tu carrito de compras</h2>
            <p>Revisa tus productos antes de continuar con la compra.</p>
        </div>
        <a href="${pageContext.request.contextPath}/tienda/menu">&larr; Seguir comprando</a>
    </div>

    <c:if test="${not empty error}">
        <div class="mensaje-error">${error}</div>
    </c:if>

    <c:choose>
        <c:when test="${empty resumen.items}">
            <div class="estado-vacio">
                <div style="font-size:2.5rem;">&#128722;</div>
                <p>Tu carrito esta vacio.</p>
                <p class="ayuda">Agrega productos desde nuestro menu para comenzar.</p>
                <a class="boton" href="${pageContext.request.contextPath}/tienda/menu">Ver el menu</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="layout-carrito">
                <div class="tarjeta">
                    <c:forEach var="item" items="${resumen.items}">
                        <div class="pos-producto">
                            <div class="fila-carrito-item">
                                <c:choose>
                                    <c:when test="${not empty item.imagen}">
                                        <div class="imagen-producto" style="background-image:url('${pageContext.request.contextPath}${fn:escapeXml(item.imagen)}');background-size:cover;background-position:center;"></div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="imagen-producto" aria-hidden="true">&#9749;</div>
                                    </c:otherwise>
                                </c:choose>
                                <div>
                                    <strong>${fn:escapeXml(item.nombreProducto)}</strong><br>
                                    <c:if test="${not empty item.opciones}">
                                        <span class="ayuda">
                                            <c:forEach var="opcion" items="${item.opciones}" varStatus="est">${fn:escapeXml(opcion.nombreOpcion)}<c:if test="${not est.last}">, </c:if></c:forEach>
                                        </span><br>
                                    </c:if>
                                    <span class="ayuda">$${item.precioUnitarioConOpcionesFormateado} c/u</span>
                                </div>
                            </div>
                            <div class="controles-cantidad">
                                <form method="post" action="${pageContext.request.contextPath}/tienda/carrito">
                                    <input type="hidden" name="accion" value="decrementar">
                                    <input type="hidden" name="claveLinea" value="${item.claveLinea}">
                                    <button type="submit" aria-label="Disminuir cantidad de ${fn:escapeXml(item.nombreProducto)}">-</button>
                                </form>
                                <span aria-label="Cantidad: ${item.cantidad}">${item.cantidad}</span>
                                <form method="post" action="${pageContext.request.contextPath}/tienda/carrito">
                                    <input type="hidden" name="accion" value="incrementar">
                                    <input type="hidden" name="claveLinea" value="${item.claveLinea}">
                                    <button type="submit" aria-label="Aumentar cantidad de ${fn:escapeXml(item.nombreProducto)}">+</button>
                                </form>
                            </div>
                            <strong>$${item.subtotalFormateado}</strong>
                            <form method="post" action="${pageContext.request.contextPath}/tienda/carrito">
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="claveLinea" value="${item.claveLinea}">
                                <button type="submit" class="boton peligro pequeno" aria-label="Eliminar ${fn:escapeXml(item.nombreProducto)} del carrito">&#128465;</button>
                            </form>
                        </div>
                    </c:forEach>

                    <form method="post" action="${pageContext.request.contextPath}/tienda/carrito" style="margin-top:14px;" id="formVaciarCarrito">
                        <input type="hidden" name="accion" value="vaciar">
                        <button type="button" class="boton secundario pequeno" id="botonVaciarCarrito">Vaciar carrito</button>
                    </form>
                </div>

                <div class="tarjeta resumen-pedido border-beam">
                    <h3>Resumen del pedido</h3>
                    <dl>
                        <dt>Subtotal (${resumen.cantidadUnidades} productos)</dt>
                        <dd>$${resumen.subtotalFormateado}</dd>
                        <dt>Envio</dt>
                        <dd>$${resumen.envioFormateado}</dd>
                        <dt class="total-final">Total</dt>
                        <dd class="total-final odometro">$${resumen.totalFormateado}</dd>
                    </dl>
                    <a class="boton boton-ripple" style="width:100%; margin-top:14px; text-align:center;" href="${pageContext.request.contextPath}/tienda/checkout">Proceder al checkout &rarr;</a>
                </div>
            </div>

            <dialog id="modalConfirmarVaciado" class="modal-confirmacion">
                <p>&iquest;Vaciar el carrito? Se eliminaran todos los productos que agregaste.</p>
                <div class="modal-confirmacion-acciones">
                    <button type="button" class="boton secundario" id="botonCancelarVaciado">Cancelar</button>
                    <button type="button" class="boton peligro" id="botonConfirmarVaciado">Si, vaciar</button>
                </div>
            </dialog>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="_footer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/js/efectos.js" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/carrito.js" defer></script>
</body>
</html>
