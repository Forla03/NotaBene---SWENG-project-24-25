export interface AlertState {
  isOpen: boolean;
  title: string;
  message: string;
  type: 'info' | 'warning' | 'error' | 'success';
  buttonText?: string;
}

export interface ConfirmState {
  isOpen: boolean;
  title: string;
  message: string;
  type: 'warning' | 'error' | 'confirm';
  confirmText?: string;
  cancelText?: string;
  onConfirm?: () => void;
}

export interface ModalContextType {
  // Alert state and methods
  alertState: AlertState;
  showAlert: (
    title: string,
    message: string,
    type?: 'info' | 'warning' | 'error' | 'success',
    buttonText?: string
  ) => void;
  showInfo: (title: string, message: string, buttonText?: string) => void;
  showSuccess: (title: string, message: string, buttonText?: string) => void;
  showWarning: (title: string, message: string, buttonText?: string) => void;
  showError: (title: string, message: string, buttonText?: string) => void;
  closeAlert: () => void;

  // Confirm state and methods
  confirmState: ConfirmState;
  showConfirm: (
    title: string,
    message: string,
    onConfirm: () => void,
    type?: 'warning' | 'error' | 'confirm',
    confirmText?: string,
    cancelText?: string
  ) => void;
  showConfirmDialog: (title: string, message: string, onConfirm: () => void) => void;
  showWarningConfirm: (title: string, message: string, onConfirm: () => void) => void;
  showDeleteConfirm: (title: string, message: string, onConfirm: () => void) => void;
  closeConfirm: () => void;
  handleConfirm: () => void;
}
