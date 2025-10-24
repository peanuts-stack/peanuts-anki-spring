import { apiRequest } from './client';
import { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';

export async function login(request: LoginRequest): Promise<AuthResponse> {
  return apiRequest<AuthResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export async function register(request: RegisterRequest): Promise<AuthResponse> {
  return apiRequest<AuthResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}
