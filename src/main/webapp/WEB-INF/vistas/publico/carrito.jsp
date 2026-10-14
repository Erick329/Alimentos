<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<c:set var="car" value="${sessionScope.carrito}"/>
<div class="container">
    <h1 class="h3 fw-bold mb-3"><i class="bi bi-cart3"></i> Carrito de compras</h1>
    <c:choose>
        <c:when test="${car.vacio}">
            <div class="text-center bg-white rounded-4 shadow-sm py-5">
                <i class="bi bi-cart-x fs-1 text-muted"></i>
                <p class="mt-2">Su carrito está vacío.</p>
                <a href="${ctx}/catalogo" class="btn btn-verde">Ir al catálogo</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <div class="col-lg-8">
                    <div class="bg-white rounded-4 shadow-sm p-3">
                        <div class="table-responsive">
                        <table class="table align-middle mb-0">
                            <thead><tr><th>Producto</th><th class="text-end">Precio</th><th style="width:150px">Cantidad</th><th class="text-end">Subtotal</th><th></th></tr></thead>
                            <tbody>
                            <c:forEach var="it" items="${car.items}">
                                <tr>
                                    <td><span class="me-2 fs-4">${it.producto.icono}</span><a href="${ctx}/producto?id=${it.producto.idProducto}"><c:out value="${it.producto.nombre}"/></a></td>
                                    <td class="text-end">Q <fmt:formatNumber value="${it.producto.precio}" pattern="#,##0.00"/></td>
                                    <td>
                                        <form method="post" action="${ctx}/carrito" class="d-flex gap-1">
                                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                            <input type="hidden" name="accion" value="actualizar">
                                            <input type="hidden" name="idProducto" value="${it.producto.idProducto}">
                                            <input type="number" name="cantidad" value="${it.cantidad}" min="0" max="${it.producto.stock}" class="form-control form-control-sm" aria-label="Cantidad">
                                            <button class="btn btn-sm btn-outline-secondary" title="Actualizar"><i class="bi bi-arrow-repeat"></i></button>
                                        </form>
                                    </td>
                                    <td class="text-end fw-semibold">Q <fmt:formatNumber value="${it.subtotal}" pattern="#,##0.00"/></td>
                                    <td class="text-end">
                                        <form method="post" action="${ctx}/carrito">
                                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                            <input type="hidden" name="accion" value="eliminar">
                                            <input type="hidden" name="idProducto" value="${it.producto.idProducto}">
                                            <button class="btn btn-sm btn-outline-danger" title="Quitar"><i class="bi bi-trash"></i></button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                        </div>
                        <div class="d-flex justify-content-between mt-3">
                            <a href="${ctx}/catalogo" class="btn btn-outline-success btn-sm"><i class="bi bi-arrow-left"></i> Seguir comprando</a>
                            <form method="post" action="${ctx}/carrito" data-confirmar="¿Vaciar el carrito?">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="vaciar">
                                <button class="btn btn-link btn-sm text-danger">Vaciar carrito</button>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="col-lg-4">
                    <div class="bg-white rounded-4 shadow-sm p-4">
                        <h2 class="h5 fw-bold">Resumen</h2>
                        <%@ include file="/WEB-INF/vistas/comun/resumen-carrito.jspf" %>
                        <a href="${ctx}/cliente/checkout" class="btn btn-naranja w-100 mt-3 btn-lg">Finalizar compra</a>
                        <c:if test="${empty u}"><p class="small text-muted text-center mt-2 mb-0">Se le pedirá iniciar sesión o crear una cuenta.</p></c:if>
                    </div>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
