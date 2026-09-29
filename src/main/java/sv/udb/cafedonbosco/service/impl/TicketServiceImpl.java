package sv.udb.cafedonbosco.service.impl;

import sv.udb.cafedonbosco.dto.response.DetalleVentaResponseDTO;
import sv.udb.cafedonbosco.dto.response.OpcionSeleccionadaResponseDTO;
import sv.udb.cafedonbosco.dto.response.VentaResponseDTO;
import sv.udb.cafedonbosco.exception.ValidacionException;
import sv.udb.cafedonbosco.model.EstadoVenta;
import sv.udb.cafedonbosco.service.EmailService;
import sv.udb.cafedonbosco.service.TicketService;
import sv.udb.cafedonbosco.service.VentaService;
import sv.udb.cafedonbosco.util.TicketPdfGenerator;
import sv.udb.cafedonbosco.util.ValidacionUtil;
import sv.udb.cafedonbosco.util.WhatsAppUtil;

public class TicketServiceImpl implements TicketService {

    private final VentaService ventaService;
    private final EmailService emailService;

    public TicketServiceImpl() {
        this.ventaService = new VentaServiceImpl();
        this.emailService = new EmailServiceImpl();
    }

    @Override
    public VentaResponseDTO obtenerPorToken(String token) {
        return ventaService.obtenerPorToken(token);
    }

    @Override
    public VentaResponseDTO obtenerPorIdAdmin(int ventaId) {
        return ventaService.obtenerPorId(ventaId);
    }

    @Override
    public void enviarPorCorreo(VentaResponseDTO ticket, String correoDestino) {
        if (!ValidacionUtil.esCorreoValido(correoDestino)) {
            throw new ValidacionException("Debes indicar un correo valido para enviar el ticket.");
        }
        byte[] pdf = TicketPdfGenerator.generar(ticket);
        emailService.enviarTicketPorCorreo(correoDestino, pdf, String.valueOf(ticket.getId()));
    }

    @Override
    public String generarEnlaceWhatsApp(VentaResponseDTO ticket, String telefonoDestino) {
        return WhatsAppUtil.construirEnlace(telefonoDestino, construirMensaje(ticket));
    }

    private String construirMensaje(VentaResponseDTO ticket) {
        StringBuilder productos = new StringBuilder();
        if (ticket.getDetalles() != null) {
            for (DetalleVentaResponseDTO detalle : ticket.getDetalles()) {
                if (productos.length() > 0) {
                    productos.append(", ");
                }
                productos.append(detalle.getCantidad()).append("x ").append(detalle.getNombreProducto());
                if (detalle.getOpciones() != null && !detalle.getOpciones().isEmpty()) {
                    productos.append(" (");
                    for (int i = 0; i < detalle.getOpciones().size(); i++) {
                        if (i > 0) {
                            productos.append(", ");
                        }
                        OpcionSeleccionadaResponseDTO opcion = detalle.getOpciones().get(i);
                        productos.append(opcion.getNombreOpcion());
                    }
                    productos.append(")");
                }
            }
        }
        return "Hola! Confirmacion de pedido #" + ticket.getId() + " en Cafe Don Bosco.\n"
                + "Productos: " + productos + ".\n"
                + "Total: $" + ticket.getTotalFormateado() + ".\n"
                + "Estado: " + formatearEstado(ticket.getEstado()) + ".";
    }

    private String formatearEstado(EstadoVenta estado) {
        if (estado == null) {
            return "Desconocido";
        }
        return switch (estado) {
            case RECIBIDO -> "Recibido";
            case EN_PREPARACION -> "En preparacion";
            case LISTO -> "Listo para entregar";
            case ENTREGADO -> "Entregado";
            case CANCELADO -> "Cancelado";
        };
    }
}
