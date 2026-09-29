const menu=document.querySelector('.menu'),side=document.querySelector('.sidebar');
menu.addEventListener('click',()=>side.classList.toggle('open'));
document.addEventListener('click',e=>{if(innerWidth<=700&&!side.contains(e.target)&&!menu.contains(e.target))side.classList.remove('open')});

(function () {
    var botones = document.querySelectorAll('#botonCerrarSesion, .abrir-cerrar-sesion');
    var modal = document.getElementById('modalCerrarSesion');
    var botonCancelar = document.getElementById('botonCancelarCerrarSesion');
    var botonConfirmar = document.getElementById('botonConfirmarCerrarSesion');
    if (!botones.length || !modal || !botonConfirmar) {
        return;
    }
    botones.forEach(function (boton) {
        boton.addEventListener('click', function () { modal.showModal(); });
    });
    if (botonCancelar) {
        botonCancelar.addEventListener('click', function () { modal.close(); });
    }
    botonConfirmar.addEventListener('click', function () {
        window.location.href = (modal.dataset.contextPath || '') + '/logout';
    });
})();
