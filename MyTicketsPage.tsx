import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  Box,
  Alert,
  CircularProgress,
  Chip,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import { Ticket } from '../types';
import { ticketService } from '../services/ticketService.ts';
import { useAuth } from '../context/AuthContext.tsx';

const MyTicketsPage: React.FC = () => {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancelDialog, setCancelDialog] = useState(false);
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);
  const [cancelLoading, setCancelLoading] = useState(false);
  const { user } = useAuth();

  useEffect(() => {
    if (user) {
      fetchTickets();
    }
  }, [user]);

  const fetchTickets = async () => {
    try {
      const data = await ticketService.getUserTickets();
      // Normalize tickets to avoid runtime errors
      const normalized = (data ?? []).map((t) => ({
        ...t,
        seatNumbers: Array.isArray(t.seatNumbers) ? t.seatNumbers : [],
        bookingDate: t.bookingDate ?? new Date().toISOString(),
        status: t.status ?? 'CONFIRMED',
      }));
      setTickets(normalized);
    } catch (err: any) {
      setError('Failed to fetch tickets. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelTicket = (ticket: Ticket) => {
    setSelectedTicket(ticket);
    setCancelDialog(true);
  };

  const confirmCancelTicket = async () => {
    if (!selectedTicket) return;

    setCancelLoading(true);
    try {
      const reference = selectedTicket.bookingReference ?? String(selectedTicket.id);
      await ticketService.cancelTicket(reference);
      await fetchTickets();
      setCancelDialog(false);
      setSelectedTicket(null);
    } catch (err: any) {
      alert(err.message || 'Failed to cancel ticket. Please try again.');
    } finally {
      setCancelLoading(false);
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  if (!user) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="warning">
          Please log in to view your tickets.
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        My Tickets
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {tickets.length === 0 ? (
        <Box textAlign="center" py={8}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            You haven't booked any tickets yet.
          </Typography>
          <Typography variant="body1" color="text.secondary" paragraph>
            Explore our movies and book your first ticket!
          </Typography>
          <Button variant="contained" href="/movies">
            Browse Movies
          </Button>
        </Box>
      ) : (
        <Grid container spacing={3}>
          {tickets.map((ticket) => (
            <Grid item xs={12} sm={6} md={4} key={ticket.id}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                }}
              >
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                    <Typography variant="h6" component="h2">
                      {ticket.movieName}
                    </Typography>
                    <Chip
                      label={ticket.status}
                      color={
                        ticket.status === 'CONFIRMED'
                          ? 'success'
                          : ticket.status === 'CANCELLED'
                          ? 'error'
                          : 'default'
                      }
                      size="small"
                    />
                  </Box>

                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    <strong>Theater:</strong> {ticket.theatreName}
                  </Typography>

                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    <strong>Tickets:</strong> {ticket.numberOfTickets}
                  </Typography>

                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    <strong>Seats:</strong> {(ticket.seatNumbers ?? []).join(', ')}
                  </Typography>

                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    <strong>Booked On:</strong>{' '}
                    {new Date(ticket.bookingDate ?? new Date().toISOString()).toLocaleDateString('en-US', {
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </Typography>

                  {ticket.status === 'CONFIRMED' && (
                    <Box mt={2}>
                      <Button
                        variant="outlined"
                        color="error"
                        size="small"
                        onClick={() => handleCancelTicket(ticket)}
                      >
                        Cancel Ticket
                      </Button>
                    </Box>
                  )}
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Cancel Confirmation Dialog */}
      <Dialog open={cancelDialog} onClose={() => setCancelDialog(false)}>
        <DialogTitle>Cancel Ticket</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to cancel your ticket for{' '}
            <strong>{selectedTicket?.movieName}</strong> at{' '}
            <strong>{selectedTicket?.theatreName}</strong>?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialog(false)} disabled={cancelLoading}>
            Keep Ticket
          </Button>
          <Button
            onClick={confirmCancelTicket}
            color="error"
            variant="contained"
            disabled={cancelLoading}
          >
            {cancelLoading ? <CircularProgress size={24} /> : 'Cancel Ticket'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default MyTicketsPage;
