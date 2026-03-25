(function () {
  const TOKEN_KEY = "auth_token";
  const LOGIN_PAGE = "login.html";

  function getToken() {
    return localStorage.getItem(TOKEN_KEY) || "";
  }

  function setToken(token) {
    if (token) localStorage.setItem(TOKEN_KEY, token);
  }

  function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
  }

  function buildQuery(query) {
    const params = new URLSearchParams();
    if (!query) return "";
    Object.keys(query).forEach((k) => {
      const v = query[k];
      if (v === undefined || v === null || v === "") return;
      params.set(k, String(v));
    });
    const s = params.toString();
    return s ? `?${s}` : "";
  }

  function tryParseJson(text) {
    if (!text) return null;
    try {
      return JSON.parse(text);
    } catch {
      return null;
    }
  }

  function redirectToLogin() {
    clearToken();
    window.location.href = LOGIN_PAGE;
  }

  async function apiRequest(path, options) {
    const {
      method = "GET",
      query,
      body,
      auth = true,
    } = options || {};

    const url = `${path}${buildQuery(query)}`;
    const headers = {};
    if (body !== undefined && body !== null) {
      headers["Content-Type"] = "application/json";
    }
    if (auth) {
      const token = getToken();
      if (token) headers["authorization"] = token;
    }

    const res = await fetch(url, {
      method,
      headers,
      body: body !== undefined && body !== null ? JSON.stringify(body) : undefined,
    });

    const text = await res.text();
    const json = tryParseJson(text);

    if (!res.ok) {
      if (res.status === 401) redirectToLogin();
      const msg =
        (json && (json.errorMsg || json.message)) ||
        text ||
        `请求失败：${res.status}`;
      throw new Error(msg);
    }

    return json;
  }

  async function api(path, options) {
    const json = await apiRequest(path, options);
    if (!json || typeof json.success !== "boolean") return json;
    if (!json.success) throw new Error(json.errorMsg || "请求失败");
    return json.data;
  }

  window.SystemApi = {
    getToken,
    setToken,
    clearToken,
    apiRequest,
    api,
  };
})();

