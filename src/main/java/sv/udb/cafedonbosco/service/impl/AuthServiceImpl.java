package sv.udb.cafedonbosco.service.impl;

import sv.udb.cafedonbosco.dao.UsuarioDAO;
import sv.udb.cafedonbosco.dao.impl.UsuarioDAOImpl;
import sv.udb.cafedonbosco.dto.request.ActualizarPerfilRequestDTO;
import sv.udb.cafedonbosco.dto.request.RegistroConsumidorDTO;
import sv.udb.cafedonbosco.dto.response.UsuarioResponseDTO;
import sv.udb.cafedonbosco.exception.CredencialesInvalidasException;
import sv.udb.cafedonbosco.exception.RecursoDuplicadoException;
import sv.udb.cafedonbosco.exception.RecursoNoEncontradoException;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.Rol;
import sv.udb.cafedonbosco.model.Usuario;
import sv.udb.cafedonbosco.service.AuthService;
import sv.udb.cafedonbosco.util.PasswordUtil;
import sv.udb.cafedonbosco.util.ValidacionUtil;

public class AuthServiceImpl implements AuthService {

    private final UsuarioDAO usuarioDAO;

    public AuthServiceImpl() {
        this(new UsuarioDAOImpl());
    }

    /** Permite inyectar un UsuarioDAO de prueba (Mockito) sin tocar una base de datos real. */
    public AuthServiceImpl(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public UsuarioResponseDTO login(String correo, String password, Rol rolEsperado) {
        if (!ValidacionUtil.esCorreoValido(correo) || !ValidacionUtil.esTextoValido(password)) {
            throw new CredencialesInvalidasException();
        }

        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
            throw new CredencialesInvalidasException();
        }
        if (!PasswordUtil.verificar(password, usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }
        if (rolEsperado != null && usuario.getRol() != rolEsperado) {
            throw new CredencialesInvalidasException();
        }

        return aDTO(usuario);
    }

    @Override
    public UsuarioResponseDTO registrarConsumidor(RegistroConsumidorDTO datos) {
        if (datos == null
                || !ValidacionUtil.esTextoValido(datos.getNombre(), 80)
                || !ValidacionUtil.esTextoValido(datos.getApellido(), 80)
                || !ValidacionUtil.esCorreoValido(datos.getCorreo())
                || !ValidacionUtil.esTextoValido(datos.getPassword())
                || datos.getPassword().length() < 6) {
            throw new ValidacionException("Revisa los datos del registro: nombre, apellido, correo y contrasena (minimo 6 caracteres).");
        }
        if (usuarioDAO.existeCorreo(datos.getCorreo())) {
            throw new RecursoDuplicadoException("Ya existe una cuenta registrada con ese correo.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(datos.getNombre().trim());
        usuario.setApellido(datos.getApellido().trim());
        usuario.setCorreo(datos.getCorreo().trim().toLowerCase());
        usuario.setPassword(PasswordUtil.hashear(datos.getPassword()));
        usuario.setRol(Rol.CONSUMIDOR);
        usuario.setActivo(true);

        return aDTO(usuarioDAO.crear(usuario));
    }

    @Override
    public UsuarioResponseDTO obtenerPerfil(int usuarioId) {
        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
        if (usuario == null) {
            throw new RecursoNoEncontradoException("El usuario no existe.");
        }
        return aDTO(usuario);
    }

    @Override
    public UsuarioResponseDTO actualizarPerfil(int usuarioId, ActualizarPerfilRequestDTO datos) {
        if (datos == null
                || !ValidacionUtil.esTextoValido(datos.getNombre(), 80)
                || !ValidacionUtil.esTextoValido(datos.getApellido(), 80)
                || !ValidacionUtil.esCorreoValido(datos.getCorreo())) {
            throw new ValidacionException("Revisa los datos del perfil: nombre, apellido y correo son obligatorios.");
        }
        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
        if (usuario == null) {
            throw new RecursoNoEncontradoException("El usuario no existe.");
        }

        String nuevoCorreo = datos.getCorreo().trim().toLowerCase();
        if (!nuevoCorreo.equalsIgnoreCase(usuario.getCorreo())) {
            Usuario existente = usuarioDAO.buscarPorCorreo(nuevoCorreo);
            if (existente != null && !existente.getId().equals(usuarioId)) {
                throw new RecursoDuplicadoException("Ya existe una cuenta registrada con ese correo.");
            }
        }

        usuario.setNombre(datos.getNombre().trim());
        usuario.setApellido(datos.getApellido().trim());
        usuario.setCorreo(nuevoCorreo);
        usuarioDAO.actualizarPerfil(usuario);
        return aDTO(usuario);
    }

    private UsuarioResponseDTO aDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.getActivo()
        );
    }
}
