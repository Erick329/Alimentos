<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<c:set var="car" value="${sessionScope.carrito}"/>
<div class="container">
    <h1 class="h3 fw-bold mb-3"><i class="bi bi-bag-check"></i> Finalizar compra</h1>
    <c:forEach var="a" items="${avisos}"><div class="alert alert-warning small"><c:out value="${a}"/></div></c:forEach>
    <c:if test="${not empty error}"><div class="alert alert-danger"><i class="bi bi-x-octagon"></i> <c:out value="${error}"/></div></c:if>
    <form method="post" action="${ctx}/cliente/checkout" id="formCheckout" autocomplete="off">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <div class="row g-4">
            <div class="col-lg-7">
                <div class="bg-white rounded-4 shadow-sm p-4 mb-3">
                    <h2 class="h5 fw-bold"><span class="paso">1</span> Datos de envío</h2>
                    <p class="mb-2 small text-muted">Cliente: <strong><c:out value="${u.nombreCompleto}"/></strong> · <c:out value="${u.email}"/></p>
                    <label class="form-label" for="direccion">Dirección de entrega *</label>
                    <textarea id="direccion" name="direccion" class="form-control" rows="2" minlength="10" maxlength="200" required
                              placeholder="Calle, número, zona, municipio y referencia"><c:out value="${direccion}"/></textarea>
                </div>
                <div class="bg-white rounded-4 shadow-sm p-4">
                    <h2 class="h5 fw-bold"><span class="paso">2</span> Método de pago</h2>
                    <div class="form-check border rounded-3 p-3 ps-5 mb-2">
                        <input class="form-check-input" type="radio" name="metodo" id="mTarjeta" value="TARJETA" ${metodo == 'TARJETA' ? 'checked' : ''}>
                        <label class="form-check-label fw-semibold" for="mTarjeta"><i class="bi bi-credit-card"></i> Tarjeta de crédito o débito</label>
                        <div id="datosTarjeta" class="row g-2 mt-2">
                            <div class="col-12"><input name="tarjeta" class="form-control" inputmode="numeric" maxlength="23" placeholder="Número de tarjeta"></div>
                            <div class="col-6"><input name="vencimiento" class="form-control" maxlength="5" placeholder="MM/AA"></div>
                            <div class="col-6"><input name="cvv" class="form-control" inputmode="numeric" maxlength="4" placeholder="CVV"></div>
                            <div class="col-12 small text-muted"><i class="bi bi-lock"></i> Pasarela de pruebas: use 4111 1111 1111 1111 (aprobada) o 4000 0000 0000 0002 (rechazada). No guardamos los datos de su tarjeta.</div>
                        </div>
                    </div>
                    <div class="form-check border rounded-3 p-3 ps-5 mb-2">
                        <input class="form-check-input" type="radio" name="metodo" id="mTransf" value="TRANSFERENCIA" ${metodo == 'TRANSFERENCIA' ? 'checked' : ''}>
                        <label class="form-check-label fw-semibold" for="mTransf"><i class="bi bi-bank"></i> Transferencia o depósito bancario</label>
                        <div class="small text-muted">El pedido queda pendiente hasta confirmar el pago.</div>
                    </div>
                    <div class="form-check border rounded-3 p-3 ps-5">
                        <input class="form-check-input" type="radio" name="metodo" id="mEntrega" value="EFECTIVO" ${metodo == 'EFECTIVO' ? 'checked' : ''}>
                        <label class="form-check-label fw-semibold" for="mEntrega"><i class="bi bi-cash-coin"></i> Efectivo contra entrega</label>
                        <div class="small text-muted">Pague en efectivo al recibir su pedido.</div>
                    </div>
                </div>
            </div>
            <div class="col-lg-5">
                <div class="bg-white rounded-4 shadow-sm p-4 sticky-lg-top" style="top: 80px">
                    <h2 class="h5 fw-bold"><span class="paso">3</span> Resumen del pedido</h2>
                    <ul class="list-unstyled small">
                        <c:forEach var="it" items="${car.items}">
                            <li class="d-flex justify-content-between border-bottom py-1">
                                <span>${it.cantidad} × <c:out value="${it.producto.nombre}"/></span>
                                <span>Q <fmt:formatNumber value="${it.subtotal}" pattern="#,##0.00"/></span></li>
                        </c:forEach>
                    </ul>
                    <%@ include file="/WEB-INF/vistas/comun/resumen-carrito.jspf" %>
                    <button class="btn btn-naranja btn-lg w-100 mt-3" id="btnPagar"><i class="bi bi-shield-lock"></i> Confirmar y pagar</button>
                    <a href="${ctx}/carrito" class="btn btn-link w-100 btn-sm">Modificar carrito</a>
                </div>
            </div>
        </div>
    </form>
</div>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
