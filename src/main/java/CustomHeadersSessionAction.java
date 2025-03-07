import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.http.sessions.ActionResult;
import burp.api.montoya.http.sessions.SessionHandlingAction;
import burp.api.montoya.http.sessions.SessionHandlingActionData;
import burp.api.montoya.logging.Logging;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Session handling action that processes macro responses, extracts tokens using regex,
 * and dynamically updates headers with the extracted tokens.
 */
public class CustomHeadersSessionAction implements SessionHandlingAction {

    private final CustomHeadersConfig config;
    private final Logging logging;

    /**
     * Constructs a new CustomHeadersSessionAction.
     *
     * @param config  The configuration containing header settings
     * @param logging The logging service
     */
    public CustomHeadersSessionAction(CustomHeadersConfig config, Logging logging) {
        this.config = config;
        this.logging = logging;
    }

    @Override
    public String name() {
        return "CustomHeaderZ Extract Token";
    }

    @Override
    public ActionResult performAction(SessionHandlingActionData actionData) {
        // If custom headers are not enabled, return the original request unchanged.
        if (!config.isEnabled()) {
            logging.logToOutput("[CustomHeaderZ] Custom headers are disabled, skipping");
            return ActionResult.actionResult(actionData.request());
        }

        HttpRequest request = actionData.request();
        List<HttpRequestResponse> macroItems = actionData.macroRequestResponses();

        // If no macro items, we can't extract anything
        if (macroItems.isEmpty()) {
            logging.logToOutput("[CustomHeaderZ] No macro configured or macro did not return any response");
            logging.raiseInfoEvent("CustomHeaderZ: No macro responses available for token extraction");
            return ActionResult.actionResult(request);
        }

        logging.logToOutput("[CustomHeaderZ] Processing " + macroItems.size() + " macro responses");

        // Process each dynamic header
        List<CustomHeadersConfig.CustomHeader> headers = config.getHeaders();
        for (CustomHeadersConfig.CustomHeader header : headers) {
            if (header.isEnabled() && header.isDynamic()) {
                request = processHeader(request, header, macroItems);
            }
        }

        return ActionResult.actionResult(request);
    }

    /**
     * Processes a single header, extracting its value from macro responses if needed.
     *
     * @param request    The current request
     * @param header     The header configuration to process
     * @param macroItems The macro responses to extract values from
     * @return The updated request
     */
    private HttpRequest processHeader(HttpRequest request, CustomHeadersConfig.CustomHeader header,
                                      List<HttpRequestResponse> macroItems) {
        logging.logToOutput("[CustomHeaderZ] Processing dynamic header: " + header.getName());
        logging.logToOutput("[CustomHeaderZ] Using pattern: " + header.getPattern());
        logging.logToOutput("[CustomHeaderZ] Using regex extraction: " + header.isRegex());

        String token = extractToken(header, macroItems);

        if (token == null) {
            logging.logToOutput("[CustomHeaderZ] No token found for header: " + header.getName());
            return request;
        }

        // Update the request with the new header
        HttpRequest updatedRequest = request;
        if (updatedRequest.hasHeader(header.getName())) {
            logging.logToOutput("[CustomHeaderZ] Removing existing header: " + header.getName());
            updatedRequest = updatedRequest.withRemovedHeader(header.getName());
        }

        updatedRequest = updatedRequest.withAddedHeader(header.getName(), token);
        logging.logToOutput("[CustomHeaderZ] Added header: '" + header.getName() + ": " + token + "'");
        logging.raiseInfoEvent("CustomHeaderZ: Added dynamic header '" + header.getName() + "'");

        return updatedRequest;
    }

    /**
     * Extracts a token from macro responses using the specified pattern.
     *
     * @param header     The header configuration with pattern information
     * @param macroItems The macro responses to extract the token from
     * @return The extracted token, or null if no match found
     */
    private String extractToken(CustomHeadersConfig.CustomHeader header, List<HttpRequestResponse> macroItems) {
        String pattern = header.getPattern();

        if (header.isRegex()) {
            return extractWithRegex(pattern, macroItems);
        } else {
            return extractWithString(pattern, macroItems);
        }
    }

    /**
     * Extracts a token using a regex pattern.
     *
     * @param pattern    The regex pattern to use
     * @param macroItems The macro responses to search in
     * @return The extracted token, or null if no match found
     */
    private String extractWithRegex(String pattern, List<HttpRequestResponse> macroItems) {
        try {
            Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
            logging.logToOutput("[CustomHeaderZ] Compiled regex pattern: " + pattern);

            for (int i = 0; i < macroItems.size(); i++) {
                HttpResponse response = macroItems.get(i).response();
                if (response == null) {
                    logging.logToOutput("[CustomHeaderZ] Response " + (i+1) + " is null");
                    continue;
                }

                String responseBody = response.bodyToString();
                Matcher m = p.matcher(responseBody);

                if (m.find()) {
                    logging.logToOutput("[CustomHeaderZ] Found match in response " + (i+1));

                    if (m.groupCount() > 0) {
                        String token = m.group(1);
                        logging.logToOutput("[CustomHeaderZ] Extracted from capture group 1: " + token);
                        if (token != null && !token.isEmpty()) {
                            return token;
                        }
                    } else {
                        String token = m.group(0);
                        logging.logToOutput("[CustomHeaderZ] Extracted from full match: " + token);
                        if (token != null && !token.isEmpty()) {
                            return token;
                        }
                    }
                } else {
                    logging.logToOutput("[CustomHeaderZ] No match found in response " + (i+1));
                    logResponseSample(responseBody);
                }
            }
        } catch (Exception e) {
            logging.logToError("[CustomHeaderZ] Syntax error in regular expression: " + e.toString());
            logging.raiseErrorEvent("CustomHeaderZ: Invalid regex pattern - " + e.getMessage());
        }
        return null;
    }

    /**
     * Logs a sample of the response for debugging purposes.
     *
     * @param responseBody The response body to sample
     */
    private void logResponseSample(String responseBody) {
        if (!responseBody.isEmpty()) {
            String sample = responseBody.length() > 200 ?
                    responseBody.substring(0, 200) + "..." : responseBody;
            logging.logToOutput("[CustomHeaderZ] Response sample: " + sample);
        } else {
            logging.logToOutput("[CustomHeaderZ] Response body is empty");
        }
    }

    /**
     * Extracts a token using a simple string search.
     *
     * @param searchString The string to search for
     * @param macroItems   The macro responses to search in
     * @return The extracted token, or null if no match found
     */
    private String extractWithString(String searchString, List<HttpRequestResponse> macroItems) {
        logging.logToOutput("[CustomHeaderZ] Looking for string: " + searchString);

        for (int i = 0; i < macroItems.size(); i++) {
            HttpResponse response = macroItems.get(i).response();
            if (response == null) {
                logging.logToOutput("[CustomHeaderZ] Response " + (i+1) + " is null");
                continue;
            }

            String responseBody = response.bodyToString();
            int index = responseBody.indexOf(searchString);

            if (index != -1) {
                logging.logToOutput("[CustomHeaderZ] Found search string in response " + (i+1) + " at index " + index);

                // Extract what comes after the search string
                int startPos = index + searchString.length();

                if (startPos >= responseBody.length()) {
                    logging.logToOutput("[CustomHeaderZ] Search string is at the end of the response, nothing to extract");
                    return "";
                }

                // Extract until next whitespace or delimiter
                StringBuilder sb = new StringBuilder();
                for (int j = startPos; j < responseBody.length(); j++) {
                    char c = responseBody.charAt(j);
                    if (Character.isWhitespace(c) || c == ',' || c == '"' || c == '}' || c == ']') {
                        break;
                    }
                    sb.append(c);
                }

                String token = sb.toString();
                logging.logToOutput("[CustomHeaderZ] Extracted value: " + token);
                if (!token.isEmpty()) {
                    return token;
                }
            } else {
                logging.logToOutput("[CustomHeaderZ] Search string not found in response " + (i+1));
            }
        }
        return null;
    }
}