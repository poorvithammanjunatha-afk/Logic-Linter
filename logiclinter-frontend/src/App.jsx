import { useState } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [code, setCode] = useState('');
  const [review, setReview] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleReview = async () => {
    if (!code.trim()) return;
    setLoading(true);
    setError('');
    setReview('');

    try {
      const response = await axios.post('https://logiclinter-backend-api.onrender.com/api/analyze', { 
  language: 'c', // or whatever language field your backend expects
  code: code 
});
      
      const data = response.data;
      let reviewText = '';
      
      if (typeof data === 'string') {
        reviewText = data;
      } else if (data && typeof data === 'object') {
        reviewText = data.review || data.message || data.result || JSON.stringify(data, null, 2);
      }
      
      setReview(reviewText);
    } catch (err) {
      console.error(err);
      setError('Failed to connect to the backend server. Make sure the Render backend is running.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ padding: '2rem', maxWidth: '900px', margin: '0 auto', fontFamily: 'sans-serif' }}>
      <h1>LogicLinter 🤖</h1>
      <p>AI-Powered Code Reviewer</p>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '1.5rem' }}>
        <textarea
          rows="10"
          value={code}
          onChange={(e) => setCode(e.target.value)}
          placeholder="Paste your code snippet here for review..."
          style={{ padding: '1rem', fontFamily: 'monospace', fontSize: '14px', borderRadius: '6px', border: '1px solid #ccc' }}
        />
        
        <button 
          onClick={handleReview} 
          disabled={loading}
          style={{ padding: '0.75rem 1.5rem', backgroundColor: '#2563eb', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' }}
        >
          {loading ? 'Analyzing Code...' : 'Review Code'}
        </button>
      </div>

      {error && <p style={{ color: 'red', marginTop: '1rem' }}>{error}</p>}

      {review && (
        <div style={{ marginTop: '2rem', padding: '1.5rem', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '6px' }}>
          <h3>Review Results:</h3>
          <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word', fontFamily: 'monospace', marginTop: '1rem' }}>
            {review}
          </pre>
        </div>
      )}
    </div>
  );
}

export default App;