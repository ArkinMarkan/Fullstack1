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
    const response = await apiClient.post('/admin/add', movieData);
    return mapMovie(response.data.data);
  },

  updateMovie: async (
    movieName: string,
    theatreName: string,
    movieData: Partial<Movie>
  ): Promise<Movie> => {
    const payload: Omit<Movie, 'id'> = {
      movieName,
      theatreName,
      totalTickets: (movieData.totalTickets ?? movieData.availableTickets) as number,
      availableTickets: (movieData.availableTickets ?? movieData.totalTickets) as number,
      showTimes: movieData.showTimes ?? [],
      status: movieData.status,
      description: movieData.description,
      genre: movieData.genre,
      language: movieData.language,
      duration: movieData.duration,
      rating: movieData.rating,
      posterUrl: movieData.posterUrl,
      releaseDate: movieData.releaseDate,
      createdDate: movieData.createdDate,
      modifiedDate: movieData.modifiedDate,
    } as unknown as Omit<Movie, 'id'>;

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
