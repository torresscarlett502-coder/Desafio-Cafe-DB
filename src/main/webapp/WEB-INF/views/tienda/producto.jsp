<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Cafe Don Bosco - ${producto.nombre}</title>
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
    <a href="${pageContext.request.contextPath}/tienda/menu">&larr; Volver al menu</a>

    <c:choose>
        <c:when test="${not empty error}">
            <div class="mensaje-error" style="margin-top:16px;">${error}</div>
        </c:when>
        <c:otherwise>
            <div class="detalle-layout" style="margin-top:20px;">
                <c:choose>
                    <c:when test="${not empty producto.imagen}">
                        <img class="detalle-imagen-foto" src="${pageContext.request.contextPath}${fn:escapeXml(producto.imagen)}" alt="${fn:escapeXml(producto.nombre)}">
                    </c:when>
                    <c:when test="${not empty gruposOpcion}">
                        <div class="beverage-layer-container" aria-hidden="true">
                            <svg id="coffeeSimulatorSVG" width="280" height="340" viewBox="0 0 280 340" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <ellipse cx="140" cy="310" rx="70" ry="12" fill="#2B1810" fill-opacity="0.15" />
                                <path d="M80 60 L95 280 C96 295 110 305 140 305 C170 305 184 295 185 280 L200 60 Z"
                                      stroke="#E5E0D8" stroke-width="4" fill="rgba(255, 255, 255, 0.2)" />
                                <path id="layerSyrup" d="M94 260 L95 280 C96 295 110 305 140 305 C170 305 184 295 185 280 L186 260 Z"
                                      fill="#3A1A05" opacity="0.5" />
                                <path id="layerMilk" d="M88 140 L94 260 L186 260 L192 140 Z"
                                      fill="#F4EFE6" opacity="0.95" />
                                <path id="layerEspresso" d="M84 90 L88 140 L192 140 L196 90 Z"
                                      fill="#4A2E1B" opacity="0.9" />
                                <path id="layerFoam" d="M80 60 L84 90 L196 90 L200 60 Z"
                                      fill="#FFFFFF" opacity="0.9" />
                                <path d="M90 70 L102 270" stroke="white" stroke-width="3" stroke-linecap="round" opacity="0.5" />
                            </svg>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="detalle-imagen" aria-hidden="true">&#9749;</div>
                    </c:otherwise>
                </c:choose>

                <div>
                    <span class="etiqueta">${fn:escapeXml(producto.categoriaNombre)}</span>
                    <h2>${fn:escapeXml(producto.nombre)}</h2>
                    <p>${fn:escapeXml(producto.descripcion)}</p>

                    <form method="post" action="${pageContext.request.contextPath}/tienda/carrito" class="form-agregar-carrito">
                        <input type="hidden" name="accion" value="agregar">
                        <input type="hidden" name="productoId" value="${producto.id}">
                        <input type="hidden" name="volver" value="${pageContext.request.contextPath}/tienda/producto?id=${producto.id}">

                        <c:if test="${not empty gruposOpcion}">
                            <div class="personalizacion-producto">
                                <c:forEach var="grupo" items="${gruposOpcion}">
                                    <fieldset class="grupo-opcion">
                                        <legend class="grupo-opcion-titulo">${fn:escapeXml(grupo.nombre)}<c:if test="${grupo.obligatorio}"> *</c:if></legend>
                                        <div class="pill-opciones">
                                            <c:forEach var="opcion" items="${grupo.opciones}" varStatus="est">
                                                <label class="pill-opcion">
                                                    <c:choose>
                                                        <c:when test="${grupo.seleccionMultiple}">
                                                            <input type="checkbox" name="opcionId" value="${opcion.id}"
                                                                   data-grupo-nombre="${fn:escapeXml(grupo.nombre)}" data-opcion-nombre="${fn:escapeXml(opcion.nombre)}"
                                                                   data-precio-adicional="${opcion.precioAdicional}">
                                                        </c:when>
                                                        <c:otherwise>
                                                            <input type="radio" name="opcionId_g${grupo.id}" value="${opcion.id}"
                                                                   data-grupo-nombre="${fn:escapeXml(grupo.nombre)}" data-opcion-nombre="${fn:escapeXml(opcion.nombre)}"
                                                                   data-precio-adicional="${opcion.precioAdicional}"
                                                                   ${est.first ? 'checked' : ''}>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <span>${fn:escapeXml(opcion.nombre)}<c:if test="${opcion.precioAdicional > 0}"> (+$${opcion.precioAdicionalFormateado})</c:if></span>
                                                </label>
                                            </c:forEach>
                                        </div>
                                    </fieldset>
                                </c:forEach>
                            </div>
                        </c:if>

                        <div class="detalle-precio odometro" id="detallePrecio" data-precio-base="${producto.precio}" aria-live="polite">$${producto.precioFormateado}</div>

                        <label for="cantidad">Cantidad</label>
                        <div class="selector-cantidad">
                            <button type="button" data-cantidad-decrementar aria-label="Disminuir cantidad">&minus;</button>
                            <input type="number" id="cantidad" name="cantidad" value="1" min="1" max="20" aria-label="Cantidad a agregar">
                            <button type="button" data-cantidad-incrementar aria-label="Aumentar cantidad">+</button>
                        </div>
                        <button type="submit" class="boton" ${producto.disponible ? '' : 'disabled'}>&#128722; Agregar al carrito</button>
                        <c:if test="${not producto.disponible}">
                            <p class="mensaje-error" role="alert">Este producto esta agotado en este momento.</p>
                        </c:if>
                    </form>

                    <div class="info-secundaria">
                        <div class="item">
                            <span class="icono-info">&#9673;</span>
                            <div>
                                <span class="etiqueta-info">Disponibilidad</span>
                                <c:choose>
                                    <c:when test="${producto.disponible}"><span class="valor-info disponible">Disponible</span></c:when>
                                    <c:otherwise><span class="valor-info no-disponible">Agotado</span></c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="item">
                            <span class="icono-info">&#9749;</span>
                            <div>
                                <span class="etiqueta-info">Categoria</span>
                                <span class="valor-info">${fn:escapeXml(producto.categoriaNombre)}</span>
                            </div>
                        </div>
                        <div class="item">
                            <span class="icono-info">&#9201;</span>
                            <div>
                                <span class="etiqueta-info">Tiempo de preparacion</span>
                                <span class="valor-info">${empty producto.tiempoPreparacion ? 'No especificado' : producto.tiempoPreparacion}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <c:if test="${not empty relacionados}">
                <div class="seccion-titulo">
                    <h2>Productos relacionados</h2>
                </div>
                <div class="grid-productos">
                    <c:forEach var="producto" items="${relacionados}">
                        <%@ include file="_tarjeta-producto.jspf" %>
                    </c:forEach>
                </div>
            </c:if>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="_modal-vista-rapida.jspf" %>
<%@ include file="_footer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/js/efectos.js" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/catalogo.js" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/producto.js" defer></script>
</body>
</html>
