import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.Preferences;

public class AddCustomHeaderZ implements BurpExtension {
    private MontoyaApi api;
    private CustomHeadersConfig config;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;

        // Set extension name
        api.extension().setName("AddCustomHeaderZ");

        // Log initialization W00T W00T
        api.logging().logToOutput("Don't Burp, AddCustomHeaderZ");

        // Get preferences for persistent storage
        Preferences preferences = api.persistence().preferences();

        // Create config panel and handler with preferences
        config = new CustomHeadersConfig(preferences);
        CustomHeadersHandler handler = new CustomHeadersHandler(config);

        // Register the HTTP handler
        api.http().registerHttpHandler(handler);

        // Register the UI component
        api.userInterface().registerSuiteTab("Custom HeaderZ", config.getPanel());


    }
}


