<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cafe Don Bosco - Ticket</title>
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
            <%@ include file="../_ticket-contenido.jspf" %>
            <div class="acciones-ticket no-imprimir">
                <button type="button" class="boton" onclick="window.print()">Imprimir ticket</button>
                <a class="boton secundario" href="${pageContext.request.contextPath}/api/tickets/${venta.tokenTicket}/pdf" target="_blank" rel="noopener">Descargar PDF oficial</a>
                <button type="button" class="boton secundario" id="botonCompartirWhatsApp"
                        data-context-path="${pageContext.request.contextPath}" data-token="${venta.tokenTicket}">
                    Compartir por WhatsApp
                </button>
                <a class="boton secundario" href="${pageContext.request.contextPath}/tienda">Volver al inicio</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="_footer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/js/efectos.js" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/ticket.js" defer></script>
</body>
</html>
