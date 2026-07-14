/**
 * Dashboard Layout Studio Launcher
 *
 * Installs the latest stable Dashboard Layout Studio HTML release into
 * Hubitat File Manager and provides a launch link. DLS manages its own
 * updates after the initial installation.
 *
 * Version: 1.0.8
 * Build: 009
 */

import groovy.transform.Field

@Field static final String APP_VERSION = "1.0.8"
@Field static final String APP_BUILD = "009"
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

            if (!(fileStatus.available as Boolean)) {
                paragraph messageCard(
                    "Hubitat File Manager could not be read. Installation and removal controls are disabled until File Manager is available.",
                    "error"
                )
            }

            if (state.lastMessage) {
                paragraph messageCard(
                    state.lastMessage as String,
                    (state.lastMessageType ?: "info") as String
                )
            }
        }

        if ((fileStatus.available as Boolean) && !installed) {
            section("Install") {
                paragraph copyCard("The launcher will download the latest stable DLS release from GitHub and save it in Hubitat File Manager as <code>${DLS_FILE_NAME}</code>.")
                paragraph installationProgressPanel()
                input(
                    name: "installDlsButton",
                    type: "button",
                    title: '<span style="color:#ffffff !important; font-weight:600;">Install Dashboard Layout Studio</span>',
                    backgroundColor: "#1976d2"
                )
                paragraph copyCard("Launcher version ${APP_VERSION} (build ${APP_BUILD}). DLS performs its own update checks after it has been installed.")
            }
        } else if ((fileStatus.available as Boolean) && installed) {
            section("Actions") {
                paragraph launchButton()
                href(
                    name: "removeDlsLink",
                    title: removeLinkTitle(),
                    page: "removeDlsPage"
                )
                paragraph copyCard("Launcher version ${APP_VERSION} (build ${APP_BUILD}). DLS performs its own update checks after it has been installed.")
            }
        } else {
            section("Launcher information") {
                paragraph copyCard("Launcher version ${APP_VERSION} (build ${APP_BUILD}). DLS performs its own update checks after it has been installed.")
            }
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

            if (!(fileStatus.available as Boolean)) {
                paragraph messageCard(
                    "Hubitat File Manager could not be read. No file was removed.",
                    "error"
                )
            } else if (installed) {
                paragraph warningCard(
                    "This deletes <code>${DLS_FILE_NAME}</code> from Hubitat File Manager. " +
                    "The launcher app will remain installed and will return to its Install state."
                )
                paragraph removalProgressPanel()
                input(
                    name: "confirmRemoveDlsButton",
                    type: "button",
                    title: '<span style="color:#ffffff !important; font-weight:600;">Confirm Removal</span>',
                    backgroundColor: "#b71c1c"
                )
            } else {
                paragraph messageCard("Dashboard Layout Studio has been removed.", "success")
                paragraph copyCard("Select <strong>Next</strong> to return to the launcher.")
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
            /* Keep the launcher compact and readable regardless of the active
               Hubitat theme. All custom copy is placed on a light surface with
               an explicit dark foreground. */
            .dls-copy,
            .dls-copy *,
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

            .mdl-card {
                min-height:0 !important;
                margin-bottom:8px !important;
            }
            .mdl-card__title {
                min-height:0 !important;
                padding-top:8px !important;
                padding-bottom:4px !important;
            }
            .mdl-card__supporting-text {
                padding-top:7px !important;
                padding-bottom:7px !important;
            }
            .mdl-card__supporting-text > p {
                margin-top:3px !important;
                margin-bottom:3px !important;
            }

            .dls-copy {
                margin:0 !important;
                padding:8px 10px;
                border:1px solid #d7dde5;
                border-radius:5px;
                background:#f8fafc;
                color:#1f2937 !important;
                font-size:13px;
                line-height:1.38;
            }

            /* Hubitat adds generous paragraph and section spacing. Tighten only
               the generated areas used by this launcher. */
            .mdl-card__supporting-text p:has(.dls-copy),
            .mdl-card__supporting-text p:has(.dls-status-card),
            .mdl-card__supporting-text p:has(.dls-message-card),
            .mdl-card__supporting-text p:has(.dls-warning-card),
            .mdl-card__supporting-text p:has(.dls-progress),
            .mdl-card__supporting-text p:has(.dls-launch-wrap) {
                margin-top:0 !important;
                margin-bottom:0 !important;
            }
            .dls-launch-wrap { margin:0 !important; padding:2px 0 !important; }

            /* Hubitat themes apply foreground colors to generated controls and
               nested spans. Force a readable button label everywhere. */
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
                display:inline-flex !important;
                align-items:center !important;
                justify-content:center !important;
                box-sizing:border-box !important;
                min-height:34px !important;
                padding:0 14px !important;
                font-size:13px !important;
                line-height:1 !important;
                text-align:center !important;
                vertical-align:middle !important;
            }

            .dls-remove-button {
                display:inline-flex !important;
                align-items:center !important;
                justify-content:center !important;
                box-sizing:border-box !important;
                min-height:34px !important;
                padding:0 12px !important;
                line-height:1 !important;
                text-align:center !important;
            }

            .dls-progress {
                display:none;
                margin:5px 0 6px !important;
                padding:9px 10px;
                border-left:4px solid #1976d2;
                border-radius:4px;
                background:#e3f2fd;
                color:#17324d !important;
                box-shadow:0 1px 2px rgba(0,0,0,.10);
            }
            .dls-progress.dls-remove-progress {
                border-left-color:#b71c1c;
                background:#ffebee;
                color:#4a1717 !important;
            }
            .dls-progress.dls-visible { display:block; }
            .dls-progress-head {
                display:flex;
                align-items:center;
                gap:8px;
                font-weight:700;
                color:#0d47a1 !important;
            }
            .dls-remove-progress .dls-progress-head { color:#8e1111 !important; }
            .dls-progress-spinner {
                width:16px;
                height:16px;
                flex:0 0 16px;
                border:3px solid rgba(25,118,210,.24);
                border-top-color:#1976d2;
                border-radius:50%;
                animation:dlsActionSpin .75s linear infinite;
            }
            .dls-remove-progress .dls-progress-spinner {
                border-color:rgba(183,28,28,.22);
                border-top-color:#b71c1c;
            }
            .dls-progress-text {
                margin-top:5px;
                font-size:12.5px;
                line-height:1.35;
            }
            .dls-progress-track {
                position:relative;
                height:6px;
                margin-top:7px;
                overflow:hidden;
                border-radius:999px;
                background:rgba(25,118,210,.17);
            }
            .dls-remove-progress .dls-progress-track { background:rgba(183,28,28,.14); }
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
            .dls-remove-progress .dls-progress-track::after { background:#b71c1c; }
            .dls-progress-elapsed {
                margin-top:5px;
                color:#34536f !important;
                font-size:11.5px;
            }
            .dls-remove-progress .dls-progress-elapsed { color:#6f3333 !important; }
            .dls-action-running,
            .dls-action-running * { cursor:progress !important; }
            .dls-busy-control { opacity:.78 !important; }

            @keyframes dlsActionSpin { to { transform:rotate(360deg); } }
            @keyframes dlsActionSweep {
                0% { left:-42%; }
                100% { left:104%; }
            }
        </style>
        <script>
            (function(){
                "use strict";
                var activeAction = "";
                var startedAt = 0;
                var timer = null;

                function actionForControl(node) {
                    var current = node;
                    while (current && current !== document) {
                        if (current.getAttribute) {
                            var name = current.getAttribute("name") || "";
                            var id = current.getAttribute("id") || "";
                            var key = name + " " + id;
                            if (key.indexOf("installDlsButton") >= 0) return "install";
                            if (key.indexOf("confirmRemoveDlsButton") >= 0) return "remove";
                        }
                        current = current.parentNode;
                    }
                    return "";
                }

                function updateElapsed() {
                    if (!activeAction || !startedAt) return;
                    var elapsed = document.getElementById("dls-" + activeAction + "-elapsed");
                    if (!elapsed) return;
                    var seconds = Math.max(0, Math.floor((Date.now() - startedAt) / 1000));
                    var verb = activeAction === "remove" ? "Removal" : "Installation";
                    elapsed.textContent = verb + " in progress — " + seconds + " second" + (seconds === 1 ? "" : "s") + " elapsed.";
                }

                function markControlBusy(action) {
                    var token = action === "remove" ? "confirmRemoveDlsButton" : "installDlsButton";
                    var controls = document.querySelectorAll('[name*="' + token + '"], [id*="' + token + '"]');
                    for (var i = 0; i < controls.length; i++) {
                        controls[i].setAttribute("aria-busy", "true");
                        controls[i].classList.add("dls-busy-control");
                    }
                }

                function showProgress(action) {
                    if (!action || activeAction) return;
                    activeAction = action;
                    startedAt = Date.now();
                    var panel = document.getElementById("dls-" + action + "-progress");
                    if (panel) panel.classList.add("dls-visible");
                    document.body.classList.add("dls-action-running");
                    markControlBusy(action);
                    updateElapsed();
                    timer = window.setInterval(updateElapsed, 1000);
                }

                /* Use the normal bubbling phase. Hubitat's own button handler
                   runs first at the control, so visual feedback cannot consume
                   or cancel the first click. Do not disable pointer events. */
                document.addEventListener("click", function(event){
                    var action = actionForControl(event.target);
                    if (action) showProgress(action);
                }, false);
            })();
        </script>
    """
}


private String installationProgressPanel() {
    return """
        <div id="dls-install-progress" class="dls-progress dls-install-progress" role="status" aria-live="polite">
            <div class="dls-progress-head">
                <span class="dls-progress-spinner" aria-hidden="true"></span>
                <span>Installing Dashboard Layout Studio</span>
            </div>
            <div class="dls-progress-text">
                Downloading, validating, saving, and verifying the stable release. Keep this page open until installation completes.
            </div>
            <div class="dls-progress-track" aria-hidden="true"></div>
            <div id="dls-install-elapsed" class="dls-progress-elapsed">Installation in progress.</div>
        </div>
    """
}


private String removalProgressPanel() {
    return """
        <div id="dls-remove-progress" class="dls-progress dls-remove-progress" role="status" aria-live="polite">
            <div class="dls-progress-head">
                <span class="dls-progress-spinner" aria-hidden="true"></span>
                <span>Removing Dashboard Layout Studio</span>
            </div>
            <div class="dls-progress-text">
                Deleting the DLS HTML file and verifying that it was removed from Hubitat File Manager.
            </div>
            <div class="dls-progress-track" aria-hidden="true"></div>
            <div id="dls-remove-elapsed" class="dls-progress-elapsed">Removal in progress.</div>
        </div>
    """
}


private String launchButton() {
    return """
        <div class="dls-launch-wrap" style="text-align:center;">
            <a class="dls-launch-button"
               href="${DLS_LOCAL_PATH}"
               target="_blank"
               rel="noopener"
               style="background:#1976d2; color:#ffffff !important;
                      -webkit-text-fill-color:#ffffff !important; text-decoration:none; border-radius:5px;
                      font-weight:600; box-shadow:0 2px 4px rgba(0,0,0,.22);">
                Launch Dashboard Layout Studio
            </a>
        </div>
    """
}


private String removeLinkTitle() {
    return """
        <span class="dls-remove-button" style="background:#b71c1c;
                     color:#ffffff !important; -webkit-text-fill-color:#ffffff !important;
                     border-radius:5px; font-weight:600; font-size:13px;">
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
        <div class="dls-status-card" style="border-left:4px solid ${color}; background:#f5f5f5; color:#263238 !important; padding:9px 11px; border-radius:4px;">
            <div style="font-size:17px; font-weight:600; color:${color} !important;">${status}</div>
            <div style="margin-top:2px; color:#263238 !important; font-size:13px; line-height:1.35;">${detail}</div>
        </div>
    """
}


private String warningCard(String message) {
    return """
        <div class="dls-warning-card" style="border-left:4px solid #b71c1c; background:#ffebee; color:#3e2723 !important; padding:9px 11px; border-radius:4px; line-height:1.38;">
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
        <div class="dls-message-card" style="border-left:4px solid ${style.border}; background:${style.background}; color:${style.text} !important; padding:9px 11px; border-radius:4px; line-height:1.38;">
            ${escapeHtml(message)}
        </div>
    """
}


private String copyCard(String html) {
    return "<div class=\"dls-copy\">${html ?: ''}</div>"
}


private String escapeHtml(String value) {
    return (value ?: "")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
        .replace("'", "&#39;")
}
