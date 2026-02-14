package com.inventory.dao;

import com.inventory.model.User;
import com.inventory.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public User login(String username, String password) {

        String sql = "SELECT user_id, username, role FROM users WHERE username = ? AND password = ?";
        User user = null;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, password.trim());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null; // graceful failure
        }

        return user;
    }
}
