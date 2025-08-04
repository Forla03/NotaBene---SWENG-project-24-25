import React from 'react';

type HomePageProps = {
  goToNotes: () => void;
};

const HomePage: React.FC<HomePageProps> = ({ goToNotes }) => {
  return (
    <div className="homepage-container">
      <h1>Benvenuto su NotaBene</h1>
      <p>App per la gestione delle tue note.</p>
      <button onClick={goToNotes}>Crea Note</button>
    </div>
  );
};

export default HomePage;