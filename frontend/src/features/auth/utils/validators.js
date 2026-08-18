export const isValidEmail = (value) => /^\S+@\S+\.\S+$/.test(value || "");

export const isStrongPassword = (value) =>
    value?.length >= 8 &&
    /[A-Z]/.test(value) &&
    /[a-z]/.test(value) &&
    /[0-9]/.test(value);
