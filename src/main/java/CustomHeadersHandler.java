import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;

import java.util.List;

/**
 * Handles HTTP requests by adding configured static headers.
 * Dynamic headers are handled separately by the session handling action.
 */
public class CustomHeadersHandler implements HttpHandler {

    private final CustomHeadersConfig config;
    private final Logging logging;

    /**
     * Constructs a new CustomHeadersHandler.
     *
     * @param config  The configuration containing header settings
     * @param logging The logging service
     */
    public CustomHeadersHandler(CustomHeadersConfig config, Logging logging) {
        this.config = config;
        this.logging = logging;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        // Check if custom headers are enabled
        if (!config.isEnabled()) {
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        // Get the list of custom headers from the config
        List<CustomHeadersConfig.CustomHeader> headers = config.getHeaders();

        // Start with the original request
        HttpRequest modifiedRequest = requestToBeSent;

        // Add each enabled header that is not dynamic
        // Dynamic headers are handled by the session handling action
        for (CustomHeadersConfig.CustomHeader header : headers) {
            if (header.isEnabled() && !header.isDynamic()) {
                modifiedRequest = addOrReplaceHeader(modifiedRequest, header);
            }
        }

        // Return the modified request
        return RequestToBeSentAction.continueWith(modifiedRequest);
    }

    /**
     * Adds or replaces a header in the HTTP request.
     *
     * @param request The original HTTP request
     * @param header  The header to add or replace
     * @return The modified HTTP request
     */
    private HttpRequest addOrReplaceHeader(HttpRequest request, CustomHeadersConfig.CustomHeader header) {
        HttpRequest modifiedRequest = request;

        // First check if the header already exists
        if (modifiedRequest.hasHeader(header.getName())) {
            // If it exists, remove it first
            logging.logToOutput("[CustomHeaderZ] Removing existing header: " + header.getName());
            modifiedRequest = modifiedRequest.withRemovedHeader(header.getName());
        }

        // Then add the new header
        HttpHeader httpHeader = HttpHeader.httpHeader(header.getName(), header.getValue());
        modifiedRequest = modifiedRequest.withAddedHeader(httpHeader);
        logging.logToOutput("[CustomHeaderZ] Added static header: '" + header.getName() + ": " + header.getValue() + "'");

        return modifiedRequest;
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        // We're not modifying responses, so just return as is
        return ResponseReceivedAction.continueWith(responseReceived);
    }
}