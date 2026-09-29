<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cafe Don Bosco - Nosotros</title>
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
    <p class="etiqueta-superior">Nuestra historia</p>
    <h2>Tradicion que se disfruta</h2>
    <p>Cafe Don Bosco nace de la pasion por preparar un buen cafe y compartir buenos momentos.</p>
</section>

<div class="contenedor-tienda">
    <div class="tarjeta">
        <h3>Quienes somos</h3>
        <p>Somos una cafeteria dedicada a ofrecer cafe, bebidas, postres y comida preparados con dedicacion todos los dias.</p>
    </div>
    <div class="tarjeta" style="margin-top:16px;">
        <h3>Nuestra forma de preparar el cafe</h3>
        <p>Seleccionamos cuidadosamente cada producto de nuestro menu para asegurar una experiencia consistente en cada visita.</p>
    </div>
    <div class="tarjeta" style="margin-top:16px;">
        <h3>Nuestros valores</h3>
        <p>Calidad, calidez y atencion cercana con cada cliente que nos visita, ya sea en el mostrador o desde la web.</p>
    </div>

    <div class="cta-tienda">
        <h2>&#191;Listo para pedir algo?</h2>
        <a class="boton" href="${pageContext.request.contextPath}/tienda/menu">Ver menu</a>
    </div>
</div>

<%@ include file="_footer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/js/efectos.js" defer></script>
</body>
</html>
