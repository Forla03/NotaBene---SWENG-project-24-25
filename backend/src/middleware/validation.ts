import { Request, Response, NextFunction } from 'express';
import { body, validationResult } from 'express-validator';

export const validateNoteInput = [
  body('title')
    .trim()
    .notEmpty()
    .withMessage('Title is required')
    .isLength({ max: 100 })
    .withMessage('Title cannot exceed 100 characters'),
  
  body('content')
    .trim()
    .notEmpty()
    .withMessage('Content is required')
    .isLength({ max: 280 })
    .withMessage('Content cannot exceed 280 characters'),
  
  body('author')
    .trim()
    .notEmpty()
    .withMessage('Author is required'),

  (req: Request, res: Response, next: NextFunction) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      const errorMessages = errors.array().map(error => error.msg);
      return res.status(400).json({
        error: errorMessages.join(', ')
      });
    }
    next();
  }
];

export const validateNoteUpdate = [
  body('title')
    .optional()
    .trim()
    .isLength({ max: 100 })
    .withMessage('Title cannot exceed 100 characters'),
  
  body('content')
    .optional()
    .trim()
    .isLength({ max: 280 })
    .withMessage('Content cannot exceed 280 characters'),

  (req: Request, res: Response, next: NextFunction) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      const errorMessages = errors.array().map(error => error.msg);
      return res.status(400).json({
        error: errorMessages.join(', ')
      });
    }
    next();
  }
];