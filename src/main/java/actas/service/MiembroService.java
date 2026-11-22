package actas.service;

import actas.dao.MiembroDao;
import actas.model.Miembro;

/** Valida y persiste miembros. */
public class MiembroService {

    private final MiembroDao dao;

    public MiembroService(MiembroDao dao) {
        this.dao = dao;
    }

    public Miembro guardar(Miembro m) {
        validar(m);
        Miembro limpio = new Miembro(m.id(), m.nombre().trim(),
                Validaciones.nullSiVacio(m.cedula()), Validaciones.nullSiVacio(m.telefono()),
                Validaciones.nullSiVacio(m.celular()), Validaciones.nullSiVacio(m.correo()),
                m.cargoId(), m.cargo(), m.comiteId(), m.comite());
        if (limpio.id() == null) {
            return dao.insert(limpio);
        }
        dao.update(limpio);
        return limpio;
    }

    static void validar(Miembro m) {
        new Validaciones.Errores()
                .requerido(m.nombre(), "El nombre")
                .maxLongitud(m.nombre(), 100, "El nombre")
                .cedulaOpcional(m.cedula())
                .telefonoOpcional(m.telefono(), "El teléfono")
                .telefonoOpcional(m.celular(), "El celular")
                .correoOpcional(m.correo())
                .si(m.comiteId() == null, "Seleccione un comité.")
                .lanzarSiHay();
    }
}
