/**
 * Dashboard Layout Studio Launcher
 *
 * Installs the latest stable Dashboard Layout Studio HTML release into
 * Hubitat File Manager and provides a launch link. DLS manages its own
 * updates after the initial installation.
 *
 * Version: 1.0.7
 * Build: 008
 */

import groovy.transform.Field

@Field static final String APP_VERSION = "1.0.7"
@Field static final String APP_BUILD = "008"
@Field static final String DLS_FILE_NAME = "dashboard-layout-studio.html"
@Field static final String DLS_LOCAL_PATH = "/local/dashboard-layout-studio.html"
@Field static final String DLS_DOWNLOAD_URL = "https://raw.githubusercontent.com/imdrewsf/Dashboard-Layout-Studio/main/dashboard-layout-studio.html"


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
            paragraph launcherStyles()
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
                paragraph installationProgressPanel()
                input(
                    name: "installDlsButton",
                    type: "button",
                    title: '<span style="color:#ffffff !important; font-weight:600;">Install Dashboard Layout Studio</span>',
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
                    title: removeLinkTitle(),
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
        section {
            paragraph launcherStyles()
        }

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
                    title: '<span style="color:#ffffff !important; font-weight:600;">Confirm Removal</span>',
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
            contentType: "text/plain",
            textParser: true,
            followRedirects: true,
            timeout: 90
        ]

        httpGet(request) { response ->
            Integer status = response?.status as Integer
            if (status != 200) {
                throw new RuntimeException("GitHub returned HTTP ${status ?: 'unknown'}.")
            }
            html = responseBodyAsText(response?.data)
            log.info "DLS download completed with HTTP status ${status}; received ${html?.length() ?: 0} characters."
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

    // Hubitat may expose a text response as a String, Reader-like object, or
    // stream-like object depending on platform/parser behavior. Avoid explicit
    // Reader class references because Hubitat's sandbox rejects them.
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
        // Fall back to toString only for objects that already represent text.
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
            /* Hubitat themes apply their own foreground colors to generated
               button and href children. Target every generated wrapper/state. */
            a.dls-launch-button,
            a.dls-launch-button:link,
            a.dls-launch-button:visited,
            a.dls-launch-button:hover,
            a.dls-launch-button:active,
            a.dls-launch-button *,
            .dls-remove-button,
            .dls-remove-button *,
            [name*="installDlsButton"],
            [name*="installDlsButton"] *,
            [id*="installDlsButton"],
            [id*="installDlsButton"] *,
            [name*="confirmRemoveDlsButton"],
            [name*="confirmRemoveDlsButton"] *,
            [id*="confirmRemoveDlsButton"],
            [id*="confirmRemoveDlsButton"] *,
            [name*="removeDlsLink"],
            [name*="removeDlsLink"] *,
            [id*="removeDlsLink"],
            [id*="removeDlsLink"] *,
            [href*="removeDlsPage"],
            [href*="removeDlsPage"] * {
                color:#ffffff !important;
                -webkit-text-fill-color:#ffffff !important;
                text-shadow:none !important;
            }

            a.dls-launch-button {
                font-size:13px !important;
                line-height:1.15 !important;
            }

            .dls-install-progress {
                display:none;
                margin:12px 0 14px;
                padding:13px 14px;
                border-left:5px solid #1976d2;
                border-radius:4px;
                background:#e3f2fd;
                color:#0d2740;
                box-shadow:0 1px 3px rgba(0,0,0,.12);
            }
            .dls-install-progress.dls-visible { display:block; }
            .dls-install-progress-head {
                display:flex;
                align-items:center;
                gap:10px;
                font-weight:700;
                color:#0d47a1;
            }
            .dls-install-spinner {
                width:18px;
                height:18px;
                flex:0 0 18px;
                border:3px solid rgba(25,118,210,.25);
                border-top-color:#1976d2;
                border-radius:50%;
                animation:dlsInstallSpin .75s linear infinite;
            }
            .dls-install-progress-text {
                margin-top:7px;
                font-size:13px;
                line-height:1.45;
            }
            .dls-install-progress-track {
                position:relative;
                height:7px;
                margin-top:10px;
                overflow:hidden;
                border-radius:999px;
                background:rgba(25,118,210,.17);
            }
            .dls-install-progress-track::after {
                content:"";
                position:absolute;
                top:0;
                bottom:0;
                width:38%;
                border-radius:999px;
                background:#1976d2;
                animation:dlsInstallSweep 1.25s ease-in-out infinite;
            }
            .dls-install-elapsed {
                margin-top:7px;
                color:#34536f;
                font-size:12px;
            }
            .dls-installing,
            .dls-installing * {
                cursor:progress !important;
            }
            @keyframes dlsInstallSpin { to { transform:rotate(360deg); } }
            @keyframes dlsInstallSweep {
                0% { left:-42%; }
                100% { left:104%; }
            }
        </style>
        <script>
            (function(){
                "use strict";
                var active = false;
                var startedAt = 0;
                var timer = null;

                function findInstallControl(node) {
                    var current = node;
                    while (current && current !== document) {
                        if (current.getAttribute) {
                            var name = current.getAttribute("name") || "";
                            var id = current.getAttribute("id") || "";
                            if (name.indexOf("installDlsButton") >= 0 || id.indexOf("installDlsButton") >= 0) {
                                return current;
                            }
                        }
                        current = current.parentNode;
                    }
                    return null;
                }

                function updateElapsed() {
                    var elapsed = document.getElementById("dls-install-elapsed");
                    if (!elapsed || !startedAt) return;
                    var seconds = Math.max(0, Math.floor((Date.now() - startedAt) / 1000));
                    elapsed.textContent = "Installation in progress — " + seconds + " second" + (seconds === 1 ? "" : "s") + " elapsed.";
                }

                function showInstallProgress() {
                    if (active) return;
                    active = true;
                    startedAt = Date.now();
                    var panel = document.getElementById("dls-install-progress");
                    if (panel) panel.classList.add("dls-visible");
                    document.body.classList.add("dls-installing");
                    updateElapsed();
                    timer = window.setInterval(updateElapsed, 1000);

                    window.setTimeout(function(){
                        var controls = document.querySelectorAll('[name*="installDlsButton"], [id*="installDlsButton"]');
                        for (var i = 0; i < controls.length; i++) {
                            controls[i].setAttribute("aria-busy", "true");
                            controls[i].style.opacity = "0.78";
                            controls[i].style.pointerEvents = "none";
                        }
                    }, 0);
                }

                document.addEventListener("click", function(event){
                    if (findInstallControl(event.target)) showInstallProgress();
                }, true);
            })();
        </script>
    """
}


private String installationProgressPanel() {
    return """
        <div id="dls-install-progress" class="dls-install-progress" role="status" aria-live="polite">
            <div class="dls-install-progress-head">
                <span class="dls-install-spinner" aria-hidden="true"></span>
                <span>Installing Dashboard Layout Studio</span>
            </div>
            <div class="dls-install-progress-text">
                Downloading, validating, saving, and verifying the latest stable release. Keep this page open until installation completes.
            </div>
            <div class="dls-install-progress-track" aria-hidden="true"></div>
            <div id="dls-install-elapsed" class="dls-install-elapsed">Installation in progress.</div>
        </div>
    """
}


private String launchButton() {
    return """
        <div style="text-align:center; padding:10px 0 8px 0;">
            <a class="dls-launch-button"
               href="${DLS_LOCAL_PATH}"
               target="_blank"
               rel="noopener"
               style="display:inline-block; padding:7px 13px; background:#1976d2; color:#ffffff !important;
                      -webkit-text-fill-color:#ffffff !important; text-decoration:none; border-radius:5px;
                      font-weight:600; font-size:13px; line-height:1.15;
                      box-shadow:0 2px 4px rgba(0,0,0,.25);">
                Launch Dashboard Layout Studio
            </a>
        </div>
    """
}


private String removeLinkTitle() {
    return """
        <span class="dls-remove-button" style="display:inline-block; padding:7px 12px; background:#b71c1c;
                     color:#ffffff !important; -webkit-text-fill-color:#ffffff !important;
                     border-radius:5px; font-weight:600; font-size:13px; line-height:1.15;">
            Remove Dashboard Layout Studio
        </span>
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
