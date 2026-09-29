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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-shell.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tienda.css">
</head>
<body>
<div class="app">
    <%@ include file="_sidebar.jspf" %>

    <main class="main">
        <%@ include file="_topbar.jspf" %>

        <div class="content">
            <c:if test="${esNueva}">
                <div class="mensaje-exito">Venta registrada correctamente.</div>
            </c:if>

            <c:choose>
                <c:when test="${not empty error}">
                    <div class="mensaje-error">${error}</div>
                </c:when>
                <c:otherwise>
                    <%@ include file="../_ticket-contenido.jspf" %>
                    <div class="acciones-ticket no-imprimir">
                        <button type="button" class="boton" onclick="window.print()">Imprimir / Descargar PDF</button>
                        <a class="boton secundario" href="${pageContext.request.contextPath}/admin/venta-nueva">Nueva venta</a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </main>
</div>
<script src="${pageContext.request.contextPath}/assets/js/admin-shell.js"></script>
</body>
</html>
