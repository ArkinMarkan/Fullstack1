import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  TextField,
  Box,
  Alert,
  CircularProgress,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import { Movie } from '../types';
import { movieService } from '../services/movieService.ts';
import { ticketService } from '../services/ticketService.ts';
import { useAuth } from '../context/AuthContext.tsx';

const MoviesPage: React.FC = () => {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [filteredMovies, setFilteredMovies] = useState<Movie[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [bookingDialog, setBookingDialog] = useState(false);
  const [selectedMovie, setSelectedMovie] = useState<Movie | null>(null);
  const [bookingData, setBookingData] = useState({
    numberOfTickets: 1,
    seatNumbers: '',
  });
  const [bookingLoading, setBookingLoading] = useState(false);
  const { user } = useAuth();

  useEffect(() => {
    fetchMovies();
  }, []);

  useEffect(() => {
    if (searchTerm.trim() === '') {
      setFilteredMovies(movies);
    } else {
      const filtered = movies.filter(movie =>
        movie.movieName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        movie.theatreName.toLowerCase().includes(searchTerm.toLowerCase())
      );
      setFilteredMovies(filtered);
    }
  }, [searchTerm, movies]);

  const fetchMovies = async () => {
    try {
      const data = await movieService.getAllMovies();
      setMovies(data);
      setFilteredMovies(data);
    } catch (err: any) {
      setError('Failed to fetch movies. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleBookTicket = (movie: Movie) => {
    setSelectedMovie(movie);
    setBookingDialog(true);
  };

  const handleBookingSubmit = async () => {
    if (!selectedMovie || !user) return;

    setBookingLoading(true);
    try {
      const seatNumbers = bookingData.seatNumbers
        .split(',')
        .map(seat => seat.trim())
        .filter(seat => seat.length > 0);

      if (seatNumbers.length !== bookingData.numberOfTickets) {
        throw new Error('Number of seat numbers must match number of tickets');
      }

      await ticketService.bookTicket(
        selectedMovie.movieName,
        {
          movieName: selectedMovie.movieName,
          theatreName: selectedMovie.theatreName,
          numberOfTickets: bookingData.numberOfTickets,
          seatNumbers,
        }
      );

      setBookingDialog(false);
      setBookingData({ numberOfTickets: 1, seatNumbers: '' });
      alert('Tickets booked successfully!');
    } catch (err: any) {
      alert(err.message || 'Booking failed. Please try again.');
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Movies
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* Search */}
      <TextField
        fullWidth
        label="Search movies or theaters"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        sx={{ mb: 4 }}
      />

      {/* Movies Grid */}
      <Grid container spacing={3}>
        {filteredMovies.length === 0 ? (
          <Grid item xs={12}>
            <Typography variant="h6" textAlign="center" color="text.secondary">
              {searchTerm ? 'No movies found for your search.' : 'No movies available.'}
            </Typography>
          </Grid>
        ) : (
          filteredMovies.map((movie) => (
            <Grid item xs={12} sm={6} md={4} key={`${movie.movieName}-${movie.theatreName}`}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  transition: 'transform 0.2s',
                  '&:hover': {
                    transform: 'translateY(-2px)',
                    boxShadow: 3,
                  },
                }}
              >
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" component="h2" gutterBottom>
                    {movie.movieName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Theatre: {movie.theatreName}
                  </Typography>
                  <Box mb={2}>
                    <Chip
                      label={`${movie.availableTickets} seats available`}
                      color={movie.availableTickets > 10 ? 'success' : 'warning'}
                      size="small"
                    />
                  </Box>
                  {movie.showTimes && movie.showTimes.length > 0 && (
                    <Box>
                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        Show Times:
                      </Typography>
                      <Box display="flex" flexWrap="wrap" gap={0.5}>
                        {movie.showTimes.map((time, index) => (
                          <Chip key={index} label={time} variant="outlined" size="small" />
                        ))}
                      </Box>
                    </Box>
                  )}
                </CardContent>
                <CardActions>
                  <Button
                    variant="contained"
                    fullWidth
                    onClick={() => handleBookTicket(movie)}
                    disabled={!user || movie.availableTickets <= 0}
                  >
                    {!user ? 'Login to Book' : movie.availableTickets <= 0 ? 'Sold Out' : 'Book Tickets'}
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))
        )}
      </Grid>

      {/* Booking Dialog */}
      <Dialog open={bookingDialog} onClose={() => setBookingDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Book Tickets for {selectedMovie?.movieName}</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Theatre: {selectedMovie?.theatreName}
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom sx={{ mb: 3 }}>
              Available Seats: {selectedMovie?.availableTickets}
            </Typography>

            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Number of Tickets</InputLabel>
              <Select
                value={bookingData.numberOfTickets}
                label="Number of Tickets"
                onChange={(e) =>
                  setBookingData(prev => ({ ...prev, numberOfTickets: Number(e.target.value) }))
                }
              >
                {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map(num => (
                  <MenuItem key={num} value={num}>
                    {num}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <TextField
              fullWidth
              label="Seat Numbers (comma-separated)"
              placeholder="A1, A2, A3"
              value={bookingData.seatNumbers}
              onChange={(e) =>
                setBookingData(prev => ({ ...prev, seatNumbers: e.target.value }))
              }
              helperText="Enter seat numbers separated by commas"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setBookingDialog(false)} disabled={bookingLoading}>
            Cancel
          </Button>
          <Button
            onClick={handleBookingSubmit}
            variant="contained"
            disabled={bookingLoading || !bookingData.seatNumbers.trim()}
          >
            {bookingLoading ? <CircularProgress size={24} /> : 'Book Tickets'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default MoviesPage;
