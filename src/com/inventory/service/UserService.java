package com.inventory.service;

import com.inventory.dao.UserDAO;
import com.inventory.model.User;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        return userDAO.login(username, password);
    }
}
