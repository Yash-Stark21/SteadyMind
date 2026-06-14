(function () {
    const TOKEN_ENDPOINT = '/auth/access-token';
    const API_PATH_PREFIX = '/api/';
    const EXPIRY_BUFFER_MS = 60 * 1000;

    let cachedAccessToken = null;
    let cachedExpiresAtMs = 0;
    let tokenRequest = null;

    function isUsableToken() {
        return cachedAccessToken && cachedExpiresAtMs - Date.now() > EXPIRY_BUFFER_MS;
    }

    function clearToken() {
        cachedAccessToken = null;
        cachedExpiresAtMs = 0;
    }

    function redirectToLogin() {
        if (window.location.pathname !== '/login') {
            window.location.assign('/login');
        }
    }

    function requestUrl(input) {
        return input instanceof Request ? input.url : input;
    }

    function isSameOriginApiRequest(input) {
        const url = new URL(requestUrl(input), window.location.href);
        return url.origin === window.location.origin && url.pathname.startsWith(API_PATH_PREFIX);
    }

    async function loadAccessToken(forceRefresh) {
        if (!forceRefresh && isUsableToken()) {
            return cachedAccessToken;
        }

        if (!forceRefresh && tokenRequest) {
            return tokenRequest;
        }

        tokenRequest = fetch(TOKEN_ENDPOINT, {
            credentials: 'same-origin',
            cache: 'no-store',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(async response => {
                if (!response.ok) {
                    throw new Error('Unable to load access token');
                }

                const token = await response.json();
                const expiresAtMs = Date.parse(token.expiresAt);
                if (!token.accessToken || Number.isNaN(expiresAtMs)) {
                    throw new Error('Invalid access token response');
                }

                cachedAccessToken = token.accessToken;
                cachedExpiresAtMs = expiresAtMs;
                return cachedAccessToken;
            })
            .catch(error => {
                clearToken();
                redirectToLogin();
                throw error;
            })
            .finally(() => {
                tokenRequest = null;
            });

        return tokenRequest;
    }

    function requestWithBearer(input, init, accessToken) {
        const requestInit = init ? { ...init } : {};
        const headers = new Headers(input instanceof Request ? input.headers : undefined);
        if (requestInit.headers) {
            new Headers(requestInit.headers).forEach((value, key) => headers.set(key, value));
        }

        headers.set('Authorization', `Bearer ${accessToken}`);
        requestInit.headers = headers;
        return new Request(input, requestInit);
    }

    async function fetchWithBearer(input, init, retried) {
        const accessToken = await loadAccessToken(false);
        const response = await fetch(requestWithBearer(input, init, accessToken));

        if (response.status === 401 && !retried) {
            clearToken();
            await loadAccessToken(true);
            return fetchWithBearer(input, init, true);
        }

        return response;
    }

    window.steadyApiFetch = function steadyApiFetch(input, init) {
        if (!isSameOriginApiRequest(input)) {
            return fetch(input, init);
        }

        return fetchWithBearer(input, init, false);
    };
})();
