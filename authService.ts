import { apiClient } from './api.ts';
import { User, RegisterRequest, JwtResponse, ApiResponse } from '../types';

// Backend JWT payload shape (supports snake_case and camelCase)
interface BackendJwtResponse {
  token: string;
  refreshToken?: string;
  type?: string;
  loginId?: string;
  login_id?: string;
  firstName?: string;
  first_name?: string;
  lastName?: string;
  last_name?: string;
  email?: string;
  role?: 'USER' | 'ADMIN' | string;
}

export const authService = {
  login: async (loginId: string, password: string): Promise<JwtResponse> => {
    // Send snake_case keys due to backend SNAKE_CASE config
    const response = await apiClient.post<ApiResponse<BackendJwtResponse>>('/login', { login_id: loginId, password });
    const payload = response.data;

    if (!payload.success || !payload.data?.token) {
      throw new Error(payload.message || 'Authentication failed');
    }

    const jwt = payload.data;

    // Normalize role (e.g., 'ROLE_USER' -> 'USER')
    const normalizedRoleRaw = jwt.role ?? 'USER';
    const normalizedRole = (normalizedRoleRaw.replace(/^ROLE_/i, '').toUpperCase() as 'USER' | 'ADMIN');

    const user: User = {
      loginId: jwt.loginId ?? jwt.login_id ?? loginId,
      firstName: jwt.firstName ?? jwt.first_name ?? '',
      lastName: jwt.lastName ?? jwt.last_name ?? '',
      email: jwt.email ?? '',
      role: normalizedRole,
    } as User;

    localStorage.setItem('authToken', jwt.token);
    localStorage.setItem('user', JSON.stringify(user));

    return { token: jwt.token, user, refreshToken: jwt.refreshToken, type: jwt.type };
  },

  register: async (userData: RegisterRequest): Promise<{ message: string }> => {
    // Transform to snake_case for backend
    const payload = {
      first_name: userData.firstName,
      last_name: userData.lastName,
      email: userData.email,
      login_id: userData.loginId,
      password: userData.password,
      contact_number: userData.contactNumber,
      confirm_password: userData.confirmPassword,
    };
    const response = await apiClient.post('/register', payload);
    return response.data;
  },

  // Legacy GET endpoint support
  forgotPassword: async (username: string): Promise<ApiResponse<string>> => {
    const response = await apiClient.get<ApiResponse<string>>(`/${username}/forgot`);
    return response.data;
  },

  // New preferred endpoints
  requestPasswordReset: async (usernameOrEmail: string): Promise<ApiResponse<string>> => {
    const response = await apiClient.post<ApiResponse<string>>('/forgot-password', {
      username_or_email: usernameOrEmail,
    });
    return response.data;
  },

  resetPasswordWithToken: async (token: string, newPassword: string, confirmPassword: string): Promise<ApiResponse<string>> => {
    const response = await apiClient.post<ApiResponse<string>>('/reset-password', {
      token,
      new_password: newPassword,
      confirm_password: confirmPassword,
    });
    return response.data;
  },

  logout: (): void => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
  },

  getCurrentUser: (): User | null => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated: (): boolean => {
    const token = localStorage.getItem('authToken');
    return !!token;
  },
};
