<%@ include file="/WEB-INF/vistas/admin/_encabezado.jspf" %>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 fw-bold mb-0">Pedido #${pedido.idPedido}</h1>
    <a href="${ctx}/admin/pedidos" class="btn btn-outline-secondary btn-sm"><i class="bi bi-arrow-left"></i> Volver</a>
</div>
<div class="row g-3">
    <div class="col-xl-8"><div class="caja"><%@ include file="/WEB-INF/vistas/comun/detalle-pedido.jspf" %></div></div>
    <div class="col-xl-4">
        <div class="caja">
            <h2 class="h6 fw-bold">Cambiar estado</h2>
            <c:choose>
                <c:when test="${empty siguientes}"><p class="small text-muted mb-0">El pedido está <strong>${pedido.estadoTexto}</strong>; no admite más cambios.</p></c:when>
                <c:otherwise>
                    <p class="small">Estado actual: <span class="badge text-bg-${pedido.colorEstado}">${pedido.estadoTexto}</span></p>
                    <c:forEach var="s" items="${siguientes}">
                        <form method="post" action="${ctx}/admin/pedidos" class="mb-2" data-confirmar="${s == 'CANCELADO' ? '¿Cancelar el pedido? Los productos regresarán al inventario.' : ''}">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="id" value="${pedido.idPedido}">
                            <input type="hidden" name="estado" value="${s}">
                            <button class="btn w-100 ${s == 'CANCELADO' ? 'btn-outline-danger' : 'btn-verde'}">
                                <i class="bi ${s == 'CANCELADO' ? 'bi-x-circle' : 'bi-arrow-right-circle'}"></i> Marcar como ${fn:replace(s, '_', ' ')}</button>
                        </form>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/vistas/admin/_pie.jspf" %>
