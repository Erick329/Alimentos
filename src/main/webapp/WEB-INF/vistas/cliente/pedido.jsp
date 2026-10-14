<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<div class="container">
    <c:if test="${nuevo}">
        <div class="text-center bg-white rounded-4 shadow-sm p-4 mb-3">
            <i class="bi bi-check-circle-fill text-success display-4"></i>
            <h1 class="h3 fw-bold mt-2">¡Pedido confirmado!</h1>
            <p class="mb-0">Su número de pedido es <strong>#${pedido.idPedido}</strong>. Le enviamos un comprobante a <c:out value="${pedido.clienteEmail}"/>.</p>
        </div>
    </c:if>
    <div class="bg-white rounded-4 shadow-sm p-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="h4 fw-bold mb-0">Pedido #${pedido.idPedido}</h2>
            <a href="${ctx}/cliente/pedidos" class="btn btn-outline-success btn-sm"><i class="bi bi-list"></i> Mis pedidos</a>
        </div>
        <%-- Línea de tiempo del estado --%>
        <c:if test="${pedido.estado != 'CANCELADO'}">
            <div class="linea-tiempo d-flex justify-content-between mb-4 small">
                <c:set var="alcanzado" value="true"/>
                <c:forEach var="e" items="${['PENDIENTE','PAGADO','EN_PREPARACION','ENVIADO','ENTREGADO']}">
                    <div class="text-center flex-fill ${alcanzado ? 'text-success fw-semibold' : 'text-muted'}">
                        <i class="bi ${alcanzado ? 'bi-check-circle-fill' : 'bi-circle'} fs-4"></i><br>${fn:replace(e, '_', ' ')}
                    </div>
                    <c:if test="${e == pedido.estado}"><c:set var="alcanzado" value="false"/></c:if>
                </c:forEach>
            </div>
        </c:if>
        <%@ include file="/WEB-INF/vistas/comun/detalle-pedido.jspf" %>
    </div>
</div>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
