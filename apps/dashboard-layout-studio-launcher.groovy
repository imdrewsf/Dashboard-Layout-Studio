/**
 * Dashboard Layout Studio Launcher
 *
 * Installs the latest stable Dashboard Layout Studio HTML release into
 * Hubitat File Manager and provides a launch link. DLS manages its own
 * updates after the initial installation.
 *
 * Version: 1.0.13
 * Build: 014
 */

import groovy.transform.Field

@Field static final String APP_VERSION = "1.0.13"
@Field static final String APP_BUILD = "014"
@Field static final String DLS_FILE_NAME = "dashboard-layout-studio.html"
@Field static final String DLS_LOCAL_PATH = "/local/dashboard-layout-studio.html"
@Field static final String DLS_DOWNLOAD_URL = "https://raw.githubusercontent.com/imdrewsf/Dashboard-Layout-Studio/main/dashboard-layout-studio.html"
@Field static final Long OPERATION_TIMEOUT_MS = 180000L

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
    // Retained so an already-open build 013 removal page remains valid after an update.
    page(name: "removeDlsPage")
}


def mainPage() {
    recoverStaleOperation()

    Map fileStatus = getDlsFileStatus()
    Boolean available = fileStatus.available as Boolean
    Boolean installed = fileStatus.exists as Boolean
    Boolean installing = isOperationActive("install")
    Boolean removing = isOperationActive("remove")
    Boolean busy = installing || removing
    Boolean confirmRemove = state.removeConfirmationRequested == true

    if (!installed || busy) {
        state.remove("removeConfirmationRequested")
        confirmRemove = false
    }

    // A successful completed operation is already represented by the status row.
    // Keeping a second success banner makes the page redundant and can briefly
    // contradict the File Manager status while Hubitat is refreshing.
    if (!busy && state.lastMessageType == "success") {
        clearMessage()
    }

    dynamicPage(
        name: "mainPage",
        title: " ",
        install: true,
        uninstall: true,
        refreshInterval: busy ? 1 : 0,
        containerClass: "dls-launcher-container"
    ) {
        section {
            String content = launcherStyles()

            if (busy) {
                content += operationProgressPanel(installing ? "install" : "remove")
            } else {
                content += statusCard(installed, available)

                if (!available) {
                    content += messageCard(
                        "Hubitat File Manager could not be read. Installation and removal controls are disabled until File Manager is available.",
                        "error"
                    )
                } else if (state.lastMessage) {
                    content += messageCard(
                        state.lastMessage as String,
                        (state.lastMessageType ?: "info") as String
                    )
                }

                if (installed && confirmRemove) {
                    content += warningCard(
                        "This deletes <code>${DLS_FILE_NAME}</code> from Hubitat File Manager. " +
                        "The launcher app remains installed."
                    )
                }
            }

            paragraph content

            if (available && installed && !busy && !confirmRemove) {
                paragraph launchButton()
            }

            if (available && !busy) {
                if (!installed) {
                    input(
                        name: "installDlsButton",
                        type: "button",
                        title: "Install Dashboard Layout Studio",
                        backgroundColor: "#1976d2"
                    )
                } else if (confirmRemove) {
                    input(
                        name: "confirmRemoveDlsButton",
                        type: "button",
                        title: "Confirm Removal",
                        backgroundColor: "#b71c1c"
                    )
                    input(
                        name: "cancelRemoveDlsButton",
                        type: "button",
                        title: "Cancel",
                        backgroundColor: "#757575"
                    )
                } else {
                    input(
                        name: "requestRemoveDlsButton",
                        type: "button",
                        title: "Remove DLS",
                        backgroundColor: "#b71c1c"
                    )
                }
            }
        }
    }
}


// Compatibility page for users who updated while the build 013 removal page was open.
def removeDlsPage() {
    recoverStaleOperation()

    Map fileStatus = getDlsFileStatus()
    Boolean available = fileStatus.available as Boolean
    Boolean installed = fileStatus.exists as Boolean
    Boolean removing = isOperationActive("remove")

    dynamicPage(
        name: "removeDlsPage",
        title: " ",
        nextPage: "mainPage",
        install: false,
        uninstall: false,
        refreshInterval: removing ? 1 : 0,
        containerClass: "dls-launcher-container"
    ) {
        section {
            String content = launcherStyles()

            if (removing) {
                content += operationProgressPanel("remove")
            } else if (!available) {
                content += messageCard(
                    "Hubitat File Manager could not be read. No file was removed.",
                    "error"
                )
            } else if (installed) {
                content += warningCard(
                    "This deletes <code>${DLS_FILE_NAME}</code> from Hubitat File Manager. " +
                    "The launcher app remains installed."
                )
            } else {
                content += statusCard(false, true)
            }

            paragraph content

            if (available && installed && !removing) {
                input(
                    name: "confirmRemoveDlsButton",
                    type: "button",
                    title: "Confirm Removal",
                    backgroundColor: "#b71c1c"
                )
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
    unschedule()
    clearOperation()
    state.remove("removeConfirmationRequested")
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
    recoverStaleOperation()
}


void appButtonHandler(String buttonName) {
    switch (buttonName) {
        case "installDlsButton":
            state.remove("removeConfirmationRequested")
            beginInstallDls()
            break

        case "requestRemoveDlsButton":
            clearMessage()
            if (!isOperationActive()) {
                state.removeConfirmationRequested = true
            }
            break

        case "cancelRemoveDlsButton":
            state.remove("removeConfirmationRequested")
            clearMessage()
            break

        case "confirmRemoveDlsButton":
            state.remove("removeConfirmationRequested")
            beginRemoveDls()
            break

        default:
            log.warn "Unknown Dashboard Layout Studio button: ${buttonName}"
            setMessage("Unknown button request.", "error")
            break
    }
}


private void beginInstallDls() {
    recoverStaleOperation()
    clearMessage()

    if (isOperationActive()) {
        setMessage("Another launcher operation is already in progress.", "info")
        return
    }

    try {
        Map initialStatus = getDlsFileStatus()
        if (!(initialStatus.available as Boolean)) {
            throw new RuntimeException("Hubitat File Manager could not be read.")
        }
        if (initialStatus.exists as Boolean) {
            setMessage("Dashboard Layout Studio is already installed.", "info")
            return
        }

        String operationToken = startOperation("install", "Starting the download from GitHub…")
        Map request = [
            uri: DLS_DOWNLOAD_URL,
            headers: [
                "Accept": "text/html,text/plain,*/*",
                "User-Agent": "Hubitat-DLS-Launcher/${APP_VERSION}"
            ],
            contentType: "text/plain",
            followRedirects: true,
            timeout: 90
        ]

        asynchttpGet("installDownloadCallback", request, [token: operationToken])
        log.info "Started asynchronous DLS download from ${DLS_DOWNLOAD_URL}."
    } catch (Exception exception) {
        log.error "Unable to start Dashboard Layout Studio installation: ${exception}"
        clearOperation()
        setMessage("Installation failed to start: ${exception.message ?: 'Unknown error'}", "error")
    }
}


def installDownloadCallback(response, data = null) {
    String operationToken = (data?.token ?: "") as String
    if (!operationMatches("install", operationToken)) {
        log.warn "Ignoring a stale Dashboard Layout Studio download callback."
        return
    }

    try {
        Integer status = readAsyncStatus(response)
        if (status != 200) {
            String detail = readAsyncError(response)
            throw new RuntimeException("GitHub returned HTTP ${status ?: 'unknown'}${detail ? ': ' + detail : ''}.")
        }

        updateOperation("install", operationToken, "Download complete. Validating the Dashboard Layout Studio HTML…")
        String html = responseBodyAsText(response?.data)
        log.info "DLS download completed with HTTP status ${status}; received ${html?.length() ?: 0} characters."

        validateDownloadedHtml(html)

        updateOperation("install", operationToken, "Validation passed. Saving the file to Hubitat File Manager…")
        uploadHubFile(DLS_FILE_NAME, html.getBytes("UTF-8"))

        updateOperation("install", operationToken, "The file was saved. Verifying the File Manager entry…")
        Map finalStatus = getDlsFileStatus()
        if (!(finalStatus.available as Boolean) || !(finalStatus.exists as Boolean)) {
            throw new RuntimeException("Hubitat did not report the uploaded file in File Manager.")
        }

        finishOperation(operationToken, "Dashboard Layout Studio was installed successfully.", "success")
        log.info "Installed ${DLS_FILE_NAME} from ${DLS_DOWNLOAD_URL}."
    } catch (Exception exception) {
        log.error "Dashboard Layout Studio installation failed: ${exception}"
        finishOperation(operationToken, "Installation failed: ${exception.message ?: 'Unknown error'}", "error")
    }
}


private void beginRemoveDls() {
    recoverStaleOperation()
    clearMessage()

    if (isOperationActive()) {
        setMessage("Another launcher operation is already in progress.", "info")
        return
    }

    try {
        Map initialStatus = getDlsFileStatus()
        if (!(initialStatus.available as Boolean)) {
            throw new RuntimeException("Hubitat File Manager could not be read.")
        }
        if (!(initialStatus.exists as Boolean)) {
            setMessage("Dashboard Layout Studio has already been removed.", "info")
            return
        }

        startOperation("remove", "Preparing to remove the DLS HTML file…")
        runIn(1, "performRemoveDls", [overwrite: true])
        log.info "Scheduled Dashboard Layout Studio removal."
    } catch (Exception exception) {
        log.error "Unable to start Dashboard Layout Studio removal: ${exception}"
        clearOperation()
        setMessage("Removal failed to start: ${exception.message ?: 'Unknown error'}", "error")
    }
}


def performRemoveDls() {
    String operationToken = (state.operationToken ?: "") as String
    if (!operationMatches("remove", operationToken)) {
        return
    }

    try {
        updateOperation("remove", operationToken, "Deleting the DLS HTML file from Hubitat File Manager…")
        Map initialStatus = getDlsFileStatus()
        if (!(initialStatus.available as Boolean)) {
            throw new RuntimeException("Hubitat File Manager could not be read.")
        }
        if (initialStatus.exists as Boolean) {
            deleteHubFile(DLS_FILE_NAME)
        }

        updateOperation("remove", operationToken, "The delete request completed. Verifying removal…")
        Map finalStatus = getDlsFileStatus()
        if (!(finalStatus.available as Boolean)) {
            throw new RuntimeException("Hubitat File Manager could not be read after removal.")
        }
        if (finalStatus.exists as Boolean) {
            throw new RuntimeException("The file is still present in Hubitat File Manager.")
        }

        finishOperation(operationToken, "Dashboard Layout Studio was removed.", "success")
        log.info "Removed ${DLS_FILE_NAME} from Hubitat File Manager."
    } catch (Exception exception) {
        log.error "Dashboard Layout Studio removal failed: ${exception}"
        finishOperation(operationToken, "Removal failed: ${exception.message ?: 'Unknown error'}", "error")
    }
}


private Integer readAsyncStatus(response) {
    Integer status = null

    try {
        status = response?.status as Integer
    } catch (Exception ignored) {
        // Try the documented accessor below.
    }

    if (status == null) {
        try {
            status = response?.getStatus() as Integer
        } catch (Exception ignored) {
            // Leave the status unknown.
        }
    }

    return status
}


private String readAsyncError(response) {
    try {
        if (response?.hasError()) {
            return (response?.getErrorMessage() ?: "Request failed") as String
        }
    } catch (Exception ignored) {
        // No additional error detail is available.
    }
    return ""
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

    try {
        String textValue = data.text as String
        if (textValue != null) {
            return textValue
        }
    } catch (Exception ignored) {
        // Try the Groovy getText extension next.
    }

    try {
        String textValue = data.getText("UTF-8") as String
        if (textValue != null) {
            return textValue
        }
    } catch (Exception ignored) {
        // Fall back to a normal string representation.
    }

    String fallback = data.toString()
    if (fallback ==~ /(?s).*@[0-9a-fA-F]+$/) {
        throw new RuntimeException("Hubitat returned an unreadable response body.")
    }
    return fallback
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


private String startOperation(String operation, String message) {
    String operationToken = now().toString()
    state.operation = operation
    state.operationToken = operationToken
    state.operationStartedAt = now()
    state.operationMessage = message
    return operationToken
}


private void updateOperation(String operation, String operationToken, String message) {
    if (operationMatches(operation, operationToken)) {
        state.operationMessage = message
    }
}


private void finishOperation(String operationToken, String message, String type) {
    if (operationToken && operationToken != (state.operationToken ?: "") as String) {
        return
    }

    clearOperation()
    setMessage(message, type)
}


private Boolean operationMatches(String operation, String operationToken) {
    return operationToken &&
        operation == (state.operation ?: "") as String &&
        operationToken == (state.operationToken ?: "") as String
}


private Boolean isOperationActive(String requestedOperation = null) {
    String currentOperation = (state.operation ?: "") as String
    if (!currentOperation) {
        return false
    }
    return requestedOperation == null || currentOperation == requestedOperation
}


private void recoverStaleOperation() {
    String currentOperation = (state.operation ?: "") as String
    if (!currentOperation) {
        return
    }

    Long startedAt = state.operationStartedAt ? (state.operationStartedAt as Long) : 0L
    if (startedAt > 0L && (now() - startedAt) > OPERATION_TIMEOUT_MS) {
        String label = currentOperation == "remove" ? "Removal" : "Installation"
        clearOperation()
        setMessage("${label} timed out before completion. Check the Hubitat log for details and try again.", "error")
    }
}


private void clearOperation() {
    state.remove("operation")
    state.remove("operationToken")
    state.remove("operationStartedAt")
    state.remove("operationMessage")
}


private void setMessage(String message, String type) {
    state.lastMessage = message
    state.lastMessageType = type
}


private void clearMessage() {
    state.remove("lastMessage")
    state.remove("lastMessageType")
}


private String launcherStyles() {
    return """
        <style>
            .dls-launcher-container {
                padding-top:0 !important;
            }
            .dls-launcher-container .mdl-grid {
                padding-top:0 !important;
                padding-bottom:0 !important;
            }
            .dls-launcher-container .mdl-cell {
                margin-top:1px !important;
                margin-bottom:1px !important;
            }
            .dls-launcher-container .mdl-card {
                min-height:0 !important;
                margin-bottom:1px !important;
            }
            .dls-launcher-container .mdl-card__title {
                min-height:0 !important;
                padding-top:2px !important;
                padding-bottom:2px !important;
            }
            .dls-launcher-container .mdl-card__supporting-text {
                padding-top:2px !important;
                padding-bottom:2px !important;
            }
            .dls-launcher-container .mdl-card__supporting-text > p {
                margin:0 !important;
            }
            .dls-status-card,
            .dls-status-card *,
            .dls-message-card,
            .dls-message-card *,
            .dls-warning-card,
            .dls-warning-card *,
            .dls-progress,
            .dls-progress * {
                -webkit-text-fill-color:currentColor !important;
                text-shadow:none !important;
            }
            .dls-status-card,
            .dls-message-card,
            .dls-warning-card,
            .dls-progress {
                box-sizing:border-box;
                width:100%;
                max-width:760px;
                margin:0 0 6px 0 !important;
            }
            .dls-status-card {
                display:flex;
                align-items:center;
                gap:8px;
                min-height:38px;
                padding:7px 10px;
                border:1px solid #e0e3e7;
                border-left-width:4px;
                border-radius:5px;
                background:#f7f8fa;
                color:#263238 !important;
            }
            .dls-status-main {
                display:flex;
                align-items:center;
                gap:7px;
                min-width:0;
            }
            .dls-status-dot {
                width:9px;
                height:9px;
                flex:0 0 9px;
                border-radius:50%;
                background:currentColor;
            }
            .dls-status-label {
                font-size:14px;
                font-weight:700;
                line-height:1.2;
                white-space:nowrap;
            }
            .dls-version {
                margin-left:auto;
                color:#607d8b !important;
                font-size:11px;
                line-height:1.2;
                white-space:nowrap;
            }
            .dls-message-card,
            .dls-warning-card {
                padding:7px 10px;
                border-left:4px solid;
                border-radius:5px;
                font-size:12.5px;
                line-height:1.3;
            }
            .dls-launch-wrap {
                display:inline-block;
                margin:1px 6px 1px 0 !important;
                padding:0 !important;
            }
            a.dls-launch-button,
            a.dls-launch-button:link,
            a.dls-launch-button:visited,
            a.dls-launch-button:hover,
            a.dls-launch-button:active,
            a.dls-launch-button * {
                color:#ffffff !important;
                -webkit-text-fill-color:#ffffff !important;
                text-shadow:none !important;
            }
            a.dls-launch-button {
                display:inline-flex !important;
                align-items:center !important;
                justify-content:center !important;
                box-sizing:border-box !important;
                min-height:34px !important;
                margin:0 !important;
                padding:0 14px !important;
                border-radius:5px !important;
                background:#1976d2 !important;
                color:#ffffff !important;
                font-size:12.5px !important;
                font-weight:600 !important;
                line-height:1 !important;
                text-align:center !important;
                text-decoration:none !important;
                box-shadow:0 1px 3px rgba(0,0,0,.20);
            }
            .dls-launcher-container .mdl-card:has(.dls-launch-wrap),
            .dls-launcher-container .mdl-cell:has(.dls-launch-wrap),
            .dls-launcher-container .mdl-card:has([name*="requestRemoveDlsButton"]),
            .dls-launcher-container .mdl-cell:has([name*="requestRemoveDlsButton"]),
            .dls-launcher-container .mdl-card:has([id*="requestRemoveDlsButton"]),
            .dls-launcher-container .mdl-cell:has([id*="requestRemoveDlsButton"]),
            .dls-launcher-container .mdl-card:has([name*="confirmRemoveDlsButton"]),
            .dls-launcher-container .mdl-cell:has([name*="confirmRemoveDlsButton"]),
            .dls-launcher-container .mdl-card:has([name*="cancelRemoveDlsButton"]),
            .dls-launcher-container .mdl-cell:has([name*="cancelRemoveDlsButton"]) {
                width:auto !important;
                min-height:0 !important;
                margin:1px 6px 1px 0 !important;
                padding:0 !important;
                display:inline-block !important;
                vertical-align:top !important;
                background:transparent !important;
                box-shadow:none !important;
            }
            .dls-launcher-container .mdl-card__supporting-text:has(.dls-launch-wrap),
            .dls-launcher-container .mdl-card__supporting-text:has([name*="requestRemoveDlsButton"]),
            .dls-launcher-container .mdl-card__supporting-text:has([id*="requestRemoveDlsButton"]),
            .dls-launcher-container .mdl-card__supporting-text:has([name*="confirmRemoveDlsButton"]),
            .dls-launcher-container .mdl-card__supporting-text:has([name*="cancelRemoveDlsButton"]) {
                width:auto !important;
                margin:0 !important;
                padding:0 !important;
            }
            [name*="installDlsButton"],
            [id*="installDlsButton"],
            [name*="requestRemoveDlsButton"],
            [id*="requestRemoveDlsButton"],
            [name*="confirmRemoveDlsButton"],
            [id*="confirmRemoveDlsButton"],
            [name*="cancelRemoveDlsButton"],
            [id*="cancelRemoveDlsButton"] {
                color:#ffffff !important;
                -webkit-text-fill-color:#ffffff !important;
                text-shadow:none !important;
            }
            input[name*="installDlsButton"],
            input[id*="installDlsButton"],
            input[name*="requestRemoveDlsButton"],
            input[id*="requestRemoveDlsButton"],
            input[name*="confirmRemoveDlsButton"],
            input[id*="confirmRemoveDlsButton"],
            input[name*="cancelRemoveDlsButton"],
            input[id*="cancelRemoveDlsButton"],
            button[name*="installDlsButton"],
            button[id*="installDlsButton"],
            button[name*="requestRemoveDlsButton"],
            button[id*="requestRemoveDlsButton"],
            button[name*="confirmRemoveDlsButton"],
            button[id*="confirmRemoveDlsButton"],
            button[name*="cancelRemoveDlsButton"],
            button[id*="cancelRemoveDlsButton"] {
                box-sizing:border-box !important;
                min-height:34px !important;
                padding:0 13px !important;
                border-radius:5px !important;
                font-size:12.5px !important;
                font-weight:600 !important;
                line-height:34px !important;
                text-align:center !important;
                vertical-align:middle !important;
                cursor:pointer !important;
                box-shadow:0 1px 3px rgba(0,0,0,.20);
            }
            .dls-progress {
                display:block;
                padding:8px 10px;
                border-left:4px solid #1976d2;
                border-radius:5px;
                background:#e3f2fd;
                color:#17324d !important;
                box-shadow:0 1px 2px rgba(0,0,0,.08);
            }
            .dls-progress.dls-remove-progress {
                border-left-color:#b71c1c;
                background:#ffebee;
                color:#4a1717 !important;
            }
            .dls-progress-head {
                display:flex;
                align-items:center;
                gap:7px;
                font-weight:700;
                color:#0d47a1 !important;
            }
            .dls-remove-progress .dls-progress-head {
                color:#8e1111 !important;
            }
            .dls-progress-spinner {
                width:14px;
                height:14px;
                flex:0 0 14px;
                border:2px solid rgba(25,118,210,.24);
                border-top-color:#1976d2;
                border-radius:50%;
                animation:dlsActionSpin .75s linear infinite;
            }
            .dls-remove-progress .dls-progress-spinner {
                border-color:rgba(183,28,28,.22);
                border-top-color:#b71c1c;
            }
            .dls-progress-text {
                margin-top:3px;
                font-size:12px;
                line-height:1.28;
            }
            .dls-progress-track {
                position:relative;
                height:5px;
                margin-top:5px;
                overflow:hidden;
                border-radius:999px;
                background:rgba(25,118,210,.17);
            }
            .dls-remove-progress .dls-progress-track {
                background:rgba(183,28,28,.14);
            }
            .dls-progress-track::after {
                content:"";
                position:absolute;
                top:0;
                bottom:0;
                width:38%;
                border-radius:999px;
                background:#1976d2;
                animation:dlsActionSweep 1.25s ease-in-out infinite;
            }
            .dls-remove-progress .dls-progress-track::after {
                background:#b71c1c;
            }
            .dls-progress-elapsed {
                margin-top:3px;
                color:#34536f !important;
                font-size:11px;
            }
            .dls-remove-progress .dls-progress-elapsed {
                color:#6f3333 !important;
            }
            @media (max-width:600px) {
                .dls-status-card {
                    align-items:flex-start;
                    flex-wrap:wrap;
                }
                .dls-version {
                    width:100%;
                    margin-left:16px;
                }
                a.dls-launch-button {
                    min-height:36px !important;
                }
            }
            @keyframes dlsActionSpin {
                to { transform:rotate(360deg); }
            }
            @keyframes dlsActionSweep {
                0% { left:-42%; }
                100% { left:104%; }
            }
        </style>
    """
}


private String operationProgressPanel(String operation) {
    Boolean removing = operation == "remove"
    String title = removing ? "Removing Dashboard Layout Studio" : "Installing Dashboard Layout Studio"
    String message = (state.operationMessage ?: (removing ? "Removal is in progress…" : "Installation is in progress…")) as String
    Long startedAt = state.operationStartedAt ? (state.operationStartedAt as Long) : now()
    Long elapsedSeconds = Math.max(0L, (now() - startedAt).intdiv(1000L))
    String elapsedLabel = "${removing ? 'Removal' : 'Installation'} in progress — ${elapsedSeconds} second${elapsedSeconds == 1L ? '' : 's'} elapsed."

    return """
        <div class="dls-progress ${removing ? 'dls-remove-progress' : 'dls-install-progress'}" role="status" aria-live="polite">
            <div class="dls-progress-head">
                <span class="dls-progress-spinner" aria-hidden="true"></span>
                <span>${title}</span>
                <span class="dls-version">Launcher ${APP_VERSION} · build ${APP_BUILD}</span>
            </div>
            <div class="dls-progress-text">${escapeHtml(message)}</div>
            <div class="dls-progress-track" aria-hidden="true"></div>
            <div class="dls-progress-elapsed">${elapsedLabel}</div>
        </div>
    """
}


private String launchButton() {
    return """
        <div class="dls-launch-wrap">
            <a class="dls-launch-button"
               href="${DLS_LOCAL_PATH}"
               target="_blank"
               rel="noopener">
                Launch Dashboard Layout Studio
            </a>
        </div>
    """
}


private String statusCard(Boolean installed, Boolean available) {
    String status = !available ? "File Manager unavailable" : (installed ? "Installed" : "Not installed")
    String color = !available ? "#b71c1c" : (installed ? "#1b5e20" : "#6d4c41")

    return """
        <div class="dls-status-card" style="border-left-color:${color};">
            <div class="dls-status-main" style="color:${color} !important;">
                <span class="dls-status-dot" aria-hidden="true"></span>
                <span class="dls-status-label">${status}</span>
            </div>
            <div class="dls-version">Launcher ${APP_VERSION} · build ${APP_BUILD}</div>
        </div>
    """
}


private String warningCard(String message) {
    return """
        <div class="dls-warning-card" style="border-left-color:#b71c1c; background:#ffebee; color:#3e2723 !important;">
            <strong style="color:#7f1d1d !important;">Confirm removal</strong><br>${message}
        </div>
    """
}


private String messageCard(String message, String type) {
    Map<String, Map<String, String>> styles = [
        success: [border: "#1b5e20", background: "#e8f5e9", text: "#173b1a"],
        error:   [border: "#b71c1c", background: "#ffebee", text: "#4a1717"],
        info:    [border: "#1565c0", background: "#e3f2fd", text: "#17324d"]
    ]

    Map<String, String> style = styles[type] ?: styles.info
    return """
        <div class="dls-message-card" style="border-left-color:${style.border}; background:${style.background}; color:${style.text} !important;">
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
