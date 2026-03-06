// src/utils/auth.ts
export const getToken = () => localStorage.getItem('token');
export const setToken = (token: string,rememberMe:boolean) => {
    if(rememberMe){
    localStorage.setItem('token', token);
}else{
    sessionStorage.setItem('token', token);
}
};
export const removeToken = () => {
    localStorage.removeItem('token');
    sessionStorage.removeItem('token');
};