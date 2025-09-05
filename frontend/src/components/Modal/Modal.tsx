import React from 'react';
import './Modal.css';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  type?: 'info' | 'warning' | 'error' | 'success' | 'confirm';
  showCloseButton?: boolean;
}

const Modal: React.FC<ModalProps> = ({ 
  isOpen, 
  onClose, 
  title, 
  children, 
  type = 'info',
  showCloseButton = true 
}) => {
  if (!isOpen) return null;

  const getIcon = () => {
    switch (type) {
      case 'success': return '✅';
      case 'warning': return '⚠️';
      case 'error': return '❌';
      case 'confirm': return '❓';
      default: return 'ℹ️';
    }
  };

  const getTypeClass = () => {
    return `modal-${type}`;
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className={`modal-content ${getTypeClass()}`} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title">
            <span className="modal-icon">{getIcon()}</span>
            <h3>{title}</h3>
          </div>
          {showCloseButton && (
            <button className="modal-close-button" onClick={onClose}>
              ×
            </button>
          )}
        </div>
        <div className="modal-body">
          {children}
        </div>
      </div>
    </div>
  );
};

export default Modal;
