package sv.udb.cafedonbosco.dao;

import sv.udb.cafedonbosco.model.Compra;
import sv.udb.cafedonbosco.model.DetalleCompra;

import java.sql.Connection;
import java.util.List;

public interface CompraDAO {

    Compra crear(Connection conexion, Compra compra);

    void crearDetalle(Connection conexion, DetalleCompra detalle, int compraId);

    List<Compra> listarTodas();

    Compra buscarPorId(int id);
}
