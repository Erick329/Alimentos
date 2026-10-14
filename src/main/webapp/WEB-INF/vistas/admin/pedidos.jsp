<%@ include file="/WEB-INF/vistas/admin/_encabezado.jspf" %>
<h1 class="h3 fw-bold mb-3">Pedidos</h1>
<div class="d-flex flex-wrap gap-2 mb-3">
    <a href="${ctx}/admin/pedidos" class="btn btn-sm rounded-pill ${empty estadoSel ? 'btn-verde' : 'btn-outline-success'}">Todos</a>
    <c:forEach var="e" items="${estados}">
        <a href="${ctx}/admin/pedidos?estado=${e}" class="btn btn-sm rounded-pill ${e == estadoSel ? 'btn-verde' : 'btn-outline-success'}">${fn:replace(e, '_', ' ')}</a>
    </c:forEach>
</div>
<div class="caja table-responsive">
    <table class="table table-sm table-hover align-middle mb-0">
        <thead><tr><th>#</th><th>Fecha</th><th>Cliente</th><th class="text-center">Art.</th><th>Pago</th><th class="text-end">Total</th><th>Estado</th><th></th></tr></thead>
        <tbody>
        <c:forEach var="p" items="${pedidos}">
            <tr><td class="fw-semibold">#${p.idPedido}</td><td><fmt:formatDate value="${p.fechaPedido}" pattern="dd/MM/yyyy HH:mm"/></td>
                <td><c:out value="${p.clienteNombre}"/></td><td class="text-center">${p.totalArticulos}</td>
                <td class="small">${fn:replace(p.metodoPago, '_', ' ')}</td>
                <td class="text-end">Q <fmt:formatNumber value="${p.total}" pattern="#,##0.00"/></td>
                <td><span class="badge text-bg-${p.colorEstado}">${p.estadoTexto}</span></td>
                <td class="text-end"><a href="${ctx}/admin/pedidos?id=${p.idPedido}" class="btn btn-sm btn-outline-success">Gestionar</a></td></tr>
        </c:forEach>
        <c:if test="${empty pedidos}"><tr><td colspan="8" class="text-center text-muted py-4">No hay pedidos.</td></tr></c:if>
        </tbody>
    </table>
</div>
<%@ include file="/WEB-INF/vistas/admin/_pie.jspf" %>
