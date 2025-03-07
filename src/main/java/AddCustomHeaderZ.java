import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.Preferences;
import burp.api.montoya.http.sessions.SessionHandlingAction;
import burp.api.montoya.logging.Logging;

/**
 * Main extension class for AddCustomHeaderZ.
 * This Burp Suite extension allows adding custom headers to HTTP requests,
 * with support for dynamic values extracted from macro responses.
 */
public class AddCustomHeaderZ implements BurpExtension {
    private MontoyaApi api;
    private CustomHeadersConfig config;
    private Logging logging;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        this.logging = api.logging();

        // Set extension name
        api.extension().setName("AddCustomHeaderZ");

        // Log initialization
        logging.logToOutput("[AddCustomHeaderZ] Extension loading...");
        logging.raiseInfoEvent("AddCustomHeaderZ extension is initializing");

        try {
            // Initialize components
            initializeComponents();
            logging.logToOutput("[AddCustomHeaderZ] Extension loaded successfully!");
            logging.raiseInfoEvent("AddCustomHeaderZ extension loaded successfully");
        } catch (Exception e) {
            logging.logToError("[AddCustomHeaderZ] Error during initialization: " + e.getMessage());
            logging.raiseErrorEvent("AddCustomHeaderZ failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes all extension components.
     */
    private void initializeComponents() {
        // Get preferences for persistent storage
        Preferences preferences = api.persistence().preferences();

        // Create config panel and handler with preferences
        config = new CustomHeadersConfig(preferences);
        CustomHeadersHandler handler = new CustomHeadersHandler(config, logging);

        // Register the HTTP handler
        api.http().registerHttpHandler(handler);
        logging.logToOutput("[AddCustomHeaderZ] HTTP handler registered");

        // Register the UI component
        api.userInterface().registerSuiteTab("Custom HeaderZ", config.getPanel());
        logging.logToOutput("[AddCustomHeaderZ] UI component registered");

        // Create and register the session handling action
        SessionHandlingAction sessionAction = new CustomHeadersSessionAction(config, logging);
        api.http().registerSessionHandlingAction(sessionAction);
        logging.logToOutput("[AddCustomHeaderZ] Session handling action registered");
    }
}