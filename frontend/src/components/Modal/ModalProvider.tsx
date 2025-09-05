import React, { useState } from 'react';
import Alert from './Alert';
import Confirm from './Confirm';
import { AlertState, ConfirmState, ModalContextType } from '../../types/modal';

interface ModalProviderProps {
  children: React.ReactNode;
}

// Context per fornire le funzioni modal a tutta l'app
export const ModalContext = React.createContext<ModalContextType | null>(null);

export const ModalProvider: React.FC<ModalProviderProps> = ({ children }) => {
  const [alertState, setAlertState] = useState<AlertState>({
    isOpen: false,
    title: '',
    message: '',
    type: 'info'
  });

  const [confirmState, setConfirmState] = useState<ConfirmState>({
    isOpen: false,
    title: '',
    message: '',
    type: 'confirm'
  });

  // Alert functions
  const showAlert = (
    title: string, 
    message: string, 
    type: 'info' | 'warning' | 'error' | 'success' = 'info',
    buttonText?: string
  ) => {
    setAlertState({
      isOpen: true,
      title,
      message,
      type,
      buttonText
    });
  };

  const showInfo = (title: string, message: string, buttonText?: string) => {
    showAlert(title, message, 'info', buttonText);
  };

  const showSuccess = (title: string, message: string, buttonText?: string) => {
    showAlert(title, message, 'success', buttonText);
  };

  const showWarning = (title: string, message: string, buttonText?: string) => {
    showAlert(title, message, 'warning', buttonText);
  };

  const showError = (title: string, message: string, buttonText?: string) => {
    showAlert(title, message, 'error', buttonText);
  };

  const closeAlert = () => {
    setAlertState(prev => ({ ...prev, isOpen: false }));
  };

  // Confirm functions
  const showConfirm = (
    title: string,
    message: string,
    onConfirm: () => void,
    type: 'warning' | 'error' | 'confirm' = 'confirm',
    confirmText?: string,
    cancelText?: string
  ) => {
    setConfirmState({
      isOpen: true,
      title,
      message,
      type,
      confirmText,
      cancelText,
      onConfirm
    });
  };

  const showConfirmDialog = (title: string, message: string, onConfirm: () => void) => {
    showConfirm(title, message, onConfirm, 'confirm');
  };

  const showWarningConfirm = (title: string, message: string, onConfirm: () => void) => {
    showConfirm(title, message, onConfirm, 'warning', 'Procedi', 'Annulla');
  };

  const showDeleteConfirm = (title: string, message: string, onConfirm: () => void) => {
    showConfirm(title, message, onConfirm, 'error', 'Elimina', 'Annulla');
  };

  const closeConfirm = () => {
    setConfirmState(prev => ({ ...prev, isOpen: false }));
  };

  const handleConfirm = () => {
    if (confirmState.onConfirm) {
      confirmState.onConfirm();
    }
    closeConfirm();
  };

  const modalContextValue: ModalContextType = {
    // Alert
    alertState,
    showAlert,
    showInfo,
    showSuccess,
    showWarning,
    showError,
    closeAlert,
    
    // Confirm
    confirmState,
    showConfirm,
    showConfirmDialog,
    showWarningConfirm,
    showDeleteConfirm,
    closeConfirm,
    handleConfirm
  };

  return (
    <ModalContext.Provider value={modalContextValue}>
      {children}
      
      {/* Alert Modal */}
      <Alert
        isOpen={alertState.isOpen}
        onClose={closeAlert}
        title={alertState.title}
        message={alertState.message}
        type={alertState.type}
        buttonText={alertState.buttonText}
      />

      {/* Confirm Modal */}
      <Confirm
        isOpen={confirmState.isOpen}
        onConfirm={handleConfirm}
        onCancel={closeConfirm}
        title={confirmState.title}
        message={confirmState.message}
        type={confirmState.type}
        confirmText={confirmState.confirmText}
        cancelText={confirmState.cancelText}
      />
    </ModalContext.Provider>
  );
};

// Hook per usare il modal context
export const useModalContext = () => {
  const context = React.useContext(ModalContext);
  if (!context) {
    throw new Error('useModalContext must be used within a ModalProvider');
  }
  return context;
};

export default ModalProvider;
