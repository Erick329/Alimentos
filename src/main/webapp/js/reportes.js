// Gráficas del módulo de reportes con Chart.js
document.addEventListener('DOMContentLoaded', function () {
    if (typeof Chart === 'undefined') return;
    var d = JSON.parse(document.getElementById('datosGraficas').textContent);
    var verde = '#2E7D32', naranja = '#F57C00';
    var paleta = ['#2E7D32', '#F57C00', '#1565C0', '#8E24AA', '#00897B', '#C62828', '#6D4C41', '#546E7A'];
    var q = function (v) { return 'Q ' + Number(v).toLocaleString('es-GT', { minimumFractionDigits: 2 }); };

    new Chart(document.getElementById('gVentas'), {
        type: 'bar',
        data: { labels: d.ventas.etiquetas, datasets: [{ label: 'Ventas', data: d.ventas.valores, backgroundColor: '#66BB6A', borderRadius: 4 }] },
        options: { plugins: { legend: { display: false }, tooltip: { callbacks: { label: function (c) { return q(c.raw); } } } },
                   scales: { y: { beginAtZero: true } } }
    });
    new Chart(document.getElementById('gTop'), {
        type: 'bar',
        data: { labels: d.top.etiquetas, datasets: [{ label: 'Unidades', data: d.top.valores, backgroundColor: naranja, borderRadius: 4 }] },
        options: { indexAxis: 'y', plugins: { legend: { display: false } } }
    });
    new Chart(document.getElementById('gCategorias'), {
        type: 'doughnut',
        data: { labels: d.categorias.etiquetas, datasets: [{ data: d.categorias.valores, backgroundColor: paleta }] },
        options: { plugins: { legend: { position: 'bottom' }, tooltip: { callbacks: { label: function (c) { return c.label + ': ' + q(c.raw); } } } } }
    });
    var coloresEstado = { PENDIENTE: '#FFC107', PAGADO: '#0DCAF0', EN_PREPARACION: '#0D6EFD', ENVIADO: '#343A40', ENTREGADO: verde, CANCELADO: '#DC3545' };
    new Chart(document.getElementById('gEstados'), {
        type: 'pie',
        data: { labels: d.estados.etiquetas.map(function (e) { return e.replace('_', ' '); }),
                datasets: [{ data: d.estados.valores, backgroundColor: d.estados.etiquetas.map(function (e) { return coloresEstado[e] || '#999'; }) }] },
        options: { plugins: { legend: { position: 'bottom' } } }
    });
});
