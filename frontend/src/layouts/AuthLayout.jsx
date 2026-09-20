import { Outlet } from 'react-router-dom';

export default function AuthLayout() {
  const layoutStyles = `
    .auth-layout-container {
      display: flex;
      min-height: 100vh;
      flex-direction: column;
    }
    @media (min-width: 768px) {
      .auth-layout-container {
        flex-direction: row;
      }
    }
    .auth-image-placeholder {
      flex: 0 0 55%;
      background-color: #111827; /* Dark hero background */
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 250px;
    }
    .auth-form-container {
      flex: 0 0 45%;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem;
      background-color: #ffffff;
    }
    .auth-form-wrapper {
      width: 100%;
      max-width: 450px;
    }
    .auth-form-wrapper .page-card {
      margin: 0;
      width: 100%;
      border: none;
      box-shadow: none;
      background: transparent;
    }
  `;

  return (
    <>
      <style dangerouslySetInnerHTML={{ __html: layoutStyles }} />
      <div className="auth-layout-container">
        
        <div className="auth-image-placeholder">
          {/* Replace this with the actual TheBigBazaar image later */}
          <div style={{ textAlign: 'center', opacity: 0.7 }}>
            <svg width="64" height="64" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" style={{ margin: '0 auto', display: 'block' }}>
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <h2 style={{ marginTop: '1rem', fontWeight: 300, letterSpacing: '2px', fontSize: '1.5rem' }}>THE BIG BAZAAR</h2>
          </div>
        </div>

        <div className="auth-form-container">
          <div className="auth-form-wrapper">
            <Outlet />
          </div>
        </div>

      </div>
    </>
  );
}
