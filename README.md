# CustomHeaderZ

AddCustomHeaderZ is a Burp Suite extension that helps penetration testers and web security professionals customize HTTP requests by adding custom headers. It supports both static header injection and dynamic header extraction from macro responses.

![Custom Headers Manager Demo](https://github.com/falasi/CustomHeaderZ/blob/main/Demo/demo.gif)

## Overview

CustomHeaderZ lets you:
- **Inject Static Headers:** Automatically add user-defined HTTP headers to outgoing requests.
- **Extract Dynamic Values:** Configure headers to extract dynamic values (like tokens) from macro responses using regex or simple string matching.
- **Configure with Ease:** Manage header settings through an intuitive Swing-based UI integrated directly into Burp Suite.
- **Session Handling Integration:** Update request headers on the fly as part of Burp’s session handling actions.

## Features

- **Static Header:**  
  Automatically add headers with fixed values to your HTTP requests.

- **Dynamic Header Extraction:**  
  Mark headers as dynamic to extract values from macro responses, which is useful for handling frequently expiring authorization tokens. Choose between:
  - **Regex Patterns:** Use capturing groups for precise token extraction.
  - **Simple String Matching:** Extract text that follows a designated string.

- **User-Friendly Configuration Panel:**  
  Easily add, edit, and remove headers. Configure options such as:
  - Header name and value
  - Enable/disable status
  - Dynamic extraction toggle
  - Row coloring for visual organization

## Installation

- [Download the compiled JAR file](https://github.com/falasi/CustomHeaderZ/blob/main/out/artifacts/addcustomheaderz_jar/AddCustomHeaderZ.jar) or build from source.

### Building the Extension

1. Clone or download the project repository.
2. Open the project in your preferred Java IDE.
3. Build the project to generate a JAR file (e.g., `AddCustomHeaderZ.jar`).

### Loading into Burp Suite

1. Open Burp Suite.
2. Go to the **Extender** tab, then **Extensions**.
3. Click **Add** and select the JAR file you built.
4. The extension will load, and you should see a new tab named **Custom HeaderZ** in Burp’s UI.

## Usage

### Configuring Headers

1. **Enable/Disable Extension:**  
   Use the checkbox at the top of the **Custom HeaderZ** tab to turn header injection on or off.

2. **Managing Headers:**  
   - **Add Header:** Click the "Add Header" button to insert a new row, then enter the header name and value.
   - **Static vs. Dynamic:**  
     - For **static headers**, simply enter the value.
     - For **dynamic headers**, check the "Dynamic" box so the extension knows to extract the header value from macro responses.
   - **Edit & Remove:** Use the available buttons to modify or delete header entries.
   - **Row Coloring:** Right-click on a header row to assign a color for easy identification.

3. **Setting Extraction Patterns:**  
   For dynamic headers, right-click on the row and choose **Set Extraction Pattern…**. In the dialog:
   - Pick between **Regex Pattern** or **Simple String**.
   - Enter the pattern (for example, a regex like `Authorization:\s*Bearer\s+([A-Za-z0-9._-]+)`).
   - Confirm your settings so the extension uses this pattern when processing macro responses.

### Using with Macros

- Set up a macro in Burp Suite that captures responses containing the token or value you need.
- When the macro runs, the extension will scan the responses using your extraction patterns.
- Dynamic headers will be updated with the extracted token before the HTTP request is sent.

## Configuration Details

- **Maximum Headers:** You can configure up to 10 headers.
- **Dynamic Extraction:** Choose between regex-based extraction (with capturing groups) or a simple string search.
- **Persistence:** Header configurations are saved using Burp’s preferences, so your settings persist between sessions.

## Troubleshooting

- **Extraction Failures:**  
  - Make sure the macro responses contain the expected content.
  - Double-check your extraction pattern syntax.
- **Headers Not Applied:**  
  - Verify the extension is enabled in the UI.
  - Look at the Burp output logs for any error messages.
- **General Issues:**  
  - Review the logs for insights into header processing and extraction.


## Contributing

Contributions, bug reports, and feature requests are welcome. Feel free to open an issue or submit a pull request on the project repository.
