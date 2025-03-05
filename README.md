# CustomHeaderZ - ustom Headers Manager for Burp Suite

A Burp Suite extension that allows you to easily add, manage, and persist custom HTTP headers for all your requests w00t.


![Custom Headers Manager Demo](https://via.placeholder.com/800x400?text=Custom+Headers+Manager)

## Features

- Add up to 10 custom HTTP headers to all Burp Suite requests
- Enable/disable individual headers without removing them
- Toggle all custom headers on/off with a single checkbox
- Persistent configuration that survives Burp Suite restarts
- Simple, intuitive user interface
- Compatible with Burp Suite Professional 2025.2+ (Montoya API)

## Installation

1. [Download the compiled JAR file](https://github.com/falasi/CustomHeaderZ/blob/main/out/artifacts/addcustomheaderz_jar/AddCustomHeaderZ.jar)
2. Open Burp Suite
3. Go to Extensions tab
4. Click "Add" button
5. Set Extension Type to "Java"
6. Select the downloaded JAR file
7. Click "Next" to complete the installation

## Usage

### Managing Headers

1. Navigate to the "Custom Headers" tab in Burp Suite
2. Use the table interface to add, edit, or remove custom headers:
   - **Header Name**: The name of the HTTP header (e.g., "X-Custom-Header")
   - **Header Value**: The value for the header (e.g., "MyValue123")
   - **Enabled**: Check or uncheck to enable/disable individual headers

### Adding Headers

1. Click the "Add Header" button to add a new row to the table
2. Enter the header name and value
3. Make sure the "Enabled" checkbox is selected
4. Click "Save Configuration" to persist your changes

### Removing Headers

1. Select the header row you want to remove
2. Click the "Remove Header" button
3. Click "Save Configuration" to persist your changes

### Enabling/Disabling All Headers

The "Enable Custom Headers" checkbox at the top of the panel allows you to quickly enable or disable all custom headers without removing them from your configuration.

### Saving Configuration

Click the "Save Configuration" button to ensure your headers are saved persistently. Your configuration will be automatically loaded when you restart Burp Suite or reload the extension.

## Implementation Details

This extension uses the Burp Suite Montoya API to:

1. Register an HTTP handler that adds custom headers to outgoing requests
2. Create a custom tab in the Burp Suite UI for managing headers
3. Store configuration persistently using Burp's Preferences API




