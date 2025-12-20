import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Box,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Tabs,
  Tab,
} from '@mui/material';
import { Movie, Ticket } from '../types';
import { movieService } from '../services/movieService.ts';
import { ticketService } from '../services/ticketService.ts';
import { useAuth } from '../context/AuthContext.tsx';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => (
  <div hidden={value !== index}>{value === index && <Box sx={{ p: 3 }}>{children}</Box>}</div>
);

const AdminDashboard: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [movies, setMovies] = useState<Movie[]>([]);
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [movieDialog, setMovieDialog] = useState(false);
  const [selectedMovie, setSelectedMovie] = useState<Movie | null>(null);
  const [movieFormData, setMovieFormData] = useState({
    movieName: '',
    theatreName: '',
    availableTickets: 100,
    showTimes: '',
  });
  const [formLoading, setFormLoading] = useState(false);
  const { user } = useAuth();

  useEffect(() => {
    if (user?.role === 'ADMIN') {
      fetchData();
    }
  }, [user]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [moviesData, ticketsData] = await Promise.all([
        movieService.getAllMovies(),
        ticketService.getAllTickets(), // Admin endpoint
      ]);
      setMovies(moviesData);
      setTickets(ticketsData);
    } catch (err: any) {
      setError('Failed to fetch data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddMovie = () => {
    setSelectedMovie(null);
    setMovieFormData({
      movieName: '',
      theatreName: '',
      availableTickets: 100,
      showTimes: '',
    });
    setMovieDialog(true);
  };

  const handleEditMovie = (movie: Movie) => {
    setSelectedMovie(movie);
    setMovieFormData({
      movieName: movie.movieName,
      theatreName: movie.theatreName,
      availableTickets: movie.availableTickets,
      showTimes: movie.showTimes?.join(', ') || '',
    });
    setMovieDialog(true);
  };

  const handleSubmitMovie = async () => {
    setFormLoading(true);
    try {
      const parsedShowTimes = movieFormData.showTimes
        .split(',')
        .map((time) => time.trim())
        .filter((time) => time.length > 0);

      if (selectedMovie) {
        // For edit: keep existing totalTickets, update availableTickets/showTimes
        const payload = {
          movieName: selectedMovie.movieName,
          theatreName: selectedMovie.theatreName,
          totalTickets: selectedMovie.totalTickets ?? movieFormData.availableTickets,
          availableTickets: movieFormData.availableTickets,
          showTimes: parsedShowTimes,
        } as Partial<Movie>;

        await movieService.updateMovie(
          selectedMovie.movieName,
          selectedMovie.theatreName,
          payload
        );
      } else {
        // For add: set totalTickets = availableTickets initially
        const payload = {
          movieName: movieFormData.movieName,
          theatreName: movieFormData.theatreName,
          totalTickets: movieFormData.availableTickets,
          availableTickets: movieFormData.availableTickets,
          showTimes: parsedShowTimes,
        } as Omit<Movie, 'id'>;

        await movieService.addMovie(payload);
      }

      setMovieDialog(false);
      await fetchData();
    } catch (err: any) {
      alert(err.message || 'Failed to save movie. Please try again.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeleteMovie = async (movieName: string, theatreName: string) => {
    if (window.confirm(`Are you sure you want to delete "${movieName}" at "${theatreName}"?`)) {
      try {
        await movieService.deleteMovie(movieName, theatreName);
        await fetchData();
      } catch (err: any) {
        alert(err.message || 'Failed to delete movie. Please try again.');
      }
    }
  };

  if (user?.role !== 'ADMIN') {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Box
          sx={{
            backgroundColor: 'error.light',
            color: 'error.contrastText',
            p: 2,
            borderRadius: 1,
            border: '1px solid',
            borderColor: 'error.main'
          }}
        >
          Access denied. Admin privileges required.
        </Box>
      </Container>
    );
  }

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
        Admin Dashboard
      </Typography>

      {error && (
        <Box
          sx={{
            backgroundColor: 'error.light',
            color: 'error.contrastText',
            p: 2,
            borderRadius: 1,
            border: '1px solid',
            borderColor: 'error.main',
            mb: 2
          }}
        >
          {error}
        </Box>
      )}

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={(_, newValue) => setTabValue(newValue)}>
          <Tab label="Movies Management" />
          <Tab label="Bookings Overview" />
        </Tabs>
      </Box>

      {/* Movies Management Tab */}
      <TabPanel value={tabValue} index={0}>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Typography variant="h6">Manage Movies</Typography>
          <Button variant="contained" onClick={handleAddMovie}>
            Add New Movie
          </Button>
        </Box>

        <Grid container spacing={3}>
          {movies.map((movie) => (
            <Grid item xs={12} sm={6} md={4} key={`${movie.movieName}-${movie.theatreName}`}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    {movie.movieName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Theater: {movie.theatreName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Available Seats: {movie.availableTickets}
                  </Typography>
                  {movie.showTimes && movie.showTimes.length > 0 && (
                    <Box mt={1}>
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
                  <Button size="small" onClick={() => handleEditMovie(movie)}>
                    Edit
                  </Button>
                  <Button
                    size="small"
                    color="error"
                    onClick={() => handleDeleteMovie(movie.movieName, movie.theatreName)}
                  >
                    Delete
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      </TabPanel>

      {/* Bookings Overview Tab */}
      <TabPanel value={tabValue} index={1}>
        <Typography variant="h6" gutterBottom>
          All Bookings
        </Typography>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>User Email</TableCell>
                <TableCell>Movie</TableCell>
                <TableCell>Theater</TableCell>
                <TableCell>Tickets</TableCell>
                <TableCell>Seats</TableCell>
                <TableCell>Booking Date</TableCell>
                <TableCell>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {tickets.map((ticket) => (
                <TableRow key={ticket.id}>
                  <TableCell>{ticket.userEmail}</TableCell>
                  <TableCell>{ticket.movieName}</TableCell>
                  <TableCell>{ticket.theatreName}</TableCell>
                  <TableCell>{ticket.numberOfTickets}</TableCell>
                  <TableCell>{ticket.seatNumbers.join(', ')}</TableCell>
                  <TableCell>
                    {new Date(ticket.bookingDate).toLocaleDateString()}
                  </TableCell>
                  <TableCell>
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
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </TabPanel>

      {/* Add/Edit Movie Dialog */}
      <Dialog open={movieDialog} onClose={() => setMovieDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {selectedMovie ? 'Edit Movie' : 'Add New Movie'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2 }}>
            <TextField
              fullWidth
              label="Movie Name"
              value={movieFormData.movieName}
              onChange={(e) =>
                setMovieFormData(prev => ({ ...prev, movieName: e.target.value }))
              }
              sx={{ mb: 2 }}
              disabled={!!selectedMovie} // Can't change movie name when editing
            />

            <TextField
              fullWidth
              label="Theater Name"
              value={movieFormData.theatreName}
              onChange={(e) =>
                setMovieFormData(prev => ({ ...prev, theatreName: e.target.value }))
              }
              sx={{ mb: 2 }}
              disabled={!!selectedMovie} // Can't change theater name when editing
            />

            <TextField
              fullWidth
              label="Available Seats"
              type="number"
              value={movieFormData.availableTickets}
              onChange={(e) =>
                setMovieFormData((prev) => ({ ...prev, availableTickets: Number(e.target.value) }))
              }
              sx={{ mb: 2 }}
              inputProps={{ min: 1 }}
            />

            <TextField
              fullWidth
              label="Show Times (comma-separated)"
              placeholder="10:00 AM, 2:00 PM, 6:00 PM"
              value={movieFormData.showTimes}
              onChange={(e) =>
                setMovieFormData(prev => ({ ...prev, showTimes: e.target.value }))
              }
              helperText="Enter show times separated by commas"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setMovieDialog(false)} disabled={formLoading}>
            Cancel
          </Button>
          <Button
            onClick={handleSubmitMovie}
            variant="contained"
            disabled={formLoading || !movieFormData.movieName || !movieFormData.theatreName}
          >
            {formLoading ? <CircularProgress size={24} /> : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default AdminDashboard;
