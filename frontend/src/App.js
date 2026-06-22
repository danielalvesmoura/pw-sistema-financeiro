import logo from './logo.svg';
import './App.css';
import {BrowserRouter, Route, Routes} from 'react-router-dom';
import Login from './features/auth/pages/LoginPage/LoginPage.jsx';
import Register from './features/auth/pages/RegisterPage/RegisterPage.jsx';

function App() {
  return (
    <div className="App">
      <BrowserRouter> 
        <Routes>
          <Route path='/' element={<Login/>}/>
          <Route path='/cadastrar-conta' element={<Register />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
