import { useContext } from 'react';
import { ModalContext } from '../components/Modal/ModalProvider';
import { ModalContextType } from '../types/modal';

export const useModal = (): ModalContextType => {
  const context = useContext(ModalContext);
  
  if (!context) {
    throw new Error('useModal must be used within a ModalProvider');
  }
  
  return context;
};

export default useModal;
