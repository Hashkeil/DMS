// Mirrors the DTOs of backend

export interface DocumentResponse {
  id: number;
  title: string;
  filename: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
  categoryId: number | null;
  categoryName: string | null;
}

export interface DocumentRequest {
  title: string;
  filename: string;
  description: string | null;
  categoryId: number | null;
}

export interface CategoryResponse {
  id: number;
  name: string;
  description: string | null;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
