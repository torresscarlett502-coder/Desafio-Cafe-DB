<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Cafe Don Bosco - Historial de ventas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-shell.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-historial.css">
</head>
<body>
<div class="app">
    <%@ include file="_sidebar.jspf" %>

    <main class="main">
        <%@ include file="_topbar.jspf" %>

        <div class="content">
            <div class="encabezado-productos">
                <h1>Historial de ventas</h1>
                <p>Ventas presenciales y pedidos web, todos en un mismo lugar.</p>
            </div>

            <section class="stats">
                <article class="stat">
                    <div class="stat-label">Ventas totales (hoy)</div>
                    <strong>$${ventasHoyTotalFormateado}</strong>
                </article>
                <article class="stat">
                    <div class="stat-label">Transacciones (hoy)</div>
                    <strong>${ventasHoyCantidad} ventas</strong>
                    <c:if test="${ventasHoyAnuladas > 0}">
                        <small>${ventasHoyAnuladas} ticket${ventasHoyAnuladas == 1 ? '' : 's'} anulado${ventasHoyAnuladas == 1 ? '' : 's'}</small>
                    </c:if>
                </article>
                <article class="stat">
                    <div class="stat-label">Ticket promedio (hoy)</div>
                    <strong>$${ticketPromedioFormateado}</strong>
                    <c:if test="${ventasHoyCantidad > 0}">
                        <small>Prom. ${itemsPromedio} items/orden</small>
                    </c:if>
                </article>
                <article class="stat">
                    <div class="stat-label">Metodo principal (hoy)</div>
                    <strong>${metodoPrincipal}</strong>
                    <c:if test="${ventasHoyCantidad > 0}">
                        <small>${porcentajeMetodoPrincipal}% de las ventas de hoy</small>
                    </c:if>
                </article>
            </section>

            <c:choose>
                <c:when test="${empty ventas}">
                    <p class="estado-vacio">Todavia no hay ventas registradas.</p>
                </c:when>
                <c:otherwise>
                    <div class="toolbar">
                        <div class="search-wrap">
                            <span>&#9906;</span>
                            <input type="text" id="buscarVenta" placeholder="Buscar por # de venta o cliente...">
                        </div>
                        <select id="filtroMetodo" class="select-filtro">
                            <option value="todos">Todos los metodos</option>
                            <option value="EFECTIVO">Efectivo</option>
                            <option value="TARJETA">Tarjeta</option>
                            <option value="TRANSFERENCIA">Transferencia</option>
                        </select>
                        <select id="filtroEstado" class="select-filtro">
                            <option value="todos">Todos los estados</option>
                            <option value="ENTREGADO">Completado</option>
                            <option value="CANCELADO">Anulado</option>
                        </select>
                        <button type="button" class="boton-exportar" id="botonExportar">&#9635; Exportar CSV</button>
                    </div>

                    <div class="panel">
                        <table class="tabla-productos">
                            <thead>
                            <tr>
                                <th># Venta</th>
                                <th>Fecha</th>
                                <th>Hora</th>
                                <th>Origen</th>
                                <th>Cliente</th>
                                <th>Met. pago</th>
                                <th>Items</th>
                                <th>Total</th>
                                <th>Estado</th>
                                <th>Acciones</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="venta" items="${ventas}">
                                <c:set var="cliente" value="${empty venta.nombreCliente ? 'Mostrador' : venta.nombreCliente}"/>
                                <tr class="fila-venta"
                                    data-busqueda="#000${venta.id} ${fn:toLowerCase(fn:escapeXml(cliente))}"
                                    data-metodo="${venta.metodoPago}"
                                    data-estado="${venta.estado}"
                                    data-ticket="#000${venta.id}"
                                    data-fecha="${venta.fechaFormateada}"
                                    data-hora="${venta.horaFormateada}"
                                    data-origen="${venta.tipoVenta}"
                                    data-cliente="${fn:escapeXml(cliente)}"
                                    data-items="${fn:escapeXml(venta.descripcionItems)}"
                                    data-total="${venta.totalFormateado}">
                                    <td class="ticket-id">#000${venta.id}</td>
                                    <td>${venta.fechaFormateada}</td>
                                    <td>${venta.horaFormateada}</td>
                                    <td>${venta.tipoVenta}</td>
                                    <td>${fn:escapeXml(cliente)}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${venta.metodoPago == 'EFECTIVO'}">
                                                <span class="pago pago-efectivo">&#128181; Efectivo</span>
                                            </c:when>
                                            <c:when test="${venta.metodoPago == 'TARJETA'}">
                                                <span class="pago pago-tarjeta">&#128179; Tarjeta</span>
                                            </c:when>
                                            <c:when test="${venta.metodoPago == 'TRANSFERENCIA'}">
                                                <span class="pago pago-transferencia">&#127974; Transferencia</span>
                                            </c:when>
                                            <c:otherwise>${venta.metodoPago}</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="items">
                                        <c:choose>
                                            <c:when test="${empty venta.descripcionItems}">&mdash;</c:when>
                                            <c:otherwise>${fn:escapeXml(venta.descripcionItems)}</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="total-celda">$${venta.totalFormateado}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${venta.estado == 'CANCELADO'}">
                                                <span class="badge badge-anulado">Anulado</span>
                                            </c:when>
                                            <c:when test="${venta.estado == 'ENTREGADO'}">
                                                <span class="badge badge-completado">Completado</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-en-curso">${venta.estado}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="acciones-fila">
                                            <a class="icono-accion" title="Ver ticket" href="${pageContext.request.contextPath}/admin/ticket?id=${venta.id}">&#9673;</a>
                                            <c:if test="${venta.estado != 'CANCELADO'}">
                                                <a class="icono-accion" title="Imprimir" href="${pageContext.request.contextPath}/admin/ticket?id=${venta.id}">&#9635;</a>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                        <p class="sin-resultados" id="sinResultadosTabla" style="display:none;">No se encontraron ventas.</p>
                        <div class="bottom">
                            <span id="contadorRegistros"></span>
                            <div class="pagination" id="paginacion"></div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </main>
</div>
<script src="${pageContext.request.contextPath}/assets/js/admin-shell.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/admin-historial.js"></script>
</body>
</html>
