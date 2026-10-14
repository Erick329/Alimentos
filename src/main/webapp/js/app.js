// Funciones generales del sitio (sin JavaScript en línea para cumplir la política CSP)
document.addEventListener('DOMContentLoaded', function () {

    // Confirmación antes de enviar formularios con data-confirmar="mensaje"
    document.querySelectorAll('form[data-confirmar]').forEach(function (f) {
        f.addEventListener('submit', function (e) {
            var msg = f.getAttribute('data-confirmar');
            if (msg && !confirm(msg)) e.preventDefault();
        });
    });

    // Checkout: mostrar datos de tarjeta solo si se elige ese método
    var datos = document.getElementById('datosTarjeta');
    if (datos) {
        var actualizar = function () {
            var tarjeta = document.getElementById('mTarjeta').checked;
            datos.style.display = tarjeta ? '' : 'none';
            datos.querySelectorAll('input').forEach(function (i) { i.required = tarjeta; });
        };
        document.querySelectorAll('input[name="metodo"]').forEach(function (r) { r.addEventListener('change', actualizar); });
        actualizar();
        // Evita doble envío del pago
        document.getElementById('formCheckout').addEventListener('submit', function () {
            var b = document.getElementById('btnPagar');
            setTimeout(function () { b.disabled = true; b.textContent = 'Procesando pago...'; }, 0);
        });
    }
});
