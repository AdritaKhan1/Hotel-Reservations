// Session/auth state + the login-or-signup modal, shared by every page.
const Auth = (() => {
  let current = { authenticated: false };

  function escapeHtml(s) {
    const div = document.createElement("div");
    div.textContent = s == null ? "" : String(s);
    return div.innerHTML;
  }

  async function refresh() {
    current = await Api.get("/api/auth/me");
    renderWidget();
    return current;
  }

  function isAuthenticated() {
    return current.authenticated === true;
  }

  function renderWidget() {
    const el = document.getElementById("authWidget");
    if (!el) return;
    if (current.authenticated) {
      const name = current.firstName || current.email;
      el.innerHTML =
        '<span class="muted">Hi, ' + escapeHtml(name) + '</span>' +
        '<button class="secondary" id="logoutBtn" type="button">Log out</button>';
      document.getElementById("logoutBtn").onclick = async () => {
        await Api.post("/api/auth/logout");
        await refresh();
      };
    } else {
      el.innerHTML = '<button class="secondary" id="loginBtn" type="button">Log in</button>';
      document.getElementById("loginBtn").onclick = () => openModal({});
    }
  }

  /**
   * Shows the login/signup modal. `options.onSuccess(user)` fires after a
   * successful local login or signup.
   */
  function openModal(options) {
    const opts = options || {};

    const backdrop = document.createElement("div");
    backdrop.className = "modal-backdrop";
    backdrop.innerHTML =
      '<div class="modal" role="dialog" aria-modal="true">' +
      '  <button class="close" type="button" aria-label="Close">&times;</button>' +
      '  <h2>Welcome</h2>' +
      '  <p class="subtitle">Log in or create an account to complete your reservation.</p>' +
      '  <div class="tabs">' +
      '    <button type="button" class="active" data-tab="login">Log in</button>' +
      '    <button type="button" data-tab="signup">Sign up</button>' +
      '  </div>' +
      '  <div id="authAlert"></div>' +
      '  <form id="loginForm">' +
      '    <div class="form-grid">' +
      '      <div class="field span-2"><label>Email</label><input type="email" name="email" required autocomplete="email"></div>' +
      '      <div class="field span-2"><label>Password</label><input type="password" name="password" required autocomplete="current-password"></div>' +
      '    </div>' +
      '    <button class="full" type="submit" style="margin-top:14px;">Log in</button>' +
      '  </form>' +
      '  <form id="signupForm" style="display:none;">' +
      '    <div class="form-grid">' +
      '      <div class="field"><label>First name</label><input name="firstName" required></div>' +
      '      <div class="field"><label>Last name</label><input name="lastName" required></div>' +
      '      <div class="field span-2"><label>Email</label><input type="email" name="email" required autocomplete="email"></div>' +
      '      <div class="field span-2"><label>Password</label><input type="password" name="password" minlength="8" required autocomplete="new-password">' +
      '        <div class="error" style="color:var(--muted)">At least 8 characters</div></div>' +
      '    </div>' +
      '    <button class="full" type="submit" style="margin-top:14px;">Create account</button>' +
      '  </form>' +
      '  <div class="divider">or</div>' +
      '  <button class="google full" type="button" id="googleBtn">' +
      '    <svg width="18" height="18" viewBox="0 0 18 18" aria-hidden="true"><path fill="#4285F4" d="M17.64 9.2c0-.64-.06-1.25-.16-1.84H9v3.48h4.84a4.14 4.14 0 0 1-1.8 2.72v2.26h2.9c1.7-1.57 2.7-3.88 2.7-6.62z"/><path fill="#34A853" d="M9 18c2.43 0 4.47-.8 5.96-2.18l-2.9-2.26c-.81.54-1.85.86-3.06.86-2.35 0-4.34-1.59-5.05-3.72H.95v2.33A9 9 0 0 0 9 18z"/><path fill="#FBBC05" d="M3.95 10.7A5.4 5.4 0 0 1 3.67 9c0-.59.1-1.17.28-1.7V4.97H.95A9 9 0 0 0 0 9c0 1.45.35 2.83.95 4.03l3-2.33z"/><path fill="#EA4335" d="M9 3.58c1.32 0 2.51.46 3.44 1.35l2.58-2.58C13.46.89 11.43 0 9 0A9 9 0 0 0 .95 4.97l3 2.33C4.66 5.17 6.65 3.58 9 3.58z"/></svg>' +
      '    Continue with Google' +
      '  </button>' +
      '</div>';
    document.body.appendChild(backdrop);

    const close = () => backdrop.remove();
    backdrop.querySelector(".close").onclick = close;
    backdrop.addEventListener("click", (e) => { if (e.target === backdrop) close(); });

    const tabs = backdrop.querySelectorAll(".tabs button");
    const loginForm = backdrop.querySelector("#loginForm");
    const signupForm = backdrop.querySelector("#signupForm");
    tabs.forEach((tab) => {
      tab.onclick = () => {
        tabs.forEach((t) => t.classList.remove("active"));
        tab.classList.add("active");
        const isLogin = tab.dataset.tab === "login";
        loginForm.style.display = isLogin ? "" : "none";
        signupForm.style.display = isLogin ? "none" : "";
        alertBox("");
      };
    });

    function alertBox(message, kind) {
      const el = backdrop.querySelector("#authAlert");
      el.innerHTML = message ? '<div class="alert ' + (kind || "error") + '">' + escapeHtml(message) + '</div>' : "";
    }

    async function handleSubmit(form, url, extractBody) {
      form.addEventListener("submit", async (e) => {
        e.preventDefault();
        alertBox("");
        const submitBtn = form.querySelector("button[type=submit]");
        submitBtn.disabled = true;
        try {
          const data = new FormData(form);
          const user = await Api.post(url, extractBody(data));
          current = user;
          renderWidget();
          close();
          if (opts.onSuccess) opts.onSuccess(user);
        } catch (err) {
          alertBox(err.message);
        } finally {
          submitBtn.disabled = false;
        }
      });
    }

    handleSubmit(loginForm, "/api/auth/login", (d) => ({
      email: d.get("email"), password: d.get("password")
    }));
    handleSubmit(signupForm, "/api/auth/register", (d) => ({
      email: d.get("email"), password: d.get("password"),
      firstName: d.get("firstName"), lastName: d.get("lastName")
    }));

    // Demo project: this doesn't perform a real Google login (no OAuth
    // provider is wired up). It goes to an explainer page instead of quietly
    // doing nothing, or crashing against a login endpoint that isn't real.
    backdrop.querySelector("#googleBtn").onclick = () => {
      location.href = "/google-demo.html";
    };
  }

  return { refresh, isAuthenticated, openModal, renderWidget, current: () => current };
})();
