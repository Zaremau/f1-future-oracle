import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';

function GrandPrixDetailPage() {
  const { id } = useParams();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null); // НОВОЕ состояние для ошибки

  useEffect(() => {
    setLoading(true);
    setError(null); // Сбрасываем ошибку при новом запросе
    
    axios.get(`/api/grand-prix/${id}/data`)
      .then(response => {
        setData(response.data);
        setLoading(false);
      })
      .catch(error => {
        console.error("Ошибка:", error);
        // Проверяем, вернул ли бэкенд наш JSON с сообщением
        if (error.response && error.response.data && error.response.data.message) {
          setError(error.response.data.message);
        } else if (error.response && error.response.status === 404) {
          setError("Гран-при не найден.");
        } else {
          setError("Произошла ошибка при загрузке данных. Попробуйте позже.");
        }
        setLoading(false);
      });
  }, [id]);

  // Если загружаем данные
  if (loading) return <h2>Считаем данные и прогнозы...</h2>;

  // Если произошла ошибка — показываем красивую плашку
  if (error) {
    return (
      <div>
        <Link to="/" className="back-link">← Назад к календарю</Link>
        <div className="error-banner">
          <h3>⚠️ Ошибка</h3>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  // Проверяем, что пришло: прогнозы или результаты
  const isRaceDone = data.length > 0 && data[0].actualPosition !== undefined;

  return (
    <div>
      <Link to="/" className="back-link">← Назад к календарю</Link>
      
      {!isRaceDone ? (
        <div>
          <h2>Прогноз на гонку</h2>
          <div className="predictions-grid">
            {data.map((pred, index) => (
              <div key={index} className={`prediction-card risk-${pred.riskLevel.toLowerCase()}`}>
                <div className="pred-header">
                  <span className="position-badge">P{pred.predictedPosition}</span>
                  <h3>{pred.driverName}</h3>
                  <span className="team-name">{pred.team}</span>
                </div>
                
                <div className="metrics">
                  <div className="metric">
                    <span>Уверенность:</span>
                    <div className="progress-bar">
                      <div className="progress-fill" style={{width: `${pred.confidence * 100}%`}}>
                        {Math.round(pred.confidence * 100)}%
                      </div>
                    </div>
                  </div>
                  <div className="risk-metric">
                    Риск: <strong>{pred.riskLevel}</strong>
                  </div>
                </div>

                <div className="arguments">
                  <h4>Аргументы (Scoring):</h4>
                  {(() => {
                    try {
                      const args = JSON.parse(pred.arguments);
                      return (
                        <ul className="arguments-list">
                          <li>
                            <span className="arg-label">История сезона:</span> 
                            <span className="arg-value">{args.seasonHistory?.toFixed(1)}/100</span>
                          </li>
                          <li>
                            <span className="arg-label">История на треке:</span> 
                            <span className="arg-value">{args.trackHistory?.toFixed(1)}/100</span>
                          </li>
                          <li>
                            <span className="arg-label">Влияние новостей:</span> 
                            <span className="arg-value">
                              {args.news?.toFixed(1)}/100 {args.penalty ? '⚠️ Штраф' : ''}
                            </span>
                          </li>

                          {args.practiceScore !== undefined && (
                            <>
                              <li className="separator"></li>
                              <li>
                                <span className="arg-label">Практика (Скор):</span> 
                                <span className="arg-value">{args.practiceScore.toFixed(1)}/100</span>
                              </li>
                              <li>
                                <span className="arg-label">Позиция в практике:</span> 
                                <span className="arg-value">P{args.practicePosition}</span>
                              </li>
                            </>
                          )}

                          {args.qualifyingScore !== undefined && (
                            <>
                              <li className="separator"></li>
                              <li>
                                <span className="arg-label">Квалификация (Скор):</span> 
                                <span className="arg-value">{args.qualifyingScore.toFixed(1)}/100</span>
                              </li>
                              <li>
                                <span className="arg-label">Стартовая решетка:</span> 
                                <span className="arg-value">P{args.startingGrid || args.qualifyingPosition}</span>
                              </li>
                            </>
                          )}
                        </ul>
                      );
                    } catch (e) {
                      return <span>Данные анализируются...</span>;
                    }
                  })()}
                </div>

              </div>
            ))}
          </div>
        </div>
      ) : (
        <div>
          <h2>Результаты гонки</h2>
          <table className="results-table">
            <thead>
              <tr>
                <th>Место</th>
                <th>Пилот</th>
                <th>Команда</th>
                <th>Прогноз</th>
                <th>Ошибка</th>
                <th>Объяснение</th>
              </tr>
            </thead>
            <tbody>
              {data.map((res, index) => (
                <tr key={index} className={res.errorMargin > 2 ? 'error-row' : ''}>
                  <td>{res.actualPosition}</td>
                  <td>{res.driverName}</td>
                  <td>{res.team}</td>
                  <td>{res.predictedPosition || '—'}</td>
                  <td>{res.errorMargin === 0 ? '✅' : res.errorMargin}</td>
                  <td>{res.explanation}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default GrandPrixDetailPage;