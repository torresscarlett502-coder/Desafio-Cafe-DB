<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cafe Don Bosco - Pedido confirmado</title>
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
    <c:choose>
        <c:when test="${not empty error}">
            <div class="mensaje-error">${error}</div>
        </c:when>
        <c:otherwise>
            <div class="confirmacion-caja">
                <div class="icono-check">&#10003;</div>
                <h2>&iexcl;Pedido confirmado!</h2>
                <p>Gracias por tu compra. Tu pedido ha sido procesado correctamente.</p>

                <%@ include file="_stepper-estado.jspf" %>

                <div class="detalle-pedido-grid">
                    <div class="item">
                        <span class="icono-info">&#129534;</span>
                        <div>
                            <span class="etiqueta-info">Numero de pedido</span>
                            #000${venta.id}
                        </div>
                    </div>
                    <div class="item">
                        <span class="icono-info">&#128197;</span>
                        <div>
                            <span class="etiqueta-info">Fecha</span>
                            ${venta.fechaFormateada}
                        </div>
                    </div>
                    <div class="item">
                        <span class="icono-info">&#128337;</span>
                        <div>
                            <span class="etiqueta-info">Hora</span>
                            ${venta.horaFormateada}
                        </div>
                    </div>
                    <div class="item">
                        <span class="icono-info">&#128176;</span>
                        <div>
                            <span class="etiqueta-info">Total</span>
                            <span class="odometro">$${venta.totalFormateado}</span>
                        </div>
                    </div>
                </div>

                <p>
                    <c:choose>
                        <c:when test="${venta.tipoEntrega == 'DOMICILIO'}">Tu pedido sera entregado a domicilio.</c:when>
                        <c:otherwise>Tu pedido sera preparado en un maximo de 10 minutos.</c:otherwise>
                    </c:choose>
                </p>

                <a class="boton" href="${pageContext.request.contextPath}/tienda/ticket?token=${venta.tokenTicket}">Ver ticket</a>
                <a class="boton secundario" href="${pageContext.request.contextPath}/tienda" style="margin-left:10px;">Volver al inicio</a>
            </div>

            <div class="cta-tienda">
                <h2>Gracias por preferirnos</h2>
                <p>Tu apoyo nos motiva a seguir compartiendo lo mejor del cafe.</p>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="_footer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/js/efectos.js" defer></script>
</body>
</html>
