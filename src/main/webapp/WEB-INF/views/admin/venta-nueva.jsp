<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Cafe Don Bosco - Nueva venta</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-shell.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-venta-nueva.css">
</head>
<body>
<div class="app">
    <%@ include file="_sidebar.jspf" %>

    <main class="main">
        <%@ include file="_topbar.jspf" %>

        <div class="content">
            <c:if test="${not empty error}">
                <div class="mensaje-error">${error}</div>
            </c:if>

            <h1>Punto de venta / Nueva venta</h1>

            <div class="pos-grid">
            <div class="catalog">
                <div class="search-wrap">
                    <span>&#9906;</span>
                    <input type="text" id="buscarProducto" placeholder="Buscar por nombre o codigo...">
                </div>

                <div class="categories">
                    <button type="button" class="cat active" data-cat="Todas">Todas</button>
                    <c:forEach var="categoria" items="${categorias}">
                        <button type="button" class="cat" data-cat="${fn:escapeXml(categoria.nombre)}">${fn:escapeXml(categoria.nombre)}</button>
                    </c:forEach>
                </div>

                <div class="products">
                    <c:forEach var="producto" items="${productos}">
                        <c:if test="${producto.activo}">
                            <article class="product" data-nombre="${fn:escapeXml(producto.nombre)}" data-sku="PRD-<fmt:formatNumber value="${producto.id}" minIntegerDigits="4" groupingUsed="false"/>" data-categoria="${fn:escapeXml(producto.categoriaNombre)}">
                                <c:choose>
                                    <c:when test="${not empty producto.imagen}">
                                        <div class="pic" style="background-image:url('${pageContext.request.contextPath}${fn:escapeXml(producto.imagen)}')"></div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="pic pic-vacia">&#9749;</div>
                                    </c:otherwise>
                                </c:choose>
                                <div class="product-body">
                                    <h3>${fn:escapeXml(producto.nombre)}</h3>
                                    <div class="sku">
                                        PRD-<fmt:formatNumber value="${producto.id}" minIntegerDigits="4" groupingUsed="false"/> &middot;
                                        <c:choose>
                                            <c:when test="${producto.stock <= 0}">
                                                <span class="stock-txt soldout">Sin stock</span>
                                            </c:when>
                                            <c:when test="${producto.stock <= producto.stockMinimo}">
                                                <span class="stock-txt low">Stock: ${producto.stock}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="stock-txt">Stock: ${producto.stock}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="price">$${producto.precioFormateado}</div>
                                    <form method="post" action="${pageContext.request.contextPath}/admin/venta-nueva">
                                        <input type="hidden" name="accion" value="agregar">
                                        <input type="hidden" name="productoId" value="${producto.id}">
                                        <button type="submit" class="add" ${producto.stock <= 0 ? 'disabled' : ''}>${producto.stock <= 0 ? 'No disponible' : '+ Agregar'}</button>
                                    </form>
                                </div>
                            </article>
                        </c:if>
                    </c:forEach>
                    <p class="sin-resultados" id="sinResultadosProductos" style="display:none;">No se encontraron productos.</p>
                </div>
            </div>

            <aside class="order">
                <div class="order-head">
                    <div>
                        <h2>Detalle de la orden</h2>
                        <span>${fechaHoy} &middot; Mostrador</span>
                    </div>
                    <b>Venta presencial</b>
                </div>

                <p class="cliente-info">Cliente: consumidor final (venta de mostrador)</p>

                <c:choose>
                    <c:when test="${empty carrito.items}">
                        <div class="cart">
                            <p class="sin-resultados">La orden esta vacia. Agrega productos para iniciar la venta.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="cart">
                            <c:forEach var="item" items="${carrito.items}">
                                <div class="cart-row">
                                    <div class="cart-name">${fn:escapeXml(item.nombreProducto)}<small>${item.precioUnitarioFormateado} c/u</small></div>
                                    <div class="qty">
                                        <form method="post" action="${pageContext.request.contextPath}/admin/venta-nueva">
                                            <input type="hidden" name="accion" value="decrementar">
                                            <input type="hidden" name="claveLinea" value="${item.claveLinea}">
                                            <button type="submit">-</button>
                                        </form>
                                        <span>${item.cantidad}</span>
                                        <form method="post" action="${pageContext.request.contextPath}/admin/venta-nueva">
                                            <input type="hidden" name="accion" value="agregar">
                                            <input type="hidden" name="productoId" value="${item.productoId}">
                                            <button type="submit">+</button>
                                        </form>
                                    </div>
                                    <div class="line-total">$${item.subtotalFormateado}</div>
                                    <form method="post" action="${pageContext.request.contextPath}/admin/venta-nueva">
                                        <input type="hidden" name="accion" value="eliminar">
                                        <input type="hidden" name="claveLinea" value="${item.claveLinea}">
                                        <button type="submit" class="remove" aria-label="Quitar">&times;</button>
                                    </form>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="summary">
                            <div><span>Subtotal (${carrito.cantidadUnidades} productos)</span><b>$${carrito.subtotalFormateado}</b></div>
                        </div>
                        <div class="total">
                            <span>Total a pagar</span>
                            <strong id="totalVenta" data-total="${carrito.total}">$${carrito.totalFormateado}</strong>
                        </div>

                        <form method="post" action="${pageContext.request.contextPath}/admin/venta-nueva">
                            <input type="hidden" name="accion" value="confirmar">
                            <label class="pay-title">Metodo de pago:</label>
                            <div class="payments">
                                <input class="pago-radio" type="radio" id="pagoEfectivo" name="metodoPago" value="EFECTIVO" checked>
                                <label class="pago-pill" for="pagoEfectivo">&#128181; Efectivo</label>
                                <input class="pago-radio" type="radio" id="pagoTarjeta" name="metodoPago" value="TARJETA">
                                <label class="pago-pill" for="pagoTarjeta">&#128179; Tarjeta</label>
                                <input class="pago-radio" type="radio" id="pagoTransferencia" name="metodoPago" value="TRANSFERENCIA">
                                <label class="pago-pill" for="pagoTransferencia">&#127974; Transfer.</label>
                            </div>

                            <div class="cash-row" id="filaEfectivo">
                                <label>Monto recibido ($)
                                    <input type="number" id="montoRecibido" min="0" step="0.01" placeholder="0.00">
                                </label>
                                <div class="change">
                                    <span>Cambio a entregar</span>
                                    <b id="montoCambio">$0.00</b>
                                </div>
                            </div>

                            <button type="submit" class="checkout">Completar y cobrar ($${carrito.totalFormateado})</button>
                        </form>

                        <div class="actions">
                            <button type="button" id="botonCancelarVenta">Cancelar venta</button>
                        </div>
                        <form method="post" action="${pageContext.request.contextPath}/admin/venta-nueva" id="formVaciarVenta" style="display:none;">
                            <input type="hidden" name="accion" value="vaciar">
                        </form>
                    </c:otherwise>
                </c:choose>
            </aside>
            </div>

            <dialog id="modalCancelarVenta" class="modal-confirmacion">
                <p>&iquest;Cancelar la venta en curso? Se eliminaran todos los productos de la orden.</p>
                <div class="modal-confirmacion-acciones">
                    <button type="button" class="boton-admin secundario" id="botonCerrarCancelar">Volver</button>
                    <button type="button" class="boton-admin peligro" id="botonConfirmarCancelar">Si, cancelar</button>
                </div>
            </dialog>
        </div>
    </main>
</div>
<script src="${pageContext.request.contextPath}/assets/js/admin-shell.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/admin-venta-nueva.js"></script>
</body>
</html>
