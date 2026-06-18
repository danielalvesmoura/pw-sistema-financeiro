const DEFAULT_USERS = [
  {
    id: 1,
    email: "admin@wallet.com",
    password: "admin"
  },
];

const login = (email, password) => {
  const session = {
    email: email,
    password: password
  }

  localStorage.setItem("session", JSON.stringify(session));
}

export const searchAccount = (email, password) => {

  let existentAccount = false;

  DEFAULT_USERS.forEach(account => {
    if (email === account.email && password === account.password) {
      login(email, password);
      existentAccount = true;
    }
  })

  if(existentAccount) return true;

  const registeredAccounts = localStorage.getItem("registeredAccounts") || [];

  if(registeredAccounts.length === 0) return false;

  registeredAccounts.forEach(account => {
    if(account.email === email && account.password === password) {
      login(email, password);
      existentAccount = true;
    }
  })

  if(existentAccount) {
    return true;
  } else {
    return false;
  }
}

