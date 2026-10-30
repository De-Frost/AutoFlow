//Muestra el stock disponible del repuesto elegido 
document.addEventListener('DOMContentLoaded', function () {
    const repuesto = document.getElementById('idRepuesto');
    if (!repuesto) { //Solo existe en la pagina de movimientos
        return;
    }
    const cantidad = document.getElementById('cantidad');
    const stock = document.getElementById('stockDisponible');

    function mostrarStock() {
        const opcion = repuesto.options[repuesto.selectedIndex];
        const disponible = opcion.dataset.stock;
        if (disponible === undefined) {
            stock.textContent = '—';
            cantidad.removeAttribute('max');
        } else {
            stock.textContent = disponible;
            cantidad.max = disponible;
        }
    }

    repuesto.addEventListener('change', mostrarStock);
    mostrarStock();
});