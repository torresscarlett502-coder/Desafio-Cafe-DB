<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cafe Don Bosco - Menu</title>
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

<section class="hero-tienda">
    <img class="bg-coffee-parallax" src="https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?q=80&amp;w=1000&amp;auto=format&amp;fit=crop" alt="" aria-hidden="true">
    <canvas id="vaporCanvas" aria-hidden="true"></canvas>
    <p class="etiqueta-superior">Nuestro menu</p>
    <h2>Descubre todos nuestros productos</h2>
    <p>Cafe, bebidas, postres y comida. Todo lo que necesitas para disfrutar el mejor sabor.</p>
    <form class="buscador" method="get" action="${pageContext.request.contextPath}/tienda/menu">
        <c:if test="${not empty categoriaSeleccionada}">
            <input type="hidden" name="categoria" value="${categoriaSeleccionada}">
        </c:if>
        <input type="search" id="buscadorEnVivo" name="buscar" placeholder="Buscar producto..." value="${busqueda}" autocomplete="off">
    </form>
</section>

<div class="contenedor-tienda">
    <div class="layout-catalogo">
        <aside class="categorias-lista">
            <h3 style="font-size:0.95rem;">Categorias</h3>
            <a href="${pageContext.request.contextPath}/tienda/menu" class="${empty categoriaSeleccionada ? 'activa' : ''}">
                <span>Todos</span><span>${totalProductos}</span>
            </a>
            <c:forEach var="categoria" items="${categorias}">
                <a href="${pageContext.request.contextPath}/tienda/menu?categoria=${categoria.id}"
                   class="${categoriaSeleccionada == categoria.id ? 'activa' : ''}">
                    <span>${fn:escapeXml(categoria.nombre)}</span><span>${categoria.cantidadProductos}</span>
                </a>
            </c:forEach>
        </aside>

        <div>
            <div class="barra-catalogo">
                <h3 style="margin:0;">Todos los productos</h3>
                <form method="get" action="${pageContext.request.contextPath}/tienda/menu" style="display:flex; gap:8px; align-items:center;">
                    <c:if test="${not empty categoriaSeleccionada}">
                        <input type="hidden" name="categoria" value="${categoriaSeleccionada}">
                    </c:if>
                    <c:if test="${not empty busqueda}">
                        <input type="hidden" name="buscar" value="${busqueda}">
                    </c:if>
                    <label for="orden" style="font-size:0.85rem;">Ordenar por:</label>
                    <select id="orden" name="orden" onchange="this.form.submit()">
                        <option value="" ${empty orden ? 'selected' : ''}>Nombre (A-Z)</option>
                        <option value="precio_menor" ${orden == 'precio_menor' ? 'selected' : ''}>Precio: menor a mayor</option>
                        <option value="precio_mayor" ${orden == 'precio_mayor' ? 'selected' : ''}>Precio: mayor a menor</option>
                    </select>
                </form>
            </div>

            <div id="resultadosCatalogo">
                <c:choose>
                    <c:when test="${empty productos}">
                        <p class="estado-vacio">No encontramos productos con esos filtros.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="grid-productos">
                            <c:forEach var="producto" items="${productos}">
                                <%@ include file="_tarjeta-producto.jspf" %>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<%@ include file="_modal-vista-rapida.jspf" %>
<%@ include file="_footer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/js/efectos.js" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/catalogo.js" defer></script>
</body>
</html>
