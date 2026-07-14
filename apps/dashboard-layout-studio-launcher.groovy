/**
 * Dashboard Layout Studio Launcher
 *
 * Installs the latest stable Dashboard Layout Studio HTML release into
 * Hubitat File Manager and provides a launch link. DLS manages its own
 * updates after the initial installation.
 *
 * Version: 1.0.3
 * Build: 004
 */

import groovy.transform.Field

@Field static final String APP_VERSION = "1.0.3"
@Field static final String APP_BUILD = "004"
@Field static final String DLS_FILE_NAME = "dashboard-layout-studio.html"
@Field static final String DLS_LOCAL_PATH = "/local/dashboard-layout-studio.html"
@Field static final String DLS_DOWNLOAD_URL = "https://github.com/imdrewsf/Dashboard-Layout-Studio/releases/latest/download/dashboard-layout-studio.html"


definition(
    name: "Dashboard Layout Studio",
    namespace: "imdrewsf",
    author: "Andrew Peck",
    description: "Installs and launches Dashboard Layout Studio for Hubitat Dashboard v1 layouts.",
    category: "Convenience",
    importUrl: "https://raw.githubusercontent.com/imdrewsf/Dashboard-Layout-Studio/main/apps/dashboard-layout-studio-launcher.groovy",
    iconUrl: "https://raw.githubusercontent.com/imdrewsf/Dashboard-Layout-Studio/main/images/dls-icon.png",
    iconX2Url: "https://raw.githubusercontent.com/imdrewsf/Dashboard-Layout-Studio/main/images/dls-icon-x2.png",
    iconX3Url: "https://raw.githubusercontent.com/imdrewsf/Dashboard-Layout-Studio/main/images/dls-icon-x3.png",
    installOnOpen: true,
    singleInstance: true
)

preferences {
    page(name: "mainPage")
    page(name: "removeDlsPage")
}


def mainPage() {
    Map fileStatus = getDlsFileStatus()
    Boolean installed = fileStatus.exists as Boolean

    dynamicPage(
        name: "mainPage",
        title: "Dashboard Layout Studio",
        install: true,
        uninstall: true
    ) {
        section {
            paragraph statusCard(installed, fileStatus.available as Boolean)
        }

        if (!(fileStatus.available as Boolean)) {
            section {
                paragraph messageCard(
                    "Hubitat File Manager could not be read. Installation and removal controls are disabled until File Manager is available.",
                    "error"
                )
            }
        }

        if (state.lastMessage) {
            section {
                paragraph messageCard(
                    state.lastMessage as String,
                    (state.lastMessageType ?: "info") as String
                )
            }
        }

        if ((fileStatus.available as Boolean) && !installed) {
            section("Install") {
                paragraph "The launcher will download the latest stable DLS release from GitHub and save it in Hubitat File Manager as <code>${DLS_FILE_NAME}</code>."
                input(
                    name: "installDlsButton",
                    type: "button",
                    title: "Install Dashboard Layout Studio",
                    backgroundColor: "#1976d2"
                )
            }
        } else if ((fileStatus.available as Boolean) && installed) {
            section("Launch") {
                paragraph launchButton()
            }

            section("Removal") {
                href(
                    name: "removeDlsLink",
                    title: "Remove Dashboard Layout Studio",
                    description: "Delete ${DLS_FILE_NAME} from Hubitat File Manager.",
                    page: "removeDlsPage"
                )
            }
        }

        section("Launcher information") {
            paragraph "Launcher version ${APP_VERSION} (build ${APP_BUILD}). DLS performs its own update checks after it has been installed."
        }
    }
}


def removeDlsPage() {
    Map fileStatus = getDlsFileStatus()
    Boolean installed = fileStatus.exists as Boolean

    dynamicPage(
        name: "removeDlsPage",
        title: "Remove Dashboard Layout Studio",
        nextPage: "mainPage",
        install: false,
        uninstall: false
    ) {
        if (!(fileStatus.available as Boolean)) {
            section {
                paragraph messageCard(
                    "Hubitat File Manager could not be read. No file was removed.",
                    "error"
                )
            }
        } else if (installed) {
            section {
                paragraph warningCard(
                    "This deletes <code>${DLS_FILE_NAME}</code> from Hubitat File Manager. " +
                    "The launcher app will remain installed and will return to its Install state."
                )
                input(
                    name: "confirmRemoveDlsButton",
                    type: "button",
                    title: "Confirm Removal",
                    backgroundColor: "#b71c1c"
                )
            }
        } else {
            section {
                paragraph messageCard("Dashboard Layout Studio has been removed.", "success")
                paragraph "Select <strong>Next</strong> to return to the launcher."
            }
        }
    }
}


def installed() {
    initialize()
}


def updated() {
    initialize()
}


def uninstalled() {
    try {
        if (getDlsFileStatus().exists as Boolean) {
            deleteHubFile(DLS_FILE_NAME)
            log.info "Deleted ${DLS_FILE_NAME} while uninstalling Dashboard Layout Studio."
        }
    } catch (Exception exception) {
        log.warn "Unable to delete ${DLS_FILE_NAME} during uninstall: ${exception.message}"
    }
}


void initialize() {
    app.updateLabel("Dashboard Layout Studio")
}


void appButtonHandler(String buttonName) {
    switch (buttonName) {
        case "installDlsButton":
            installDls()
            break

        case "confirmRemoveDlsButton":
            removeDls()
            break

        default:
            log.warn "Unknown Dashboard Layout Studio button: ${buttonName}"
            setMessage("Unknown button request.", "error")
            break
    }
}


private void installDls() {
    clearMessage()

    try {
        Map initialStatus = getDlsFileStatus()
        if (!(initialStatus.available as Boolean)) {
            throw new RuntimeException("Hubitat File Manager could not be read.")
        }
        if (initialStatus.exists as Boolean) {
            setMessage("Dashboard Layout Studio is already installed.", "info")
            return
        }
        String html = null

        Map request = [
            uri: DLS_DOWNLOAD_URL,
            headers: [
                "Accept": "*/*",
                "User-Agent": "Hubitat-DLS-Launcher/${APP_VERSION}"
            ],
            textParser: true,
            followRedirects: true,
            timeout: 90
        ]

        httpGet(request) { response ->
            if (response?.status != 200) {
                throw new RuntimeException("GitHub returned HTTP ${response?.status ?: 'unknown'}.")
            }
            html = responseBodyAsText(response?.data)
        }

        validateDownloadedHtml(html)
        uploadHubFile(DLS_FILE_NAME, html.getBytes("UTF-8"))

        Map finalStatus = getDlsFileStatus()
        if (!(finalStatus.available as Boolean) || !(finalStatus.exists as Boolean)) {
            throw new RuntimeException("Hubitat did not report the uploaded file in File Manager.")
        }

        setMessage("Dashboard Layout Studio was installed successfully.", "success")
        log.info "Installed ${DLS_FILE_NAME} from ${DLS_DOWNLOAD_URL}."
    } catch (Exception exception) {
        log.error "Dashboard Layout Studio installation failed: ${exception}"
        setMessage("Installation failed: ${exception.message ?: exception.class.simpleName}", "error")
    }
}


private void removeDls() {
    clearMessage()

    try {
        Map initialStatus = getDlsFileStatus()
        if (!(initialStatus.available as Boolean)) {
            throw new RuntimeException("Hubitat File Manager could not be read.")
        }
        if (initialStatus.exists as Boolean) {
            deleteHubFile(DLS_FILE_NAME)
        }

        Map finalStatus = getDlsFileStatus()
        if (!(finalStatus.available as Boolean)) {
            throw new RuntimeException("Hubitat File Manager could not be read after removal.")
        }
        if (finalStatus.exists as Boolean) {
            throw new RuntimeException("The file is still present in Hubitat File Manager.")
        }

        setMessage("Dashboard Layout Studio was removed.", "success")
        log.info "Removed ${DLS_FILE_NAME} from Hubitat File Manager."
    } catch (Exception exception) {
        log.error "Dashboard Layout Studio removal failed: ${exception}"
        setMessage("Removal failed: ${exception.message ?: exception.class.simpleName}", "error")
    }
}


private Map getDlsFileStatus() {
    try {
        List<Map<String, String>> files = getHubFiles() ?: []
        Boolean exists = files.any { Map<String, String> file ->
            String fileName = (file?.name ?: file?.fileName ?: "") as String
            fileName == DLS_FILE_NAME
        }
        return [available: true, exists: exists]
    } catch (Exception exception) {
        log.warn "Unable to read Hubitat File Manager contents: ${exception.message}"
        return [available: false, exists: false, error: exception.message]
    }
}


private String responseBodyAsText(Object data) {
    if (data == null) {
        return null
    }

    // With textParser:true Hubitat supplies a reader-like response object.
    // The Groovy text property consumes that response and returns its content.
    // Calling toString() only returns an object description such as
    // java.io.StringReader@1a2b3c, not the downloaded file.
    return data.text as String
}


private void validateDownloadedHtml(String html) {
    if (!html) {
        throw new RuntimeException("The downloaded release was empty.")
    }

    if (html.length() < 10000) {
        throw new RuntimeException("The downloaded release was unexpectedly small (${html.length()} characters).")
    }

    String lower = html.toLowerCase()
    if (!lower.contains("<html") || !lower.contains("dashboard layout studio")) {
        throw new RuntimeException("The downloaded file did not appear to be Dashboard Layout Studio HTML.")
    }
}


private void setMessage(String message, String type) {
    state.lastMessage = message
    state.lastMessageType = type
}


private void clearMessage() {
    state.remove("lastMessage")
    state.remove("lastMessageType")
}


private String launchButton() {
    return """
        <div style="text-align:center; padding:18px 0 12px 0;">
            <a href="${DLS_LOCAL_PATH}"
               target="_blank"
               rel="noopener"
               style="display:inline-block; padding:13px 24px; background:#1976d2; color:#ffffff;
                      text-decoration:none; border-radius:5px; font-weight:600; font-size:16px;
                      box-shadow:0 2px 4px rgba(0,0,0,.25);">
                Launch Dashboard Layout Studio
            </a>
        </div>
    """
}


private String statusCard(Boolean installed, Boolean available) {
    String status = !available ? "File Manager unavailable" : (installed ? "Installed" : "Not installed")
    String color = !available ? "#b71c1c" : (installed ? "#1b5e20" : "#6d4c41")
    String detail = !available
        ? "The launcher could not determine whether ${DLS_FILE_NAME} is installed."
        : (installed
            ? "${DLS_FILE_NAME} is present in Hubitat File Manager."
            : "${DLS_FILE_NAME} is not present in Hubitat File Manager.")

    return """
        <div style="border-left:5px solid ${color}; background:#f5f5f5; padding:14px 16px; border-radius:4px;">
            <div style="font-size:18px; font-weight:600; color:${color};">${status}</div>
            <div style="margin-top:4px;">${detail}</div>
        </div>
    """
}


private String warningCard(String message) {
    return """
        <div style="border-left:5px solid #b71c1c; background:#ffebee; padding:14px 16px; border-radius:4px;">
            <strong>Confirm removal</strong><br>${message}
        </div>
    """
}


private String messageCard(String message, String type) {
    Map<String, Map<String, String>> styles = [
        success: [border: "#1b5e20", background: "#e8f5e9"],
        error:   [border: "#b71c1c", background: "#ffebee"],
        info:    [border: "#1565c0", background: "#e3f2fd"]
    ]

    Map<String, String> style = styles[type] ?: styles.info
    return """
        <div style="border-left:5px solid ${style.border}; background:${style.background}; padding:12px 14px; border-radius:4px;">
            ${escapeHtml(message)}
        </div>
    """
}


private String escapeHtml(String value) {
    return (value ?: "")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
        .replace("'", "&#39;")
}
