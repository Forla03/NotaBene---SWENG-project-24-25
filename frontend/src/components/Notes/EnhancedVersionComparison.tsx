import React from 'react';
import { DiffSegment, EnhancedVersionComparisonDTO } from '../../services/api';
import './EnhancedVersionComparison.css';

interface Props {
  comparison: EnhancedVersionComparisonDTO;
  onClose: () => void;
}

const EnhancedVersionComparison: React.FC<Props> = ({ comparison, onClose }) => {
  
  const renderSegments = (segments: DiffSegment[]) => {
    return segments.map((segment, index) => {
      let className = 'diff-segment';
      
      switch (segment.type) {
        case 'ADDED':
          className += ' diff-added';
          break;
        case 'REMOVED':
          className += ' diff-removed';
          break;
        case 'EQUAL':
          className += ' diff-equal';
          break;
      }
      
      return (
        <span key={index} className={className}>
          {segment.text}
        </span>
      );
    });
  };

  const formatTimestamp = (timestamp: string) => {
    return new Date(timestamp).toLocaleString('it-IT', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="enhanced-comparison-overlay">
      <div className="enhanced-comparison-modal">
        <div className="enhanced-comparison-header">
          <h2>Confronto Avanzato delle Versioni</h2>
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        
        <div className="version-info-bar">
          <div className="version-info left">
            <strong>Versione {comparison.leftVersion.versionNumber}</strong>
            <span className="timestamp">{formatTimestamp(comparison.leftVersion.createdAt)}</span>
          </div>
          <div className="comparison-arrow">→</div>
          <div className="version-info right">
            <strong>Versione {comparison.rightVersion.versionNumber}</strong>
            <span className="timestamp">{formatTimestamp(comparison.rightVersion.createdAt)}</span>
          </div>
        </div>

        <div className="comparison-content">
          {/* Confronto Titolo */}
          <div className="comparison-section">
            <h3>Titolo</h3>
            <div className="diff-container">
              <div className="diff-side left-side">
                <div className="diff-header">Versione {comparison.leftVersion.versionNumber}</div>
                <div className="diff-text">
                  {comparison.titleDiff.leftSegments && comparison.titleDiff.leftSegments.length > 0 ? (
                    renderSegments(comparison.titleDiff.leftSegments)
                  ) : (
                    <span className="empty-text">{comparison.leftVersion.title || '(Titolo vuoto)'}</span>
                  )}
                </div>
              </div>
              
              <div className="diff-side right-side">
                <div className="diff-header">Versione {comparison.rightVersion.versionNumber}</div>
                <div className="diff-text">
                  {comparison.titleDiff.rightSegments && comparison.titleDiff.rightSegments.length > 0 ? (
                    renderSegments(comparison.titleDiff.rightSegments)
                  ) : (
                    <span className="empty-text">{comparison.rightVersion.title || '(Titolo vuoto)'}</span>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Confronto Contenuto */}
          <div className="comparison-section">
            <h3>Contenuto</h3>
            <div className="diff-container">
              <div className="diff-side left-side">
                <div className="diff-header">Versione {comparison.leftVersion.versionNumber}</div>
                <div className="diff-text content-text">
                  {comparison.contentDiff.leftSegments && comparison.contentDiff.leftSegments.length > 0 ? (
                    renderSegments(comparison.contentDiff.leftSegments)
                  ) : (
                    <span className="empty-text">{comparison.leftVersion.content || '(Contenuto vuoto)'}</span>
                  )}
                </div>
              </div>
              
              <div className="diff-side right-side">
                <div className="diff-header">Versione {comparison.rightVersion.versionNumber}</div>
                <div className="diff-text content-text">
                  {comparison.contentDiff.rightSegments && comparison.contentDiff.rightSegments.length > 0 ? (
                    renderSegments(comparison.contentDiff.rightSegments)
                  ) : (
                    <span className="empty-text">{comparison.rightVersion.content || '(Contenuto vuoto)'}</span>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="legend">
          <div className="legend-item">
            <span className="legend-color added"></span>
            <span>Testo aggiunto</span>
          </div>
          <div className="legend-item">
            <span className="legend-color removed"></span>
            <span>Testo rimosso</span>
          </div>
          <div className="legend-item">
            <span className="legend-color equal"></span>
            <span>Testo identico</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EnhancedVersionComparison;
