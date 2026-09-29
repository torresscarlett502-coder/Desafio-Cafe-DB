<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Cafe Don Bosco - Productos</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-shell.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-productos.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">
<div class="app">
    <%@ include file="_sidebar.jspf" %>

    <main class="main">
        <%@ include file="_topbar.jspf" %>

        <div class="content">
            <div class="encabezado-productos">
                <div>
                    <h1>Gestion de productos</h1>
                    <p>Catalogo completo con stock e informacion administrativa.</p>
                </div>
                <button type="button" class="boton-admin" id="botonNuevoProducto">+ Anadir nuevo producto</button>
            </div>

            <div class="columnas-productos">
                <div>
                    <div class="filtros">
                        <div class="search-wrap">
                            <span>&#9906;</span>
                            <input type="text" id="buscarProducto" placeholder="Buscar por nombre o codigo...">
                        </div>
                        <select id="filtroCategoria" class="select-categoria">
                            <option value="todas">Todas las categorias</option>
                            <c:forEach var="categoria" items="${categorias}">
                                <option value="${categoria.id}">${fn:escapeXml(categoria.nombre)}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="panel">
                        <c:choose>
                            <c:when test="${empty productos}">
                                <p class="estado-vacio">Todavia no hay productos registrados.</p>
                            </c:when>
                            <c:otherwise>
                                <table class="tabla-productos">
                                    <thead>
                                    <tr>
                                        <th>Codigo</th>
                                        <th>Foto</th>
                                        <th>Nombre</th>
                                        <th>Categoria</th>
                                        <th>Precio</th>
                                        <th>Stock</th>
                                        <th>Estado</th>
                                        <th>Acciones</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach var="producto" items="${productos}">
                                        <c:set var="codigo"><fmt:formatNumber value="${producto.id}" minIntegerDigits="4" groupingUsed="false"/></c:set>
                                        <tr class="fila-producto" data-nombre="${fn:escapeXml(producto.nombre)}" data-codigo="PRD-${codigo}" data-categoria-id="${producto.categoriaId}">
                                            <td class="codigo">PRD-${codigo}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty producto.imagen}">
                                                        <div class="miniatura" style="background-image:url('${pageContext.request.contextPath}${fn:escapeXml(producto.imagen)}')"></div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="miniatura pic-vacia">&#9749;</div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${fn:escapeXml(producto.nombre)}</td>
                                            <td class="categoria-celda">${fn:escapeXml(producto.categoriaNombre)}</td>
                                            <td>$${producto.precioFormateado}</td>
                                            <td>${producto.stock}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${!producto.activo}">
                                                        <span class="badge badge-inactivo">Inactivo</span>
                                                    </c:when>
                                                    <c:when test="${producto.stock <= 0}">
                                                        <span class="badge badge-agotado">Agotado</span>
                                                    </c:when>
                                                    <c:when test="${producto.stock <= producto.stockMinimo}">
                                                        <span class="badge badge-bajo">Stock bajo</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge-disponible">Disponible</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div class="acciones-fila">
                                                    <button type="button" class="icono-accion boton-editar" title="Editar"
                                                            data-id="${producto.id}"
                                                            data-nombre="${fn:escapeXml(producto.nombre)}"
                                                            data-categoria-id="${producto.categoriaId}"
                                                            data-descripcion="${fn:escapeXml(producto.descripcion)}"
                                                            data-precio="${producto.precio}"
                                                            data-imagen="${fn:escapeXml(producto.imagen)}"
                                                            data-tiempo="${producto.tiempoPreparacionMinutos}"
                                                            data-stock-minimo="${producto.stockMinimo}"
                                                            data-activo="${producto.activo}">&#9998;</button>
                                                    <button type="button" class="icono-accion peligro boton-estado"
                                                            title="${producto.activo ? 'Desactivar' : 'Activar'}"
                                                            data-id="${producto.id}"
                                                            data-nombre="${fn:escapeXml(producto.nombre)}"
                                                            data-activo="${producto.activo}">${producto.activo ? '&#128465;' : '&#8635;'}</button>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                                <p class="sin-resultados" id="sinResultadosTabla" style="display:none;">No se encontraron productos.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <aside>
                    <section class="panel">
                        <div class="panel-title"><h3>&#9888; &nbsp; Stock critico</h3></div>
                        <c:set var="hayStockCritico" value="false"/>
                        <div class="lowstock">
                            <c:forEach var="producto" items="${productos}">
                                <c:if test="${producto.activo && producto.stock <= producto.stockMinimo}">
                                    <c:set var="hayStockCritico" value="true"/>
                                    <div class="item">
                                        <b>${fn:escapeXml(producto.nombre)}</b>
                                        <span>${producto.stock} u. (min ${producto.stockMinimo})</span>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                        <c:if test="${!hayStockCritico}">
                            <p class="estado-vacio">Ningun producto activo esta por debajo de su stock minimo.</p>
                        </c:if>
                    </section>
                </aside>
            </div>
        </div>
    </main>
</div>

<dialog id="modalNuevoProducto" class="dialogo-producto">
    <h3>Anadir nuevo producto</h3>
    <div class="error-formulario"></div>
    <form id="formNuevoProducto">
        <div class="form-campo">
            <label for="nuevoNombre">Nombre</label>
            <input type="text" id="nuevoNombre" name="nombre" required>
        </div>
        <div class="form-fila">
            <div class="form-campo">
                <label for="nuevaCategoria">Categoria</label>
                <select id="nuevaCategoria" name="categoriaId" required>
                    <c:forEach var="categoria" items="${categorias}">
                        <option value="${categoria.id}">${fn:escapeXml(categoria.nombre)}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-campo">
                <label for="nuevoPrecio">Precio ($)</label>
                <input type="number" id="nuevoPrecio" name="precio" min="0" step="0.01" required>
            </div>
        </div>
        <div class="form-campo">
            <label for="nuevaDescripcion">Descripcion</label>
            <textarea id="nuevaDescripcion" name="descripcion"></textarea>
        </div>
        <div class="form-campo">
            <label for="nuevaImagen">Imagen (ruta o URL, opcional)</label>
            <input type="text" id="nuevaImagen" name="imagen" placeholder="/assets/img/producto.jpg">
        </div>
        <div class="form-fila">
            <div class="form-campo">
                <label for="nuevoStockInicial">Stock inicial</label>
                <input type="number" id="nuevoStockInicial" name="stockInicial" min="0" step="1">
            </div>
            <div class="form-campo">
                <label for="nuevoStockMinimo">Stock minimo</label>
                <input type="number" id="nuevoStockMinimo" name="stockMinimo" min="0" step="1">
            </div>
        </div>
        <div class="form-campo">
            <label for="nuevoTiempo">Tiempo de preparacion (minutos)</label>
            <input type="number" id="nuevoTiempo" name="tiempoPreparacionMinutos" min="0" step="1">
        </div>
        <div class="form-check">
            <input type="checkbox" id="nuevoActivo" name="activo" checked>
            <label for="nuevoActivo">Producto activo (visible en catalogo y punto de venta)</label>
        </div>
        <div class="dialogo-acciones">
            <button type="button" class="boton-admin secundario" id="botonCancelarNuevo">Cancelar</button>
            <button type="submit" class="boton-admin">Guardar producto</button>
        </div>
    </form>
</dialog>

<dialog id="modalEditarProducto" class="dialogo-producto">
    <h3>Editar producto</h3>
    <div class="error-formulario"></div>
    <form id="formEditarProducto">
        <div class="form-campo">
            <label for="editarNombre">Nombre</label>
            <input type="text" id="editarNombre" name="nombre" required>
        </div>
        <div class="form-fila">
            <div class="form-campo">
                <label for="editarCategoria">Categoria</label>
                <select id="editarCategoria" name="categoriaId" required>
                    <c:forEach var="categoria" items="${categorias}">
                        <option value="${categoria.id}">${fn:escapeXml(categoria.nombre)}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-campo">
                <label for="editarPrecio">Precio ($)</label>
                <input type="number" id="editarPrecio" name="precio" min="0" step="0.01" required>
            </div>
        </div>
        <div class="form-campo">
            <label for="editarDescripcion">Descripcion</label>
            <textarea id="editarDescripcion" name="descripcion"></textarea>
        </div>
        <div class="form-campo">
            <label for="editarImagen">Imagen (ruta o URL, opcional)</label>
            <input type="text" id="editarImagen" name="imagen" placeholder="/assets/img/producto.jpg">
        </div>
        <div class="form-campo">
            <label for="editarStockMinimo">Stock minimo</label>
            <input type="number" id="editarStockMinimo" name="stockMinimo" min="0" step="1">
        </div>
        <div class="form-campo">
            <label for="editarTiempo">Tiempo de preparacion (minutos)</label>
            <input type="number" id="editarTiempo" name="tiempoPreparacionMinutos" min="0" step="1">
        </div>
        <div class="form-check">
            <input type="checkbox" id="editarActivo" name="activo">
            <label for="editarActivo">Producto activo (visible en catalogo y punto de venta)</label>
        </div>
        <div class="dialogo-acciones">
            <button type="button" class="boton-admin secundario" id="botonCancelarEditar">Cancelar</button>
            <button type="submit" class="boton-admin">Guardar cambios</button>
        </div>
    </form>
</dialog>

<dialog id="modalConfirmarEstado" class="modal-confirmacion">
    <p id="mensajeConfirmarEstado"></p>
    <div class="modal-confirmacion-acciones">
        <button type="button" class="boton-admin secundario" id="botonCancelarEstado">Volver</button>
        <button type="button" class="boton-admin peligro" id="botonConfirmarEstado">Confirmar</button>
    </div>
</dialog>

<script src="${pageContext.request.contextPath}/assets/js/admin-shell.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/admin-productos.js"></script>
</body>
</html>
