import React, { useEffect, useState } from 'react';
import { NoteVersion, Note, EnhancedVersionComparisonDTO } from '../../services/api';
import { getVersionHistory, getSpecificVersion, restoreToVersion, compareVersionsEnhanced } from '../../services/api';
import { useModal } from '../../hooks/useModal';
import EnhancedVersionComparison from './EnhancedVersionComparison';
import './NoteVersions.css';

interface NoteVersionsProps {
  noteId: number;
  currentNote: Note;
  onVersionRestored?: (restoredNote: Note) => void;
  onClose: () => void;
  onNoteUpdated?: () => void; // Aggiungiamo callback per ricaricare la nota corrente
}

const NoteVersions: React.FC<NoteVersionsProps> = ({ 
  noteId, 
  currentNote, 
  onVersionRestored, 
  onClose,
  onNoteUpdated
}) => {
  const { showInfo, showWarningConfirm, showConfirm } = useModal();
  const [versions, setVersions] = useState<NoteVersion[]>([]);
  const [selectedVersion, setSelectedVersion] = useState<NoteVersion | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isRestoring, setIsRestoring] = useState(false);
  const [compareWithVersion, setCompareWithVersion] = useState<number | null>(null);
  const [enhancedComparison, setEnhancedComparison] = useState<EnhancedVersionComparisonDTO | null>(null);
  const [showEnhancedComparison, setShowEnhancedComparison] = useState(false);

  useEffect(() => {
    loadVersionHistory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [noteId]);

  const loadVersionHistory = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const versionHistory = await getVersionHistory(noteId);
      setVersions(versionHistory);
    } catch (err) {
      setError('Errore nel caricamento dello storico versioni');
      console.error('Error loading version history:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleVersionSelect = async (version: NoteVersion) => {
    try {
      setError(null);
      const fullVersion = await getSpecificVersion(noteId, version.versionNumber);
      setSelectedVersion(fullVersion);
    } catch (err) {
      setError('Errore nel caricamento della versione');
      console.error('Error loading version:', err);
    }
  };

  const getCurrentVersionNumber = () => {
    // With the new approach, the current version is always the highest version number
    // which is the first one in the versions list (since they're ordered by version number desc)
    if (versions.length > 0) {
      return versions[0].versionNumber;
    }
    // If no versions exist yet, the current version is 1
    return 1;
  };

  const handleRestore = async (versionNumber: number) => {
    // Verifica se stiamo tentando di ripristinare la versione corrente
    const currentVersionNumber = getCurrentVersionNumber();
    if (versionNumber === currentVersionNumber) {
      showInfo(
        'Versione Corrente', 
        `La versione ${versionNumber} è già quella corrente. Nessuna operazione necessaria.`
      );
      return;
    }

    showConfirm(
      'Switch Versione',
      `Vuoi switchare alla versione ${versionNumber}? Questo cambierà solo quale versione è attiva, senza creare nuove versioni.`,
      async () => {
        try {
          setIsRestoring(true);
          setError(null);
          
          console.log('🔄 Switching to version', versionNumber, 'for note', noteId);
      
      const restoredNote = await restoreToVersion(noteId, versionNumber);
      
      console.log('✅ Switched to version successfully:', restoredNote);
      
      // Aggiorna la nota corrente nel componente padre
      if (onVersionRestored) {
        console.log('📞 Calling onVersionRestored in parent component...');
        onVersionRestored(restoredNote);
      }
      
      // Ricarica lo storico versioni per aggiornare i dati
      console.log('🔄 Reloading version history...');
      await loadVersionHistory();
      
      // Mostra messaggio di successo
      showInfo('Successo', `Ora stai visualizzando la versione ${versionNumber}! L'elenco delle versioni rimane invariato.`);
      
      // Chiudi automaticamente la modale per mostrare immediatamente il risultato
      console.log('❌ Closing modal...');
      onClose();
      
    } catch (err) {
      setError('Errore nel cambio versione');
      console.error('❌ Error switching version:', err);
    } finally {
      setIsRestoring(false);
    }
      }
    );
  };

  const handleCompare = async (versionNumber: number) => {
    if (compareWithVersion === null) {
      setCompareWithVersion(versionNumber);
      showInfo('Selezione Confronto', `Seleziona un'altra versione per confrontare con la v${versionNumber}`);
      return;
    }

    try {
      setError(null);
      const enhancedResult = await compareVersionsEnhanced(noteId, compareWithVersion, versionNumber);
      setEnhancedComparison(enhancedResult);
      setShowEnhancedComparison(true);
      setCompareWithVersion(null);
      
      console.log('Confronto ricevuto:', enhancedResult);
      
    } catch (err) {
      setError('Errore nel confronto versioni');
      console.error('Error in enhanced comparison:', err);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('it-IT', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (isLoading) {
    return (
      <div className="note-versions-modal">
        <div className="note-versions-content">
          <div className="loading">Caricamento versioni...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="note-versions-modal" onClick={onClose}>
      <div className="note-versions-content" onClick={(e) => e.stopPropagation()}>
        <div className="note-versions-header">
          <h2>Versioni della Nota: {currentNote.title}</h2>
          <button className="close-button" onClick={onClose}>✕</button>
        </div>

        {error && (
          <div className="error-message">{error}</div>
        )}

        <div className="note-versions-body">
          <div className="versions-list">
            <h3>Storico Versioni</h3>
            
            {/* Indicatore versione attiva */}
            <div className="current-version-indicator">
              📍 <strong>Versione attualmente visualizzata: {getCurrentVersionNumber()}</strong>
            </div>
            
            {/* Messaggio di stato per il confronto */}
            {compareWithVersion !== null && (
              <div className="comparison-status">
                📋 Versione {compareWithVersion} selezionata per confronto. 
                Clicca su "Confronta" di un'altra versione per procedere.
              </div>
            )}
            
            {/* Versioni ordinate con evidenziazione di quella attiva */}
            {versions.map((version) => {
              const isCurrentlyActive = getCurrentVersionNumber() === version.versionNumber;
              return (
                <div 
                  key={version.id} 
                  className={`version-item ${isCurrentlyActive ? 'active-version' : ''} ${compareWithVersion === version.versionNumber ? 'selected-for-comparison' : ''} ${version.isRestored ? 'restored-version' : ''}`}
                >
                  <div className="version-info">
                    <span className="version-number">
                      Versione {version.versionNumber} 
                      {isCurrentlyActive && ' 🟢 (Attiva)'}
                      {version.isRestored && ` 🔄 (Ripristinata da v.${version.restoredFromVersion})`}
                    </span>
                    <span className="version-date">{formatDate(version.createdAt)}</span>
                    <span className="version-author">{version.createdByUsername || `Utente ${version.createdBy}`}</span>
                  </div>
                  <div className="version-actions">
                    <button 
                      className="view-button"
                      onClick={() => handleVersionSelect(version)}
                    >
                      👁️ Visualizza
                    </button>
                    <button 
                      className="restore-button"
                      onClick={() => handleRestore(version.versionNumber)}
                      disabled={isRestoring || isCurrentlyActive}
                    >
                      {isCurrentlyActive ? '✅ Attiva' : (isRestoring ? '⏳ Switching...' : '🔄 Switch')}
                    </button>
                    <button 
                      className="compare-button"
                      onClick={() => handleCompare(version.versionNumber)}
                      disabled={compareWithVersion === version.versionNumber}
                      title="Confronto avanzato carattere per carattere"
                    >
                      {compareWithVersion === version.versionNumber ? '✅ Selezionata' : '� Confronta'}
                    </button>
                  </div>
                </div>
              );
            })}

            {versions.length === 0 && (
              <div className="no-versions">Nessuna versione precedente disponibile</div>
            )}
          </div>

          <div className="version-preview">
            {selectedVersion ? (
              <div className="version-details">
                <h3>Versione {selectedVersion.versionNumber}</h3>
                <div className="version-metadata">
                  <p><strong>Data:</strong> {formatDate(selectedVersion.createdAt)}</p>
                  <p><strong>Autore:</strong> {selectedVersion.createdByUsername || `Utente ${selectedVersion.createdBy}`}</p>
                </div>
                <div className="version-content">
                  <h4>Titolo</h4>
                  <div className="content-display">{selectedVersion.title}</div>
                  <h4>Contenuto</h4>
                  <div className="content-display">
                    <pre>{selectedVersion.content}</pre>
                  </div>
                </div>
              </div>
            ) : (
              <div className="no-selection">
                Seleziona una versione per visualizzarne i dettagli
              </div>
            )}
          </div>
        </div>
      </div>
      
      {showEnhancedComparison && enhancedComparison && (
        <EnhancedVersionComparison 
          comparison={enhancedComparison}
          onClose={() => setShowEnhancedComparison(false)}
        />
      )}
    </div>
  );
};

export default NoteVersions;
