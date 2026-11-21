(() => {
    const lineas = document.getElementById('lineas-servicio');
    const agregar = document.getElementById('agregar-servicio');
    function agregarLinea() {
        if (lineas.children.length >= 50) return;
        lineas.append(document.getElementById('linea-servicio').content.cloneNode(true));
        agregar.disabled = lineas.children.length >= 50;
    }
    agregar.addEventListener('click', agregarLinea);
    lineas.addEventListener('click', event => {
        const quitar = event.target.closest('.af-quitar');
        if (quitar && lineas.children.length > 1) {
            quitar.closest('.af-linea-servicio').remove();
            agregar.disabled = false;
        }
    });
    if (!lineas.children.length) agregarLinea();
})();
