package com.inventory.ui;

import com.inventory.model.User;
import javafx.stage.Stage;

public class DashboardUI {

    public static void open(User user, Stage loginStage) {
        TabbedDashboardUI.show(user, loginStage);
    }

    public static void openLogin(Stage stage) {
        new FXLoginApp().start(stage);
    }
}
