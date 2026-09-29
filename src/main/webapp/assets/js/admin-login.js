function togglePassword() {
  const input = document.getElementById('password');
  input.type = input.type === 'password' ? 'text' : 'password';
}

function validarLogin(e) {
  const correo = document.getElementById('correo').value.trim();
  const pass = document.getElementById('password').value.trim();
  const err = document.getElementById('error');
  if (!correo || !pass) {
    e.preventDefault();
    err.textContent = 'Por favor ingresa tu correo y contrasena.';
    err.style.display = 'block';
    return false;
  }
  err.style.display = 'none';
  return true;
}
