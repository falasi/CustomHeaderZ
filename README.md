# CustomHeaderZ

## A Powerful Custom Header Manager for Burp Suite

CustomHeaderZ is a comprehensive Burp Suite extension that enhances your web testing capabilities by allowing you to inject, manage, and dynamically update HTTP headers with precision and flexibility.

Built on Burp Suite's modern Montoya API, this extension is the successor to the popular [AddCustomHeader](https://github.com/PortSwigger/add-custom-header) extension, offering advanced features for security professionals.

## Why Use CustomHeaderZ?

- **Multiple Header Support**: Unlike its predecessor, CustomHeaderZ lets you configure and manage multiple headers simultaneously
- **Modern Architecture**: Built on Burp Suite's latest Montoya API for improved performance and stability
- **Session Token Handling**: Perfect for handling JWT tokens, and other authentication/session mechanisms
- **Intuitive Interface**: Color-coded, easy-to-use UI that integrates seamlessly with Burp Suite

## Demo
<p align="center">
  <img src="https://github.com/falasi/CustomHeaderZ/blob/main/Demo/demo.gif" alt="CustomHeaderZ Demo"/>
</p>

## Key Features

### 🔹 Flexible Header Management
- Add up to 10 custom headers with independent enable/disable toggles
- Organize headers with color coding for visual distinction
- Easily edit, add, and remove headers through an intuitive interface

### 🔹 Static Header Injection
- Add permanent headers to every request without modifying your browser or proxy settings
- Configure once and apply consistently across all tools in Burp Suite
- Perfect for adding authorization tokens, API keys, or custom identifiers

### 🔹 Dynamic Value Extraction
- Automatically extract and update tokens from previous responses
- Support for both regex pattern extraction and simple string matching
- Integrates with Burp's session handling rules and macros
- Ideal for handling CSRF tokens, JWT refresh, and other dynamic authentication mechanisms

### 🔹 Visual Organization
- Color-code different headers for quick visual identification
- Enable/disable individual headers without removing configuration
- At-a-glance view of your current header configuration

## Installation

### Option 1: Direct Download
1. [Download the compiled JAR file](https://github.com/falasi/CustomHeaderZ/blob/main/out/artifacts/addcustomheaderz_jar/AddCustomHeaderZ.jar)
2. In Burp Suite, go to **Extender** → **Extensions** → **Add**
3. Select the downloaded JAR file
4. The extension will load with a new **Custom HeaderZ** tab

### Option 2: Build from Source
1. Clone the repository: `git clone https://github.com/falasi/CustomHeaderZ.git`
2. Open the project in your Java IDE
3. Build the project to generate the JAR file
4. Load the JAR into Burp Suite as described above

## Usage Guide

### Setting Up Headers

1. Navigate to the **Custom HeaderZ** tab in Burp Suite
2. Use the master toggle to enable/disable the extension functionality
3. Click **Add Header** to create a new header entry
4. Configure your header:
   - **Header Name**: The HTTP header name (e.g., `Authorization`, `X-API-Key`)
   - **Header Value**: The static value to use (for static headers)
   - **Enabled**: Toggle to include/exclude this header in requests
   - **Dynamic**: Check this box if the value should be extracted from responses

### Static vs. Dynamic Headers

#### Static Headers
For headers with unchanging values (like API keys):
1. Enter the header name and value
2. Leave the **Dynamic** checkbox unchecked
3. The value will be applied to all requests

#### Dynamic Headers
For headers that need values extracted from responses (like tokens):
1. Enter the header name
2. Check the **Dynamic** checkbox
3. Right-click the header row and select **Set Extraction Pattern...**
4. Choose between:
   - **Regex Pattern**: For precise extraction using capture groups
     - Example: `Authorization:\s*Bearer\s+([A-Za-z0-9._-]+)`
   - **Simple String**: For straightforward extraction
     - Example: `"access_token":"`

### Organizing Headers
- **Color-Coding**: Right-click any header row to assign a color
- **Reordering**: Use the table to visually organize your headers
- **Toggle Headers**: Quickly enable/disable headers without removing them

### Setting Up with Macros

For dynamic headers with values that need to be refreshed:

1. Configure a dynamic header with an extraction pattern
2. In Burp, go to **Project options** → **Sessions**
3. Under **Session Handling Rules**, add a new rule
4. In the rule details, add a new **Run a macro** action
5. Configure the macro to target the authentication endpoint
6. Add the **CustomHeaderZ Extract Token** action to the rule
7. Apply the rule scope as needed

## Troubleshooting

### Extraction Not Working
- Verify your macro produces responses containing the expected tokens
- Check your extraction pattern against the actual response content
- For regex patterns, ensure capture groups are correctly defined
- Enable Burp Suite's logging for detailed diagnostic information

### Headers Not Applied
- Confirm the master toggle is enabled in the CustomHeaderZ tab
- Check that individual headers are marked as enabled
- Verify the header isn't being overwritten by another extension
- Check the scope settings in your session handling rules

### Debugging Tips
- Right-click on a header row to set its extraction pattern
- Use Burp's logger (in the Extender tab) to see CustomHeaderZ's output
- Test your regex patterns with a tool like regex101.com before using them

## Contributing

Contributions to enhance CustomHeaderZ are welcome! Feel free to:
- Report bugs by opening an issue
- Suggest new features or improvements
- Submit pull requests with bug fixes or enhancements

