import React from 'react';
import Modal from './Modal';

interface AlertProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  message: string;
  type?: 'info' | 'warning' | 'error' | 'success';
  buttonText?: string;
}

const Alert: React.FC<AlertProps> = ({ 
  isOpen, 
  onClose, 
  title, 
  message, 
  type = 'info',
  buttonText = 'OK'
}) => {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title} type={type} showCloseButton={false}>
      <div className="alert-content">
        <p className="alert-message">{message}</p>
        <div className="alert-actions">
          <button className={`alert-button alert-button-${type}`} onClick={onClose}>
            {buttonText}
          </button>
        </div>
      </div>
    </Modal>
  );
};

export default Alert;
