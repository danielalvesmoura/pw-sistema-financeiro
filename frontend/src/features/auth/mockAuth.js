const DEFAULT_USERS = {
  adm: {
    id: 1,
    email: "admin@wallet.com",
    password: "admin"
  },
};

const searchAccount = (email, password) => {
  if (email === DEFAULT_USERS.adm.email && password === DEFAULT_USERS.adm.password) {
    login(email,senha);
  }

  const registeredAccounts = localStorage.getItem("registeredAccounts") || [];

  
}

const login = (email, password) => {
  const session = {
    email: email,
    password: password
  }

  localStorage.setItem("session", JSON.stringify(session));

  return true;
}