import { apiClient } from './api.ts';
import { Movie } from '../types';

const mapMovie = (m: any): Movie => ({
  id: m.id,
  movieName: m.movie_name,
  theatreName: m.theatre_name,
  totalTickets: m.total_tickets,
  availableTickets: m.available_tickets,
  showTimes: m.show_times ?? [],
  status: m.status,
  description: m.description,
  genre: m.genre,
  language: m.language,
  duration: m.duration,
  rating: m.rating,
  posterUrl: m.poster_url,
  releaseDate: m.release_date,
  createdDate: m.created_date,
  modifiedDate: m.modified_date,
});

export const movieService = {
  getAllMovies: async (): Promise<Movie[]> => {
    const response = await apiClient.get('/all');
    const list = response.data.data as any[];
    return Array.isArray(list) ? list.map(mapMovie) : [];
  },

  searchMovies: async (movieName: string): Promise<Movie[]> => {
    const response = await apiClient.get(`/movies/search/${encodeURIComponent(movieName)}`);
    const list = response.data.data as any[];
    return Array.isArray(list) ? list.map(mapMovie) : [];
  },

  addMovie: async (movieData: Omit<Movie, 'id'>): Promise<Movie> => {
    // Backend expects snake_case fields
    const payload = {
      movie_name: movieData.movieName,
      theatre_name: movieData.theatreName,
      total_tickets: movieData.totalTickets,
      available_tickets: movieData.availableTickets,
      show_times: movieData.showTimes ?? [],
      status: movieData.status,
      description: movieData.description,
      genre: movieData.genre,
      language: movieData.language,
      duration: movieData.duration,
      rating: movieData.rating,
      poster_url: movieData.posterUrl,
      release_date: movieData.releaseDate,
      created_date: movieData.createdDate,
      modified_date: movieData.modifiedDate,
    };
    const response = await apiClient.post('/admin/add', payload);
    return mapMovie(response.data.data);
  },

  updateMovie: async (
    movieName: string,
    theatreName: string,
    movieData: Partial<Movie>
  ): Promise<Movie> => {
    // Build complete payload in snake_case
    const payload = {
      movie_name: movieName,
      theatre_name: theatreName,
      total_tickets: movieData.totalTickets ?? movieData.availableTickets,
      available_tickets: movieData.availableTickets ?? movieData.totalTickets,
      show_times: movieData.showTimes ?? [],
      status: movieData.status,
      description: movieData.description,
      genre: movieData.genre,
      language: movieData.language,
      duration: movieData.duration,
      rating: movieData.rating,
      poster_url: movieData.posterUrl,
      release_date: movieData.releaseDate,
      created_date: movieData.createdDate,
      modified_date: movieData.modifiedDate,
    };

    // Current backend controller supports add. For edit, backend may require delete then re-add.
    await apiClient.delete(`/${encodeURIComponent(movieName)}/delete/${encodeURIComponent(theatreName)}`);
    const response = await apiClient.post('/admin/add', payload);
    return mapMovie(response.data.data);
  },

  updateTicketStatus: async (movieName: string, status: string): Promise<Movie> => {
    const response = await apiClient.put(`/${encodeURIComponent(movieName)}/update/${status}?theatreName=${encodeURIComponent('PVR Cinemas')}`);
    return mapMovie(response.data.data);
  },

  deleteMovie: async (movieName: string, theatreName: string): Promise<void> => {
    await apiClient.delete(`/${encodeURIComponent(movieName)}/delete/${encodeURIComponent(theatreName)}`);
  },
};
