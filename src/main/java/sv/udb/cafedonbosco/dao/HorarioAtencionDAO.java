package sv.udb.cafedonbosco.dao;

import sv.udb.cafedonbosco.model.HorarioAtencion;

import java.util.List;

public interface HorarioAtencionDAO {

    HorarioAtencion obtenerPorDia(int diaSemana);

    List<HorarioAtencion> listarTodos();

    void actualizar(HorarioAtencion horario);
}
