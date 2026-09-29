<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Cafe Don Bosco - Iniciar sesion</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-login.css">
</head>
<body>

  <div class="brand">
    <div class="logo-line">
      <svg viewBox="0 0 24 24" fill="none" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round">
        <path d="M17 8h1a4 4 0 1 1 0 8h-1"/>
        <path d="M3 8h14v9a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4Z"/>
        <line x1="6" y1="2" x2="6" y2="4"/><line x1="10" y1="2" x2="10" y2="4"/><line x1="14" y1="2" x2="14" y2="4"/>
      </svg>
      <h1>Cafe<br>Don Bosco</h1>
    </div>
    <p class="tagline">Una tradicion desde siempre</p>
    <div class="divider"></div>
    <p class="frase">Un buen cafe<br>comienza aqui</p>
  </div>

  <div class="card">
    <div class="cup">&#9749;</div>
    <h2>Cafe Don Bosco</h2>
    <p class="sub">Sistema Centralizado de Mostrador</p>
    <div class="mini-divider"></div>
    <h3>&iexcl;Bienvenido!</h3>
    <p class="hint">Ingresa a tu cuenta de administrador para continuar.</p>

    <form action="${pageContext.request.contextPath}/login" method="post" onsubmit="return validarLogin(event)">
      <div class="error" id="error" <c:if test="${not empty error}">style="display:block"</c:if>>${error}</div>
      <div class="field">
        <svg viewBox="0 0 24 24" fill="none" stroke-width="1.8" stroke-linecap="round"><circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 3.6-6 8-6s8 2 8 6"/></svg>
        <input type="email" id="correo" name="correo" placeholder="Usuario / Correo" autocomplete="username" autofocus>
      </div>
      <div class="field">
        <svg viewBox="0 0 24 24" fill="none" stroke-width="1.8" stroke-linecap="round"><rect x="4" y="10" width="16" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>
        <input type="password" id="password" name="password" placeholder="Contrasena" autocomplete="current-password">
        <button type="button" class="eye" onclick="togglePassword()" aria-label="Mostrar contrasena">
          <svg viewBox="0 0 24 24" fill="none" stroke-width="1.8" stroke-linecap="round"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
        </button>
      </div>
      <button type="submit" class="btn">INICIAR SESION <span class="arrow">&rarr;</span></button>
    </form>

    <p class="help">Acceso exclusivo para el equipo administrador de Cafe Don Bosco.</p>
    <div class="foot-divider"></div>
    <p class="foot">Cafe Don Bosco</p>
    <p class="lema">&#9749; Mas que cafe, es comunidad</p>
    <a class="volver-portal" href="${pageContext.request.contextPath}/">&larr; Volver al portal</a>
  </div>

<script src="${pageContext.request.contextPath}/assets/js/admin-login.js"></script>
</body>
</html>
