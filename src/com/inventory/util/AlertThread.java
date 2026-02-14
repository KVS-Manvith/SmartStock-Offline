package com.inventory.util;

import com.inventory.service.AlertService;
import com.inventory.service.ExpiryAlertService;

public class AlertThread extends Thread {

    public void run() {

        AlertService stockAlert = new AlertService();
        ExpiryAlertService expiryAlert = new ExpiryAlertService();

        while (true) {
            try {
                stockAlert.checkLowStock();
                expiryAlert.checkExpiry();

                Thread.sleep(30000); // 30 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
