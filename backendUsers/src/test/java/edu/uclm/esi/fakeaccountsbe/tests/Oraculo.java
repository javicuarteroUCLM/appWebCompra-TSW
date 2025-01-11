package edu.uclm.esi.fakeaccountsbe.tests;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Oraculo {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/usuariosListaCompra?serverTimezone=UTC";
    private static final String DB_USER = "listacompra";
    private static final String DB_PASSWORD = "listacompra";

    public static boolean isUserRegistered(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement ps = connection.prepareStatement(query)) {

                ps.setString(1, email);

                try (ResultSet resultSet = ps.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }   
                return false;
            }
    }
}
