import React, { useState } from 'react';
import { Box, Paper, TextField, Button, Typography, Alert, CircularProgress } from '@mui/material';
import { authService } from '../services/authService.ts';

const ForgotPasswordPage: React.FC = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [token, setToken] = useState<string | null>(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleRequest = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);
    try {
      const res = await authService.requestPasswordReset(usernameOrEmail);
      setMessage(res.message || 'If the account exists, an email has been sent.');
      if (res.data) setToken(res.data);
    } catch (err: any) {
      setError(err?.response?.data?.message || err.message || 'Failed to request reset.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh" px={2}>
      <Paper elevation={3} sx={{ p: 4, maxWidth: 420, width: '100%' }}>
        <Typography variant="h4" component="h1" gutterBottom align="center">
          Forgot Password
        </Typography>

        {message && (
          <Alert severity="success" sx={{ mb: 2 }}>
            {message}
          </Alert>
        )}
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box component="form" onSubmit={handleRequest}>
          <TextField
            fullWidth
            label="Username or Email"
            type="text"
            value={usernameOrEmail}
            onChange={(e) => setUsernameOrEmail(e.target.value)}
            margin="normal"
            required
            disabled={loading}
          />

          <Button type="submit" fullWidth variant="contained" sx={{ mt: 2 }} disabled={loading}>
            {loading ? <CircularProgress size={24} /> : 'Send Reset Link'}
          </Button>
        </Box>

        {token && (
          <Alert severity="info" sx={{ mt: 2 }}>
            Dev token: {token} (use for testing)
          </Alert>
        )}
      </Paper>
    </Box>
  );
};

export default ForgotPasswordPage;
