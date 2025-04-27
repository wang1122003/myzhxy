import {defineStore} from 'pinia';
import {ref} from 'vue';

export const useUserStore = defineStore('user', () => {
    // State
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'));
    const token = ref(localStorage.getItem('token') || '');

    // Computed (Getters)
    const isLoggedIn = () => !!token.value;

    // Actions
    function setUserInfo(newUserInfo) {
        userInfo.value = newUserInfo;
        localStorage.setItem('userInfo', JSON.stringify(newUserInfo));
    }

    function setToken(newToken) {
        token.value = newToken;
        localStorage.setItem('token', newToken);
    }

    function logout() {
        userInfo.value = {};
        token.value = '';
        localStorage.removeItem('userInfo');
        localStorage.removeItem('token');
        // Optionally redirect to login page
        // router.push('/login');
    }

    return {
        userInfo,
        token,
        isLoggedIn,
        setUserInfo,
        setToken,
        logout
    };
}); 