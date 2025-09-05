import React from 'react';
import Modal from './Modal';

interface ConfirmProps {
  isOpen: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'warning' | 'error' | 'confirm';
}

const Confirm: React.FC<ConfirmProps> = ({ 
  isOpen, 
  onConfirm, 
  onCancel, 
  title, 
  message, 
  confirmText = 'Conferma',
  cancelText = 'Annulla',
  type = 'confirm'
}) => {
  return (
    <Modal isOpen={isOpen} onClose={onCancel} title={title} type={type} showCloseButton={false}>
      <div className="confirm-content">
        <p className="confirm-message">{message}</p>
        <div className="confirm-actions">
          <button className="confirm-button confirm-button-cancel" onClick={onCancel}>
            {cancelText}
          </button>
          <button className={`confirm-button confirm-button-${type}`} onClick={onConfirm}>
            {confirmText}
          </button>
        </div>
      </div>
    </Modal>
  );
};

export default Confirm;
