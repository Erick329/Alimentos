<%@ include file="/WEB-INF/vistas/admin/_encabezado.jspf" %>
<c:set var="rango" value="desde=${desde}&hasta=${hasta}"/>
<div class="d-flex flex-column flex-md-row justify-content-between align-items-md-end gap-2 mb-3">
    <div><h1 class="h3 fw-bold mb-0">Reportes para la toma de decisiones</h1>
        <p class="text-muted small mb-0">Pedidos cancelados no se cuentan como ventas.</p></div>
    <form method="get" class="d-flex gap-2 align-items-end">
        <div><label class="form-label small mb-0">Desde</label><input type="date" name="desde" value="${desde}" class="form-control form-control-sm"></div>
        <div><label class="form-label small mb-0">Hasta</label><input type="date" name="hasta" value="${hasta}" class="form-control form-control-sm"></div>
        <button class="btn btn-sm btn-verde"><i class="bi bi-funnel"></i> Aplicar</button>
    </form>
</div>

<div class="row g-3 mb-3">
    <div class="col-6 col-xl"><div class="kpi"><div class="kpi-t">Ventas</div><div class="kpi-v">Q <fmt:formatNumber value="${resumen.ventas}" pattern="#,##0.00"/></div></div></div>
    <div class="col-6 col-xl"><div class="kpi"><div class="kpi-t">Pedidos</div><div class="kpi-v">${resumen.pedidos}</div></div></div>
    <div class="col-6 col-xl"><div class="kpi"><div class="kpi-t">Ticket promedio</div><div class="kpi-v">Q <fmt:formatNumber value="${resumen.ticket}" pattern="#,##0.00"/></div></div></div>
    <div class="col-6 col-xl"><div class="kpi"><div class="kpi-t">Unidades vendidas</div><div class="kpi-v">${resumen.unidades}</div></div></div>
    <div class="col-6 col-xl"><div class="kpi"><div class="kpi-t">Clientes que compraron</div><div class="kpi-v">${resumen.clientes}</div></div></div>
</div>

<div class="row g-3 mb-3">
    <div class="col-xl-8"><div class="caja h-100">
        <div class="d-flex justify-content-between"><h2 class="h6 fw-bold">R1. Ventas por período (Q)</h2><a class="small" href="${ctx}/admin/reportes?${rango}&exportar=ventas"><i class="bi bi-download"></i> CSV</a></div>
        <canvas id="gVentas" height="120"></canvas></div></div>
    <div class="col-xl-4"><div class="caja h-100">
        <div class="d-flex justify-content-between"><h2 class="h6 fw-bold">R7. Pedidos por estado</h2><a class="small" href="${ctx}/admin/reportes?${rango}&exportar=estados"><i class="bi bi-download"></i> CSV</a></div>
        <canvas id="gEstados" height="220"></canvas></div></div>
</div>

<div class="row g-3 mb-3">
    <div class="col-xl-7"><div class="caja h-100">
        <div class="d-flex justify-content-between"><h2 class="h6 fw-bold">R2. Top 10 productos más vendidos (unidades)</h2><a class="small" href="${ctx}/admin/reportes?${rango}&exportar=top"><i class="bi bi-download"></i> CSV</a></div>
        <canvas id="gTop" height="170"></canvas></div></div>
    <div class="col-xl-5"><div class="caja h-100">
        <div class="d-flex justify-content-between"><h2 class="h6 fw-bold">R4. Ventas por categoría (Q)</h2><a class="small" href="${ctx}/admin/reportes?${rango}&exportar=categorias"><i class="bi bi-download"></i> CSV</a></div>
        <canvas id="gCategorias" height="220"></canvas></div></div>
</div>

<div class="row g-3">
    <div class="col-xl-4"><div class="caja h-100">
        <div class="d-flex justify-content-between"><h2 class="h6 fw-bold">R6. Clientes frecuentes</h2><a class="small" href="${ctx}/admin/reportes?${rango}&exportar=clientes"><i class="bi bi-download"></i> CSV</a></div>
        <table class="table table-sm small mb-0"><thead><tr><th>Cliente</th><th class="text-center">Pedidos</th><th class="text-end">Total</th></tr></thead><tbody>
            <c:forEach var="f" items="${clientes}"><tr><td><c:out value="${f.etiqueta}"/></td><td class="text-center">${f.cantidad}</td><td class="text-end">Q <fmt:formatNumber value="${f.monto}" pattern="#,##0.00"/></td></tr></c:forEach>
            <c:if test="${empty clientes}"><tr><td colspan="3" class="text-muted">Sin datos en el período.</td></tr></c:if>
        </tbody></table></div></div>
    <div class="col-xl-4"><div class="caja h-100">
        <h2 class="h6 fw-bold">R3. Productos de baja rotación</h2>
        <table class="table table-sm small mb-0"><thead><tr><th>Producto</th><th class="text-center">Vendidas</th><th class="text-end">Stock</th></tr></thead><tbody>
            <c:forEach var="f" items="${bajaRotacion}"><tr><td><c:out value="${f.etiqueta}"/></td><td class="text-center">${f.cantidad}</td><td class="text-end"><fmt:formatNumber value="${f.monto}" pattern="#,##0"/></td></tr></c:forEach>
        </tbody></table>
        <p class="small text-muted mt-2 mb-0">Candidatos a promoción o descuento.</p></div></div>
    <div class="col-xl-4"><div class="caja h-100">
        <div class="d-flex justify-content-between"><h2 class="h6 fw-bold">R5. Inventario bajo mínimo</h2><a class="small" href="${ctx}/admin/reportes?exportar=inventario"><i class="bi bi-download"></i> CSV</a></div>
        <table class="table table-sm small mb-0"><thead><tr><th>Producto</th><th class="text-center">Stock</th><th class="text-end">Mínimo</th></tr></thead><tbody>
            <c:forEach var="p" items="${bajoStock}"><tr><td><c:out value="${p.nombre}"/></td><td class="text-center text-danger fw-bold">${p.stock}</td><td class="text-end">${p.stockMinimo}</td></tr></c:forEach>
            <c:if test="${empty bajoStock}"><tr><td colspan="3" class="text-muted">Sin alertas.</td></tr></c:if>
        </tbody></table>
        <p class="small text-muted mt-2 mb-0">Reabastecer para no perder ventas.</p></div></div>
</div>

<div class="caja mt-3 table-responsive">
    <div class="d-flex justify-content-between"><h2 class="h6 fw-bold">R8. Detalle de pedidos del período</h2><a class="small" href="${ctx}/admin/reportes?${rango}&exportar=pedidos"><i class="bi bi-download"></i> CSV</a></div>
    <div style="max-height: 360px; overflow-y: auto">
    <table class="table table-sm small mb-0 align-middle">
        <thead class="sticky-top bg-white"><tr><th>#</th><th>Fecha</th><th>Cliente</th><th>NIT</th><th>Pago</th><th>Estado</th><th class="text-end">Total</th></tr></thead>
        <tbody>
        <c:forEach var="p" items="${pedidos}">
            <tr class="${p.estado == 'CANCELADO' ? 'text-muted text-decoration-line-through' : ''}"><td><a href="${ctx}/admin/pedidos?id=${p.idPedido}">#${p.idPedido}</a></td><td class="text-nowrap"><fmt:formatDate value="${p.fechaPedido}" pattern="dd/MM/yy HH:mm"/></td>
                <td><c:out value="${p.clienteNombre}"/></td><td><c:out value="${p.clienteNit}"/></td><td>${p.metodoPago}</td>
                <td><span class="badge text-bg-${p.colorEstado}">${p.estadoTexto}</span></td><td class="text-end">Q <fmt:formatNumber value="${p.total}" pattern="#,##0.00"/></td></tr>
        </c:forEach>
        <c:if test="${empty pedidos}"><tr><td colspan="7" class="text-muted">Sin pedidos en el período.</td></tr></c:if>
        </tbody>
    </table></div>
    <div class="d-flex justify-content-end border-top pt-2 mt-1 fw-bold">TOTAL VENDIDO (sin cancelados):&nbsp;<span class="text-success">Q <fmt:formatNumber value="${totalVendido}" pattern="#,##0.00"/></span></div>
</div>

<div class="row g-3 mt-0">
    <div class="col-xl-7"><div class="caja h-100 table-responsive">
        <div class="d-flex justify-content-between"><h2 class="h6 fw-bold">R9. Inventario actual</h2><a class="small" href="${ctx}/admin/reportes?exportar=stock"><i class="bi bi-download"></i> CSV</a></div>
        <div style="max-height: 360px; overflow-y: auto">
        <table class="table table-sm small mb-0">
            <thead class="sticky-top bg-white"><tr><th>Producto</th><th>Categoría</th><th class="text-end">Stock</th><th class="text-end">Precio</th><th class="text-end">Valor</th></tr></thead>
            <tbody>
            <c:forEach var="p" items="${inventario}"><tr class="${p.bajoStock ? 'table-warning' : ''}"><td><c:out value="${p.nombre}"/></td><td><c:out value="${p.categoriaNombre}"/></td><td class="text-end">${p.stock}</td>
                <td class="text-end text-nowrap">Q <fmt:formatNumber value="${p.precio}" pattern="#,##0.00"/></td><td class="text-end text-nowrap">Q <fmt:formatNumber value="${p.precio * p.stock}" pattern="#,##0.00"/></td></tr></c:forEach>
            </tbody>
        </table></div>
        <div class="d-flex justify-content-end gap-4 border-top pt-2 mt-1 fw-bold"><span>TOTAL EN STOCK: ${totalUnidades} unidades</span><span class="text-success">Q <fmt:formatNumber value="${valorInventario}" pattern="#,##0.00"/></span></div>
    </div></div>
    <div class="col-xl-5"><div class="caja h-100">
        <div class="d-flex justify-content-between"><h2 class="h6 fw-bold">R10. Lotes vencidos o por vencer (30 días)</h2><a class="small" href="${ctx}/admin/reportes?exportar=lotes"><i class="bi bi-download"></i> CSV</a></div>
        <table class="table table-sm small mb-0"><thead><tr><th>Producto</th><th>Lote</th><th class="text-end">Cant.</th><th>Vence</th><th>Estado</th></tr></thead><tbody>
            <c:forEach var="l" items="${lotesPorVencer}"><tr class="${l.vencido ? 'table-danger' : 'table-warning'}"><td><c:out value="${l.productoNombre}"/></td><td class="text-nowrap"><c:out value="${l.numeroLote}"/></td><td class="text-end">${l.cantidad}</td>
                <td class="text-nowrap"><fmt:formatDate value="${l.fechaVencimiento}" pattern="dd/MM/yy"/></td><td>${l.estadoVencimiento}</td></tr></c:forEach>
            <c:if test="${empty lotesPorVencer}"><tr><td colspan="5" class="text-muted">Sin lotes por vencer.</td></tr></c:if>
        </tbody></table>
        <p class="small text-muted mt-2 mb-0">Promocionar antes de que venzan o dar de baja los vencidos en Inventario.</p></div></div>
</div>

<%-- Datos de las gráficas en JSON (lo lee js/reportes.js) --%>
<script type="application/json" id="datosGraficas">${jsonGraficas}</script>
<script src="${ctx}/lib/chartjs/chart.umd.js"></script>
<script src="${ctx}/js/reportes.js"></script>
<%@ include file="/WEB-INF/vistas/admin/_pie.jspf" %>
