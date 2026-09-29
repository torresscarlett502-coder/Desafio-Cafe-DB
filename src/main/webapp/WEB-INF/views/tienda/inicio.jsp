<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cafe Don Bosco</title>
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
    <p class="etiqueta-superior">Cafe Don Bosco</p>
    <h2>El cafe que hace especial tu momento.</h2>
    <p>Cafe, bebidas, postres y comida. Todo lo que necesitas para disfrutar el mejor sabor.</p>
    <a class="boton" href="${pageContext.request.contextPath}/tienda/menu" style="margin-top:16px; background-color:var(--color-cobre-claro); color:var(--color-cafe-oscuro); font-weight:bold;">Ver menu &rarr;</a>
</section>

<div class="contenedor-tienda">
    <div class="seccion-titulo">
        <h2>&#9733; Nuestros favoritos</h2>
        <a href="${pageContext.request.contextPath}/tienda/menu">Ver todo &rarr;</a>
    </div>
    <div class="grid-productos">
        <c:forEach var="producto" items="${destacados}">
            <%@ include file="_tarjeta-producto.jspf" %>
        </c:forEach>
    </div>

    <div class="seccion-titulo">
        <h2>&#9889; Acciones rapidas</h2>
    </div>
    <div class="grid-acciones">
        <a class="tarjeta-accion" href="${pageContext.request.contextPath}/tienda/menu">
            <span class="icono">&#128722;</span>
            <span><strong>Explorar el menu</strong><span>Ver todo el catalogo</span></span>
        </a>
        <a class="tarjeta-accion" href="${pageContext.request.contextPath}/tienda/carrito">
            <span class="icono">&#128717;</span>
            <span><strong>Ver mi carrito</strong><span>Revisar lo que llevas</span></span>
        </a>
        <a class="tarjeta-accion" href="${pageContext.request.contextPath}/tienda/nosotros">
            <span class="icono">&#9825;</span>
            <span><strong>Conocenos</strong><span>Nuestra historia</span></span>
        </a>
    </div>

    <div class="seccion-titulo">
        <h2>Categorias</h2>
    </div>
    <div class="grid-categorias">
        <c:forEach var="categoria" items="${categorias}">
            <a class="tarjeta-categoria" href="${pageContext.request.contextPath}/tienda/menu?categoria=${categoria.id}">
                <span class="icono">&#9749;</span>
                ${fn:escapeXml(categoria.nombre)}
            </a>
        </c:forEach>
    </div>

    <div class="cta-tienda border-beam">
        <h2>&#191;Listo para pedir algo?</h2>
        <a class="boton" href="${pageContext.request.contextPath}/tienda/menu">Ver productos</a>
    </div>
</div>

<%@ include file="_modal-vista-rapida.jspf" %>
<%@ include file="_footer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/js/efectos.js" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/catalogo.js" defer></script>
</body>
</html>
