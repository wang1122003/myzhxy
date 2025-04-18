const TokenKey = 'Authorization-Token'; // Key for storing token in localStorage

/**
 * Gets the token from localStorage.
 * @returns {string | null} The token or null if not found.
 */
export function getToken() {
    return localStorage.getItem(TokenKey);
}

/**
 * Sets the token in localStorage.
 * @param {string} token The token to store.
 */
export function setToken(token) {
    return localStorage.setItem(TokenKey, token);
}

/**
 * Removes the token from localStorage.
 */
export function removeToken() {
    return localStorage.removeItem(TokenKey);
} 