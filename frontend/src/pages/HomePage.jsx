import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';

function HomePage() {
  const [grandPrixList, setGrandPrixList] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axios.get('http://localhost:8080/api/grand-prix')
      .then(response => {
        setGrandPrixList(response.data);
        setLoading(false);
      })
      .catch(error => {
        console.error("Ошибка загрузки данных:", error);
        setLoading(false);
      });
  }, []);

  if (loading) return <h2>Загрузка календаря...</h2>;

  return (
    <div>
      <h2>Календарь сезона 2026</h2>
      <div className="gp-grid">
        {grandPrixList.map(gp => (
          <Link to={`/grand-prix/${gp.id}`} key={gp.id} className="gp-card">
            <h3>{gp.name}</h3>
            <p>📍 {gp.country}</p>
            <span className={`status-badge ${gp.stage === 'RACE_DONE' ? 'status-done' : 'status-upcoming'}`}>
              {gp.stage === 'RACE_DONE' ? 'Гонка завершена' : 'Предстоит гонка'}
            </span>
          </Link>
        ))}
      </div>
    </div>
  );
}

export default HomePage;