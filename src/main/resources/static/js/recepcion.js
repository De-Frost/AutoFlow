// Estado inicial: si se marca "Si" en danos u objetos personales, la descripcion es obligatoria
document.addEventListener('DOMContentLoaded', function () {
    function configurar(nombreRadio, idDescripcion) {
        const descripcion = document.getElementById(idDescripcion);
        const opciones = document.querySelectorAll('input[name="' + nombreRadio + '"]');
        if (!descripcion || opciones.length === 0) {
            return;
        }
        function actualizar() {
            const si = document.querySelector('input[name="' + nombreRadio + '"]:checked').value === 'true';
            descripcion.required = si;
            descripcion.disabled = !si;
            descripcion.classList.toggle('d-none', !si);
        }
        opciones.forEach(function (o) {
            o.addEventListener('change', actualizar);
        });
        actualizar();
    }
    configurar('conDanos', 'danosVisibles');
    configurar('conObjetos', 'objetosPersonales');
});
