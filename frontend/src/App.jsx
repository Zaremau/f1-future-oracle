import { BrowserRouter, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import GrandPrixDetailPage from './pages/GrandPrixDetailPage';

function App() {
  return (
    <BrowserRouter>
      <div className="container">
        <header className="app-header">
          {/* Стильный текстовый логотип F1 */}
          <div className="f1-text-logo">
            F<span>1</span>
          </div>
          <h1 className="main-title">Future Oracle</h1>
        </header>
        
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/grand-prix/:id" element={<GrandPrixDetailPage />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;