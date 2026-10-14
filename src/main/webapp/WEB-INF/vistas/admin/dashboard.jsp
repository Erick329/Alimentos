<%@ include file="/WEB-INF/vistas/admin/_encabezado.jspf" %>
<h1 class="h3 fw-bold">Dashboard</h1>
<p class="text-muted">Bienvenido(a), <c:out value="${u.nombre}"/>. Resumen de la operación.</p>
<div class="row g-3 mb-4">
    <div class="col-6 col-xl-3"><div class="kpi"><div class="kpi-t">Ventas de hoy</div><div class="kpi-v">Q <fmt:formatNumber value="${hoy.ventas}" pattern="#,##0.00"/></div><div class="kpi-d">${hoy.pedidos} pedido(s)</div></div></div>
    <div class="col-6 col-xl-3"><div class="kpi"><div class="kpi-t">Ventas últimos 30 días</div><div class="kpi-v">Q <fmt:formatNumber value="${mes.ventas}" pattern="#,##0.00"/></div><div class="kpi-d">${mes.pedidos} pedidos · ticket Q <fmt:formatNumber value="${mes.ticket}" pattern="#,##0.00"/></div></div></div>
    <div class="col-6 col-xl-3"><div class="kpi"><div class="kpi-t">Pedidos por atender</div><div class="kpi-v text-primary">${porAtender}</div><div class="kpi-d"><a href="${ctx}/admin/pedidos?estado=PAGADO">Ver pagados</a> · <a href="${ctx}/admin/pedidos?estado=PENDIENTE">pendientes</a></div></div></div>
    <div class="col-6 col-xl-3"><div class="kpi"><div class="kpi-t">Productos bajo mínimo</div><div class="kpi-v ${fn:length(bajoStock) > 0 ? 'text-danger' : 'text-success'}">${fn:length(bajoStock)}</div><div class="kpi-d">Requieren reabastecer</div></div></div>
</div>
<div class="row g-3">
    <div class="col-xl-8">
        <div class="caja">
            <div class="d-flex justify-content-between"><h2 class="h6 fw-bold">Pedidos recientes</h2><a href="${ctx}/admin/pedidos" class="small">Ver todos</a></div>
            <div class="table-responsive">
            <table class="table table-sm align-middle mb-0">
                <thead><tr><th>#</th><th>Cliente</th><th>Fecha</th><th class="text-end">Total</th><th>Estado</th></tr></thead>
                <tbody>
                <c:forEach var="p" items="${recientes}">
                    <tr><td><a href="${ctx}/admin/pedidos?id=${p.idPedido}">#${p.idPedido}</a></td><td><c:out value="${p.clienteNombre}"/></td>
                        <td><fmt:formatDate value="${p.fechaPedido}" pattern="dd/MM HH:mm"/></td>
                        <td class="text-end">Q <fmt:formatNumber value="${p.total}" pattern="#,##0.00"/></td>
                        <td><span class="badge text-bg-${p.colorEstado}">${p.estadoTexto}</span></td></tr>
                </c:forEach>
                </tbody>
            </table>
            </div>
        </div>
    </div>
    <div class="col-xl-4">
        <div class="caja">
            <h2 class="h6 fw-bold"><i class="bi bi-exclamation-triangle text-danger"></i> Alertas de inventario</h2>
            <c:choose>
                <c:when test="${empty bajoStock}"><p class="small text-muted mb-0">Todo el inventario está sobre el mínimo.</p></c:when>
                <c:otherwise>
                    <ul class="list-group list-group-flush small">
                        <c:forEach var="p" items="${bajoStock}">
                            <li class="list-group-item d-flex justify-content-between px-0"><span><c:out value="${p.nombre}"/></span>
                                <span class="text-danger fw-semibold">${p.stock} / mín. ${p.stockMinimo}</span></li>
                        </c:forEach>
                    </ul>
                    <c:if test="${u.admin}"><a href="${ctx}/admin/inventario" class="btn btn-sm btn-verde mt-2">Registrar entrada</a></c:if>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="caja mt-3">
            <h2 class="h6 fw-bold"><i class="bi bi-calendar-x text-warning"></i> Lotes por vencer (15 días)</h2>
            <c:choose>
                <c:when test="${empty porVencer}"><p class="small text-muted mb-0">No hay lotes por vencer.</p></c:when>
                <c:otherwise>
                    <ul class="list-group list-group-flush small">
                        <c:forEach var="l" items="${porVencer}">
                            <li class="list-group-item d-flex justify-content-between px-0"><span><c:out value="${l.productoNombre}"/> · <c:out value="${l.numeroLote}"/></span>
                                <span class="${l.vencido ? 'text-danger fw-semibold' : 'text-warning-emphasis'}">${l.cantidad} u. · ${l.estadoVencimiento}</span></li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/vistas/admin/_pie.jspf" %>
