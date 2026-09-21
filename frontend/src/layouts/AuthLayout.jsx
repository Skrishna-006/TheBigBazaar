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
    .auth-image-panel {
      flex: 0 0 55%;
      background-color: #111827;
      background-image: url("/images/banner/04afb6c3-82e0-4f5e-af95-8c5fb5107f74.png");
      background-size: cover;
      background-position: center;
      background-repeat: no-repeat;
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
        
        <div className="auth-image-panel" aria-label="The Big Bazaar banner" role="img"></div>

        <div className="auth-form-container">
          <div className="auth-form-wrapper">
            <Outlet />
          </div>
        </div>

      </div>
    </>
  );
}
