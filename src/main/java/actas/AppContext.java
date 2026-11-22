package actas;

import actas.dao.ActaDao;
import actas.dao.CatalogoDao;
import actas.dao.MiembroDao;
import actas.dao.ReporteDao;
import actas.dao.ReunionDao;
import actas.dao.TemaDao;
import actas.dao.UsuarioDao;
import actas.db.Jdbc;
import actas.model.Usuario;
import actas.service.MiembroService;
import actas.service.ReunionService;

/**
 * Contenedor de dependencias (inyección manual por constructor). Sustituye el estado estático
 * global del original ({@code FrmPadre.dtpEscritorio}, {@code ifrSelReunion.filaSeleccionada}).
 */
public final class AppContext {

    private final CatalogoDao catalogos;
    private final MiembroDao miembros;
    private final ReunionDao reuniones;
    private final TemaDao temas;
    private final ActaDao actas;
    private final UsuarioDao usuarios;
    private final ReporteDao reportes;
    private final MiembroService miembroService;
    private final ReunionService reunionService;
    private Usuario usuarioActual;

    public AppContext(Jdbc jdbc) {
        this.catalogos = new CatalogoDao(jdbc);
        this.miembros = new MiembroDao(jdbc);
        this.reuniones = new ReunionDao(jdbc);
        this.temas = new TemaDao(jdbc);
        this.actas = new ActaDao(jdbc);
        this.usuarios = new UsuarioDao(jdbc);
        this.reportes = new ReporteDao(jdbc);
        this.miembroService = new MiembroService(miembros);
        this.reunionService = new ReunionService(reuniones);
    }

    public CatalogoDao catalogos() { return catalogos; }
    public MiembroDao miembros() { return miembros; }
    public ReunionDao reuniones() { return reuniones; }
    public TemaDao temas() { return temas; }
    public ActaDao actas() { return actas; }
    public UsuarioDao usuarios() { return usuarios; }
    public ReporteDao reportes() { return reportes; }
    public MiembroService miembroService() { return miembroService; }
    public ReunionService reunionService() { return reunionService; }

    public Usuario usuarioActual() { return usuarioActual; }
    public void setUsuarioActual(Usuario usuario) { this.usuarioActual = usuario; }

    public Integer usuarioActualId() {
        return usuarioActual == null ? null : usuarioActual.id();
    }
}
