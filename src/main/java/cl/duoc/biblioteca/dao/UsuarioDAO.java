package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Rol;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.model.UsuarioFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO extends BaseDAO implements CrudDAO<Usuario> {
    private static final String SELECT_BASE = "SELECT id, nombre, rut, correo, `contraseña` AS clave, rol FROM usuarios ";

    public Usuario autenticar(String correoORut, String contrasena) throws SQLException {
        String sql = SELECT_BASE + "WHERE (correo = ? OR rut = ?) AND `contraseña` = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, correoORut);
            ps.setString(2, correoORut);
            ps.setString(3, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public int insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, rut, correo, `contraseña`, rol) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = prepararInsert(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getRol().getValorBD());
            int id = ejecutarInsertYObtenerId(ps);
            u.setId(id);
            return id;
        }
    }

    @Override
    public Usuario buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_BASE + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Usuario buscarPorRut(String rut) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_BASE + "WHERE rut = ?")) {
            ps.setString(1, rut);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(SELECT_BASE + "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public boolean actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, `contraseña` = ?, rol = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getRol().getValorBD());
            ps.setInt(6, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = UsuarioFactory.crear(Rol.desdeBD(rs.getString("rol")));
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setRut(rs.getString("rut"));
        u.setCorreo(rs.getString("correo"));
        u.setContrasena(rs.getString("clave"));
        return u;
    }
}
