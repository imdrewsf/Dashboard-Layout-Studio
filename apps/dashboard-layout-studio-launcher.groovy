/**
 * Dashboard Layout Studio Launcher
 *
 * Installs the latest stable Dashboard Layout Studio HTML release into
 * Hubitat File Manager and provides a launch link. DLS manages its own
 * updates after the initial installation.
 *
 * Version: 1.0.4
 * Build: 005
 */

import groovy.transform.Field

@Field static final String APP_VERSION = "1.0.4"
@Field static final String APP_BUILD = "005"
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
    recoverStaleInstallation()

    Map fileStatus = getDlsFileStatus()
    Boolean installed = fileStatus.exists as Boolean
    Boolean installing = state.installInProgress == true

    Map pageOptions = [
        name: "mainPage",
        title: "Dashboard Layout Studio",
        install: true,
        uninstall: true
    ]
    if (installing) {
        pageOptions.refreshInterval = 2
    }

    dynamicPage(pageOptions) {
        section {
            paragraph statusCard(installed, fileStatus.available as Boolean, installing)
        }

        if (installing) {
            section("Installation progress") {
                paragraph progressCard(
                    (state.installPhase ?: "Preparing installation...") as String,
                    (state.installStep ?: 1) as Integer
                )
                paragraph "This page refreshes automatically while the installation is running."
            }
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

        if ((fileStatus.available as Boolean) && !installed && !installing) {
            section("Install") {
                paragraph "The launcher will download the latest stable DLS release from GitHub and save it in Hubitat File Manager as <code>${DLS_FILE_NAME}</code>."
                input(
                    name: "installDlsButton",
                    type: "button",
                    title: "Install Dashboard Layout Studio",
                    backgroundColor: "#1976d2"
                )
            }
        } else if ((fileStatus.available as Boolean) && installed && !installing) {
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
        unschedule("installTimeoutHandler")
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


void appButtonHandler(buttonName) {
    log.info "Dashboard Layout Studio launcher button pressed: ${buttonName}"

    switch (buttonName) {
        case "installDlsButton":
            startDlsInstallation()
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


private void startDlsInstallation() {
    clearMessage()

    if (state.installInProgress == true) {
        setMessage("Dashboard Layout Studio installation is already running.", "info")
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

        state.installInProgress = true
        state.installStartedAt = now()
        updateInstallProgress(1, "Downloading the latest stable DLS release from GitHub...")

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

        runIn(120, "installTimeoutHandler", [overwrite: true])
        asynchttpGet("dlsDownloadHandler", request, [startedAt: state.installStartedAt])
        log.info "Started asynchronous DLS download from ${DLS_DOWNLOAD_URL}."
    } catch (Exception exception) {
        finishInstallationFailure(exception)
    }
}


void dlsDownloadHandler(response, callbackData = null) {
    unschedule("installTimeoutHandler")

    if (state.installInProgress != true) {
        log.warn "Ignoring a late DLS download response because no installation is active."
        return
    }

    try {
        Integer responseStatus = response?.getStatus() as Integer
        log.info "DLS download completed with HTTP status ${responseStatus ?: 'unknown'}."

        if (responseStatus != 200) {
            String responseError = response?.getErrorMessage()
            throw new RuntimeException(
                "GitHub returned HTTP ${responseStatus ?: 'unknown'}" +
                (responseError ? ": ${responseError}" : ".")
            )
        }

        updateInstallProgress(2, "Validating the downloaded DLS release...")
        String html = response?.getData() as String
        validateDownloadedHtml(html)
        state.downloadedCharacters = html.length()

        updateInstallProgress(3, "Saving DLS to Hubitat File Manager...")
        uploadHubFile(DLS_FILE_NAME, html.getBytes("UTF-8"))

        updateInstallProgress(4, "Verifying the File Manager installation...")
        Map finalStatus = getDlsFileStatus()
        if (!(finalStatus.available as Boolean) || !(finalStatus.exists as Boolean)) {
            throw new RuntimeException("Hubitat did not report the uploaded file in File Manager.")
        }

        Integer downloadedCharacters = state.downloadedCharacters as Integer
        clearInstallProgress()
        setMessage(
            "Dashboard Layout Studio was installed successfully" +
            (downloadedCharacters ? " (${downloadedCharacters} characters)." : "."),
            "success"
        )
        log.info "Installed ${DLS_FILE_NAME} from ${DLS_DOWNLOAD_URL}."
    } catch (Exception exception) {
        finishInstallationFailure(exception)
    }
}


void installTimeoutHandler() {
    if (state.installInProgress == true) {
        finishInstallationFailure(
            new RuntimeException("The download did not complete before the installer timeout.")
        )
    }
}


private void finishInstallationFailure(Exception exception) {
    unschedule("installTimeoutHandler")
    log.error "Dashboard Layout Studio installation failed: ${exception}"
    clearInstallProgress()
    setMessage(
        "Installation failed: ${exception?.message ?: exception?.class?.simpleName ?: 'Unknown error'}",
        "error"
    )
}


private void updateInstallProgress(Integer step, String phase) {
    state.installStep = step
    state.installPhase = phase
}


private void clearInstallProgress() {
    state.remove("installInProgress")
    state.remove("installStartedAt")
    state.remove("installStep")
    state.remove("installPhase")
    state.remove("downloadedCharacters")
}


private void recoverStaleInstallation() {
    if (state.installInProgress == true && state.installStartedAt) {
        Long age = now() - (state.installStartedAt as Long)
        if (age > 180000L) {
            clearInstallProgress()
            setMessage("The previous installation did not complete. Select Install to try again.", "error")
        }
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
        List files = getHubFiles() ?: []
        Boolean exists = files.any { file ->
            String fileName = (file?.name ?: file?.fileName ?: "") as String
            fileName == DLS_FILE_NAME
        }
        return [available: true, exists: exists]
    } catch (Exception exception) {
        log.warn "Unable to read Hubitat File Manager contents: ${exception.message}"
        return [available: false, exists: false, error: exception.message]
    }
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


private String statusCard(Boolean installed, Boolean available, Boolean installing) {
    String status = installing
        ? "Installing"
        : (!available ? "File Manager unavailable" : (installed ? "Installed" : "Not installed"))
    String color = installing
        ? "#1565c0"
        : (!available ? "#b71c1c" : (installed ? "#1b5e20" : "#6d4c41"))
    String detail = installing
        ? "The launcher is downloading and installing ${DLS_FILE_NAME}."
        : (!available
            ? "The launcher could not determine whether ${DLS_FILE_NAME} is installed."
            : (installed
                ? "${DLS_FILE_NAME} is present in Hubitat File Manager."
                : "${DLS_FILE_NAME} is not present in Hubitat File Manager."))

    return """
        <div style="border-left:5px solid ${color}; background:#f5f5f5; padding:14px 16px; border-radius:4px;">
            <div style="font-size:18px; font-weight:600; color:${color};">${status}</div>
            <div style="margin-top:4px;">${detail}</div>
        </div>
    """
}


private String progressCard(String phase, Integer currentStep) {
    List<String> steps = [
        "Download release",
        "Validate download",
        "Save to File Manager",
        "Verify installation"
    ]

    String stepRows = ""
    steps.eachWithIndex { String label, Integer index ->
        Integer stepNumber = index + 1
        String marker = stepNumber < currentStep ? "&#10003;" : (stepNumber == currentStep ? "&#9679;" : "&#9675;")
        String weight = stepNumber == currentStep ? "600" : "400"
        String stepColor = stepNumber < currentStep ? "#1b5e20" : (stepNumber == currentStep ? "#1565c0" : "#666666")
        stepRows += """
            <div style="margin:5px 0; color:${stepColor}; font-weight:${weight};">
                <span style="display:inline-block; width:22px;">${marker}</span>${stepNumber}. ${label}
            </div>
        """
    }

    return """
        <style>
            @keyframes dlsLauncherSpin { to { transform: rotate(360deg); } }
        </style>
        <div style="border-left:5px solid #1565c0; background:#e3f2fd; padding:14px 16px; border-radius:4px;">
            <div style="display:flex; align-items:center; gap:10px; margin-bottom:10px;">
                <span style="display:inline-block; width:18px; height:18px; border:3px solid #90caf9;
                             border-top-color:#1565c0; border-radius:50%; animation:dlsLauncherSpin .8s linear infinite;"></span>
                <strong>${escapeHtml(phase)}</strong>
            </div>
            ${stepRows}
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
    Map styles = [
        success: [border: "#1b5e20", background: "#e8f5e9"],
        error:   [border: "#b71c1c", background: "#ffebee"],
        info:    [border: "#1565c0", background: "#e3f2fd"]
    ]

    Map style = styles[type] ?: styles.info
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
