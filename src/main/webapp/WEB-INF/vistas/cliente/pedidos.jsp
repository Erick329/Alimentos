<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<div class="container">
    <h1 class="h3 fw-bold mb-3"><i class="bi bi-bag"></i> Mis pedidos</h1>
    <c:choose>
        <c:when test="${empty pedidos}">
            <div class="text-center bg-white rounded-4 shadow-sm py-5"><p>Aún no ha realizado pedidos.</p><a href="${ctx}/catalogo" class="btn btn-verde">Comprar ahora</a></div>
        </c:when>
        <c:otherwise>
            <div class="bg-white rounded-4 shadow-sm p-3 table-responsive">
                <table class="table align-middle mb-0">
                    <thead><tr><th>Pedido</th><th>Fecha</th><th>Artículos</th><th>Pago</th><th class="text-end">Total</th><th>Estado</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach var="p" items="${pedidos}">
                        <tr>
                            <td class="fw-semibold">#${p.idPedido}</td>
                            <td><fmt:formatDate value="${p.fechaPedido}" pattern="dd/MM/yyyy HH:mm"/></td>
                            <td>${p.totalArticulos}</td>
                            <td class="small">${fn:replace(p.metodoPago, '_', ' ')}</td>
                            <td class="text-end">Q <fmt:formatNumber value="${p.total}" pattern="#,##0.00"/></td>
                            <td><span class="badge text-bg-${p.colorEstado}">${p.estadoTexto}</span></td>
                            <td class="text-end"><a href="${ctx}/cliente/pedido?id=${p.idPedido}" class="btn btn-sm btn-outline-success">Ver detalle</a></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
