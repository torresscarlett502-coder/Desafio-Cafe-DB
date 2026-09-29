package sv.udb.cafedonbosco.dao;

import sv.udb.cafedonbosco.model.Bitacora;

import java.sql.Connection;
import java.util.List;

public interface BitacoraDAO {

    /** Se registra dentro de la misma transaccion que la operacion auditada. */
    void registrar(Connection conexion, Bitacora entrada);

    List<Bitacora> listar(String entidad, Integer entidadId, int limite);
}
