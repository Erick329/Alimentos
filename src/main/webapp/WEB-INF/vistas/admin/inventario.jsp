<%@ include file="/WEB-INF/vistas/admin/_encabezado.jspf" %>
<h1 class="h3 fw-bold mb-1">Inventario por lotes</h1>
<p class="text-muted small mb-3">El stock de cada producto es la suma de sus lotes activos. Las ventas descuentan primero del lote que vence antes (FEFO) y nunca de un lote vencido.</p>

<%-- Selector de producto --%>
<form method="get" action="${ctx}/admin/inventario" class="caja mb-3 d-flex flex-wrap gap-2 align-items-end">
    <div class="flex-grow-1"><label class="form-label">Producto</label>
        <select name="producto" class="form-select" onchange="this.form.submit()"><option value="0">Seleccione un producto para ver sus lotes...</option>
            <c:forEach var="p" items="${productos}"><option value="${p.idProducto}" ${p.idProducto == productoSel ? 'selected' : ''}><c:out value="${p.codigo}"/> · <c:out value="${p.nombre}"/> (stock ${p.stock})</option></c:forEach>
        </select></div>
    <noscript><button class="btn btn-outline-secondary">Ver</button></noscript>
</form>

<c:if test="${not empty producto}">
<div class="row g-3 mb-3">
    <div class="col-lg-7">
        <div class="caja table-responsive h-100">
            <h2 class="h6 fw-bold">Lotes de <c:out value="${producto.nombre}"/> <span class="badge text-bg-success ms-1">Stock: ${producto.stock}</span></h2>
            <table class="table table-sm align-middle mb-0 small">
                <thead><tr><th>Lote</th><th class="text-end">Cantidad</th><th>Vence</th><th>Estado</th><th>Ingreso</th></tr></thead>
                <tbody>
                <c:forEach var="l" items="${lotes}">
                    <tr class="${l.vencido ? 'table-danger' : (l.diasParaVencer <= 30 ? 'table-warning' : '')}">
                        <td class="fw-semibold"><c:out value="${l.numeroLote}"/></td><td class="text-end">${l.cantidad}</td>
                        <td><fmt:formatDate value="${l.fechaVencimiento}" pattern="dd/MM/yyyy"/></td>
                        <td>${l.estadoVencimiento}</td><td><fmt:formatDate value="${l.fechaIngreso}" pattern="dd/MM/yy"/></td></tr>
                </c:forEach>
                <c:if test="${empty lotes}"><tr><td colspan="5" class="text-muted">Este producto aún no tiene lotes. Registre una entrada.</td></tr></c:if>
                </tbody>
            </table>
        </div>
    </div>
    <div class="col-lg-5">
        <form method="post" action="${ctx}/admin/inventario" class="caja mb-3">
            <h2 class="h6 fw-bold text-success"><i class="bi bi-box-arrow-in-down"></i> Entrada: nuevo lote</h2>
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="tipo" value="ENTRADA">
            <input type="hidden" name="idProducto" value="${producto.idProducto}">
            <div class="row g-2 mb-2">
                <div class="col-6"><label class="form-label">Número de lote *</label><input name="numeroLote" class="form-control" maxlength="50" required placeholder="A-1003"></div>
                <div class="col-6"><label class="form-label">Cantidad *</label><input name="cantidad" type="number" min="1" class="form-control" required></div>
                <div class="col-6"><label class="form-label">Vence *</label><input name="fechaVencimiento" type="date" class="form-control" required></div>
                <div class="col-6"><label class="form-label">Motivo</label><input name="motivo" class="form-control" maxlength="150" placeholder="Factura #123"></div>
            </div>
            <button class="btn btn-verde btn-sm"><i class="bi bi-plus-lg"></i> Registrar lote</button>
        </form>
        <c:if test="${not empty lotes}">
        <form method="post" action="${ctx}/admin/inventario" class="caja">
            <h2 class="h6 fw-bold text-danger"><i class="bi bi-box-arrow-up"></i> Salida o ajuste de un lote</h2>
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="idProducto" value="${producto.idProducto}">
            <div class="row g-2 mb-2">
                <div class="col-12"><label class="form-label">Lote *</label>
                    <select name="idLote" class="form-select" required>
                        <c:forEach var="l" items="${lotes}"><option value="${l.idLote}"><c:out value="${l.numeroLote}"/> · ${l.cantidad} u. · ${l.estadoVencimiento}</option></c:forEach>
                    </select></div>
                <div class="col-6"><label class="form-label">Tipo *</label>
                    <select name="tipo" class="form-select">
                        <option value="SALIDA">Salida (merma / vencido)</option>
                        <option value="AJUSTE">Ajuste (conteo físico)</option>
                    </select></div>
                <div class="col-6"><label class="form-label">Cantidad *</label><input name="cantidad" type="number" min="0" class="form-control" required></div>
                <div class="col-12"><label class="form-label">Motivo</label><input name="motivo" class="form-control" maxlength="150" placeholder="Producto vencido, empaque dañado..."></div>
            </div>
            <div class="form-text mb-2">En <strong>Ajuste</strong> la cantidad es lo que realmente hay en el lote.</div>
            <button class="btn btn-outline-danger btn-sm"><i class="bi bi-check2"></i> Registrar</button>
        </form>
        </c:if>
    </div>
</div>
</c:if>

<div class="row g-3">
    <div class="col-lg-5">
        <div class="caja mb-3">
            <h2 class="h6 fw-bold text-warning-emphasis"><i class="bi bi-calendar-x"></i> Lotes vencidos o por vencer en 30 días (${fn:length(porVencer)})</h2>
            <ul class="list-group list-group-flush small">
                <c:forEach var="l" items="${porVencer}">
                    <li class="list-group-item px-0 d-flex justify-content-between">
                        <a href="${ctx}/admin/inventario?producto=${l.idProducto}"><c:out value="${l.productoNombre}"/> · <c:out value="${l.numeroLote}"/></a>
                        <span class="${l.vencido ? 'text-danger fw-semibold' : 'text-warning-emphasis'}">${l.cantidad} u. · ${l.estadoVencimiento}</span></li>
                </c:forEach>
                <c:if test="${empty porVencer}"><li class="list-group-item px-0 text-muted">Sin lotes por vencer.</li></c:if>
            </ul>
        </div>
        <div class="caja">
            <h2 class="h6 fw-bold text-danger"><i class="bi bi-exclamation-triangle"></i> Bajo stock mínimo (${fn:length(bajoStock)})</h2>
            <ul class="list-group list-group-flush small">
                <c:forEach var="p" items="${bajoStock}">
                    <li class="list-group-item px-0 d-flex justify-content-between"><a href="${ctx}/admin/inventario?producto=${p.idProducto}"><c:out value="${p.nombre}"/></a><span class="text-danger">${p.stock} / mín. ${p.stockMinimo}</span></li>
                </c:forEach>
                <c:if test="${empty bajoStock}"><li class="list-group-item px-0 text-muted">Sin alertas.</li></c:if>
            </ul>
        </div>
    </div>
    <div class="col-lg-7">
        <div class="caja table-responsive">
            <h2 class="h6 fw-bold">Últimos movimientos (tabla Movimientos)</h2>
            <table class="table table-sm align-middle mb-0 small">
                <thead><tr><th>Fecha</th><th>Producto</th><th>Lote</th><th>Tipo</th><th class="text-end">Cant.</th><th>Motivo</th><th>Usuario</th></tr></thead>
                <tbody>
                <c:forEach var="m" items="${movimientos}">
                    <tr><td class="text-nowrap"><fmt:formatDate value="${m.fecha}" pattern="dd/MM/yy HH:mm"/></td><td><c:out value="${m.productoNombre}"/></td><td class="text-nowrap"><c:out value="${m.numeroLote}"/></td>
                        <td><span class="badge ${m.tipo == 'ENTRADA' ? 'text-bg-success' : (m.tipo == 'SALIDA' ? 'text-bg-danger' : 'text-bg-warning')}">${m.tipo}</span></td>
                        <td class="text-end">${m.cantidad}</td><td><c:out value="${m.motivo}"/></td><td><c:out value="${m.usuarioNombre}"/></td></tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/vistas/admin/_pie.jspf" %>
