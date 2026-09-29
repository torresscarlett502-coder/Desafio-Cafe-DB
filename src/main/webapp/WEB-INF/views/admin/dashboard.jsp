<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Panel administrativo | Cafe Don Bosco</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-shell.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-dashboard.css">
</head>
<body>
<div class="app">
    <%@ include file="_sidebar.jspf" %>

    <main class="main">
        <%@ include file="_topbar.jspf" %>

        <div class="content">
            <div class="welcome">
                <h1>&#161;${saludo}, ${fn:escapeXml(usuario.nombre)}! &#9749;</h1>
                <p>Todo listo para atender a nuestros clientes.</p>
            </div>

            <div class="columns">
                <div>
                    <section class="stats">
                        <article class="stat">
                            <span class="stat-icon">&#9749;</span>
                            <div>
                                <small>Productos disponibles</small>
                                <b>${resumen.productosDisponibles}</b>
                                <c:choose>
                                    <c:when test="${not empty resumen.productosStockBajo}">
                                        <span class="warn">&#9888; ${resumen.productosStockBajo.size()} con stock bajo</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="up">Catalogo activo</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </article>
                        <article class="stat">
                            <span class="stat-icon">&#128722;</span>
                            <div>
                                <small>Ventas del dia</small>
                                <b>$${resumen.ventasHoyTotalFormateado}</b>
                                <span class="up">${resumen.ventasHoyCantidad} pedidos hoy</span>
                            </div>
                        </article>
                        <article class="stat">
                            <span class="stat-icon">&#9635;</span>
                            <div>
                                <small>Total de ventas</small>
                                <b>$${resumen.ingresosTotalesFormateado}</b>
                                <span class="up">$${resumen.totalVentasMesFormateado} este mes</span>
                            </div>
                        </article>
                    </section>

                    <section class="hero">
                        <em>Cafe Don Bosco</em>
                        <h2>El mejor cafe,<br>siempre contigo</h2>
                        <p>Disfruta de nuestra seleccion de productos<br>hechos con pasion y calidad.</p>
                        <a href="${pageContext.request.contextPath}/admin/productos">Ver catalogo &rarr;</a>
                    </section>

                    <section class="panel" id="productos">
                        <div class="panel-title">
                            <h3>&#9733; &nbsp; Productos destacados</h3>
                            <a href="${pageContext.request.contextPath}/admin/productos">Ver todos &rarr;</a>
                        </div>
                        <c:choose>
                            <c:when test="${empty destacados}">
                                <p class="estado-vacio">Todavia no hay productos activos.</p>
                            </c:when>
                            <c:otherwise>
                                <div class="products">
                                    <c:forEach var="producto" items="${destacados}">
                                        <article class="product">
                                            <c:choose>
                                                <c:when test="${not empty producto.imagen}">
                                                    <div class="pic" style="background-image:url('${pageContext.request.contextPath}${fn:escapeXml(producto.imagen)}')"></div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="pic pic-vacia">&#9749;</div>
                                                </c:otherwise>
                                            </c:choose>
                                            <h4>${fn:escapeXml(producto.nombre)}</h4>
                                            <small>${fn:escapeXml(producto.categoriaNombre)}</small>
                                            <div class="price">$${producto.precioFormateado}</div>
                                            <c:choose>
                                                <c:when test="${producto.disponible}">
                                                    <div class="stock">&#9679; Disponible</div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="stock" style="color:#b23b2f;">&#9679; Agotado</div>
                                                </c:otherwise>
                                            </c:choose>
                                            <a class="detail" href="${pageContext.request.contextPath}/admin/productos">Ver detalle</a>
                                        </article>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </div>

                <aside class="right">
                    <section class="panel sales" id="ventas">
                        <div class="panel-title">
                            <h3>&#9638; &nbsp; Ventas recientes</h3>
                            <a href="${pageContext.request.contextPath}/admin/historial-ventas">Ver historial &rarr;</a>
                        </div>
                        <c:choose>
                            <c:when test="${empty resumen.ventasRecientes}">
                                <p class="estado-vacio">Todavia no hay ventas registradas.</p>
                            </c:when>
                            <c:otherwise>
                                <table>
                                    <thead>
                                    <tr>
                                        <th># Venta</th>
                                        <th>Fecha</th>
                                        <th>Total</th>
                                        <th>Estado</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="venta" items="${resumen.ventasRecientes}">
                                        <tr>
                                            <td><b>#000${venta.id}</b></td>
                                            <td>${venta.fechaFormateada}</td>
                                            <td>$${venta.totalFormateado}</td>
                                            <td>${venta.estado}</td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </c:otherwise>
                        </c:choose>
                    </section>

                    <section class="panel">
                        <div class="panel-title"><h3>&#9889; &nbsp; Accesos rapidos</h3></div>
                        <div class="quick">
                            <a href="${pageContext.request.contextPath}/admin/venta-nueva">
                                <span class="ico">&#128722;</span>
                                <span><b>Nueva venta</b><small>Registrar una venta</small></span>
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/productos">
                                <span class="ico">&#9635;</span>
                                <span><b>Ver productos</b><small>Explorar catalogo</small></span>
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/historial-ventas">
                                <span class="ico">&#9201;</span>
                                <span><b>Historial de ventas</b><small>Consultar ventas anteriores</small></span>
                            </a>
                            <button type="button" class="abrir-cerrar-sesion">
                                <span class="ico">&#8618;</span>
                                <span><b>Cerrar sesion</b><small>Salir del sistema</small></span>
                            </button>
                        </div>
                    </section>

                    <c:if test="${not empty resumen.productosStockBajo}">
                        <section class="panel">
                            <div class="panel-title"><h3>&#9888; &nbsp; Stock bajo</h3></div>
                            <div class="lowstock">
                                <c:forEach var="producto" items="${resumen.productosStockBajo}">
                                    <div class="item">
                                        <b>${fn:escapeXml(producto.nombre)}</b>
                                        <span>${producto.stock} u. (min ${producto.stockMinimo})</span>
                                    </div>
                                </c:forEach>
                            </div>
                        </section>
                    </c:if>

                    <div class="signature">Cafe Don Bosco<small>Tradicion que se disfruta</small></div>
                </aside>
            </div>
        </div>
    </main>
</div>
<script src="${pageContext.request.contextPath}/assets/js/admin-shell.js"></script>
</body>
</html>
