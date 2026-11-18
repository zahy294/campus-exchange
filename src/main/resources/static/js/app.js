/*
 * Campus Exchange: small progressive enhancements. Every page works without this file.
 *  - <details data-menu> dropdowns close on outside click / Escape
 *  - flash messages can be dismissed
 *  - forms with data-confirm ask first (native <dialog>)
 *  - submit buttons are disabled after the first click (no double posts)
 *  - textareas/inputs with data-counter show remaining characters
 */
(function () {
    "use strict";

    // Dropdown menus
    const menus = () => document.querySelectorAll("details[data-menu]");
    document.addEventListener("click", (event) => {
        menus().forEach((menu) => {
            if (menu.open && !menu.contains(event.target)) menu.open = false;
        });
    });
    document.addEventListener("keydown", (event) => {
        if (event.key !== "Escape") return;
        menus().forEach((menu) => {
            if (menu.open) {
                menu.open = false;
                menu.querySelector("summary")?.focus();
            }
        });
    });
    // Only one menu open at a time
    document.addEventListener("toggle", (event) => {
        const opened = event.target;
        if (!(opened instanceof HTMLDetailsElement) || !opened.matches("[data-menu]") || !opened.open) return;
        menus().forEach((menu) => { if (menu !== opened) menu.open = false; });
    }, true);

    // Dismissible flash messages
    document.addEventListener("click", (event) => {
        const button = event.target.closest("[data-dismiss]");
        if (button) button.closest(".flash")?.remove();
    });

    // Confirmation dialog for destructive or important actions
    const dialog = document.getElementById("confirm-dialog");
    document.addEventListener("submit", (event) => {
        const form = event.target;
        if (!(form instanceof HTMLFormElement)) return;

        if (form.dataset.confirm && !form.dataset.confirmed && dialog && typeof dialog.showModal === "function") {
            event.preventDefault();
            dialog.querySelector("#confirm-title").textContent = form.dataset.confirmTitle || "Are you sure?";
            dialog.querySelector("#confirm-text").textContent = form.dataset.confirm;
            const ok = dialog.querySelector("#confirm-ok");
            ok.textContent = form.dataset.confirmAction || "Confirm";
            ok.className = "btn " + (form.dataset.confirmTone === "neutral" ? "btn--primary" : "btn--danger");
            dialog.returnValue = "";
            dialog.showModal();
            dialog.addEventListener("close", function onClose() {
                dialog.removeEventListener("close", onClose);
                if (dialog.returnValue === "ok") {
                    form.dataset.confirmed = "true";
                    form.requestSubmit ? form.requestSubmit() : form.submit();
                }
            });
            return;
        }

        // Prevent double submission
        if (form.method.toLowerCase() === "post") {
            const submitter = event.submitter || form.querySelector("[type=submit]");
            if (form.dataset.submitting) {
                event.preventDefault();
                return;
            }
            form.dataset.submitting = "true";
            if (submitter && submitter.tagName === "BUTTON") {
                submitter.classList.add("is-busy");
                submitter.setAttribute("aria-busy", "true");
                if (submitter.dataset.busyLabel) {
                    const label = submitter.querySelector("span") || submitter;
                    label.textContent = submitter.dataset.busyLabel;
                }
            }
            // If the user navigates back (bfcache), allow submitting again.
            window.addEventListener("pageshow", () => {
                delete form.dataset.submitting;
                submitter?.classList.remove("is-busy");
                submitter?.removeAttribute("aria-busy");
            }, { once: true });
        }
    });

    // Character counters
    document.querySelectorAll("[data-counter]").forEach((field) => {
        const max = Number(field.getAttribute("maxlength"));
        const out = document.getElementById(field.dataset.counter);
        if (!max || !out) return;
        const update = () => {
            const left = max - field.value.length;
            out.textContent = left + " characters left";
            out.classList.toggle("is-near", left <= Math.max(20, max * 0.05));
        };
        field.addEventListener("input", update);
        update();
    });
})();
