<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<div class="container">
    <div class="row g-4">
        <div class="col-md-5">
            <div class="bg-verde text-white rounded-4 p-4 h-100">
                <h1 class="h3 fw-bold">Contacto</h1>
                <p>¿Tiene dudas sobre un pedido o un producto? Escríbanos.</p>
                <p><i class="bi bi-geo-alt"></i> Portales, Guatemala</p>
                <p><i class="bi bi-telephone"></i> 5555-0000</p>
                <p><i class="bi bi-envelope"></i> contacto@lacosecha.com</p>
                <p class="mb-0"><i class="bi bi-clock"></i> Lunes a sábado, 8:00 a 18:00</p>
            </div>
        </div>
        <div class="col-md-7">
            <div class="bg-white rounded-4 shadow-sm p-4">
                <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
                <form method="post" action="${ctx}/contacto" novalidate>
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <div class="mb-3"><label class="form-label">Nombre</label>
                        <input name="nombre" class="form-control" maxlength="60" required value="<c:out value='${empty nombre ? u.nombreCompleto : nombre}'/>"></div>
                    <div class="mb-3"><label class="form-label">Correo electrónico</label>
                        <input name="email" type="email" class="form-control" maxlength="120" required value="<c:out value='${empty email ? u.email : email}'/>"></div>
                    <div class="mb-3"><label class="form-label">Mensaje</label>
                        <textarea name="mensaje" class="form-control" rows="5" maxlength="1000" required><c:out value="${mensaje}"/></textarea></div>
                    <button class="btn btn-verde"><i class="bi bi-send"></i> Enviar mensaje</button>
                </form>
            </div>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
