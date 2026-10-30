// Formulario de reserva: solo se muestran los vehiculos del cliente seleccionado
document.addEventListener('DOMContentLoaded', function () {
    const cliente = document.getElementById('cliente');
    const vehiculo = document.getElementById('vehiculo');
    if (!cliente || !vehiculo) {
        return;
    }
    function filtrarVehiculos() {
        for (const opcion of vehiculo.options) {
            if (opcion.value === '') {
                continue; //Opcion "Seleccione..."
            }
            const delCliente = opcion.getAttribute('data-cliente') === cliente.value;
            opcion.hidden = !delCliente;
            if (!delCliente && opcion.selected) {
                vehiculo.value = '';
            }
        }
    }
    cliente.addEventListener('change', filtrarVehiculos);
    filtrarVehiculos();
});
