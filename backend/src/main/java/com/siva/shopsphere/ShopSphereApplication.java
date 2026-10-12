package com.siva.shopsphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.siva.shopsphere.diagnostics.DatasourceDiagnosticsListener;

@SpringBootApplication
public class ShopSphereApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ShopSphereApplication.class);
        application.addListeners(new DatasourceDiagnosticsListener());
        application.run(args);
    }
}

