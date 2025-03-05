import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.util.List;

public class CustomHeadersHandler implements HttpHandler {

    private final CustomHeadersConfig config;

    public CustomHeadersHandler(CustomHeadersConfig config) {
        this.config = config;
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

        // Add each enabled header
        for (CustomHeadersConfig.CustomHeader header : headers) {
            if (header.isEnabled()) {
                HttpHeader httpHeader = HttpHeader.httpHeader(header.getName(), header.getValue());
                modifiedRequest = modifiedRequest.withAddedHeader(httpHeader);
            }
        }

        // Return the modified request
        return RequestToBeSentAction.continueWith(modifiedRequest);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        // We're not modifying responses, so just return as is
        return ResponseReceivedAction.continueWith(responseReceived);
    }
}