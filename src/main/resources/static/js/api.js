// Small fetch wrapper: JSON in, JSON out, and errors carry the API's message.
const Api = (() => {
  async function toError(response) {
    try {
      const body = await response.json();
      return new Error(body.message || response.statusText);
    } catch (e) {
      return new Error(response.statusText || ("Request failed (" + response.status + ")"));
    }
  }

  async function request(method, url, body) {
    const response = await fetch(url, {
      method,
      headers: body === undefined ? undefined : { "Content-Type": "application/json" },
      body: body === undefined ? undefined : JSON.stringify(body),
      credentials: "same-origin"
    });
    if (!response.ok) {
      throw await toError(response);
    }
    if (response.status === 204) {
      return null;
    }
    const text = await response.text();
    return text ? JSON.parse(text) : null;
  }

  return {
    get: (url) => request("GET", url),
    post: (url, body) => request("POST", url, body === undefined ? {} : body)
  };
})();
