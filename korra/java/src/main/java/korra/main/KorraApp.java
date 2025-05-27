package korra.main;

import korra.core.Coordinator;
import korra.api.AdminInterface;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for KORRA
 */
public class KorraApp {
    private static final Logger LOGGER = Logger.getLogger(KorraApp.class.getName());
    
    private static final int DEFAULT_ADMIN_PORT = 8080;
    
    /**
     * Main method
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        LOGGER.info("Starting KORRA");
        
        try {
            // Parse command line arguments
            int adminPort = DEFAULT_ADMIN_PORT;
            if (args.length > 0) {
                try {
                    adminPort = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    LOGGER.warning("Invalid admin port: " + args[0] + ", using default: " + DEFAULT_ADMIN_PORT);
                }
            }
            
            // Create and start coordinator
            Coordinator coordinator = new Coordinator();
            coordinator.start();
            
            // Create and start admin interface
            AdminInterface adminInterface = new AdminInterface(coordinator, adminPort);
            adminInterface.start();
            
            LOGGER.info("KORRA started successfully");
            
            // Wait for shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutting down KORRA");
                adminInterface.stop();
                coordinator.stop();
                LOGGER.info("KORRA shutdown complete");
            }));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start KORRA", e);
            System.exit(1);
        }
    }
}