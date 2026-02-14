package com.inventory.ui;

import com.inventory.model.User;

public class MenuUI {

    public static void showMenu(User user) {

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            AdminMenu.show();
        } else {
            StaffMenu.show();
        }
    }
}
