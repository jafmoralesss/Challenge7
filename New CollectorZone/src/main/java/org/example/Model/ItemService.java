package org.example.Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


public class ItemService {


    public Collection<CollectibleItem> getAllItems() {
        List<CollectibleItem> items = new ArrayList<>();
        String sql = "SELECT * FROM items"; // SQL para obtener todos

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(mapRowToItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApiException(500, "Error de base de datos al obtener items");
        }
        return items;
    }

    public CollectibleItem getItemById(String id) {
        String sql = "SELECT * FROM items WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, UUID.fromString(id));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToItem(rs);
                } else {
                    throw new ApiException(404, "Item not found");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApiException(500, "Error de base de datos");
        }
    }

    public CollectibleItem createItem(String id, CollectibleItem item) {
        String sql = "INSERT INTO items (id, name, description, price) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            UUID uuid = UUID.fromString(id);
            pstmt.setObject(1, uuid);
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());
            pstmt.setDouble(4, item.getPrice());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new ApiException(500, "No se pudo crear el item");
            }

            item.setId(id);
            return item;

        } catch (SQLException e) {

            if (e.getSQLState().equals("23505")) { // Código de violación de llave única
                throw new ApiException(409, "Item with this ID already exists");
            }
            e.printStackTrace();
            throw new ApiException(500, "Error de base de datos al crear item");
        }
    }

    public CollectibleItem updateItem(String id, CollectibleItem item) {
        String sql = "UPDATE items SET name = ?, description = ?, price = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getDescription());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setObject(4, UUID.fromString(id));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new ApiException(404, "Item not found, cannot update");
            }

            item.setId(id);
            return item;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApiException(500, "Error de base de datos al actualizar item");
        }
    }

    public void deleteItem(String id) {
        String sql = "DELETE FROM items WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, UUID.fromString(id));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new ApiException(404, "Item not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApiException(500, "Error de base de datos al borrar item");
        }
    }

    public void itemExists(String id) {
        getItemById(id);
    }

    private CollectibleItem mapRowToItem(ResultSet rs) throws SQLException {
        CollectibleItem item = new CollectibleItem();
        item.setId(rs.getObject("id", UUID.class).toString());
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setPrice(rs.getDouble("price"));
        return item;
    }

}