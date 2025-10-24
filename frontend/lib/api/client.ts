const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export async function apiRequest<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
  const userId = typeof window !== 'undefined' ? localStorage.getItem('userId') : null;

  const response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(userId && { 'X-User-Id': userId }),
      ...options?.headers,
    },
  });

  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || `API error: ${response.statusText}`);
  }

  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return undefined as any;
  }

  return response.json();
}
