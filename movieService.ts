import { apiClient } from './api.ts';
import { Movie } from '../types';

const mapMovie = (m: any): Movie => ({
  id: m.id,
  movieName: m.movie_name,
  theatreName: m.theatre_name,
  totalTickets: m.total_tickets,
  availableTickets: m.available_tickets,
  // If backend returns objects with time/date, map to human-readable strings, else pass-through
  showTimes: Array.isArray(m.show_times)
    ? m.show_times.map((st: any) => {
        const time = st.show_time ?? st.time ?? st;
        const date = st.show_date ?? st.date;
        return date ? `${date} ${time}` : time;
      })
    : [],
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

  addMovie: async (movieData: Omit<Movie, 'id'> & { showTimesDetailed?: { time: string; date: string }[] }): Promise<Movie> => {
    // Strict: require explicit showTimesDetailed or both showTimes & a show date provided by caller
    const showTimesPayload = (movieData.showTimesDetailed && movieData.showTimesDetailed.length > 0)
      ? movieData.showTimesDetailed.map(st => ({ show_time: st.time, show_date: st.date }))
      : (movieData.showTimes ?? []).map((t) => ({ show_time: t, show_date: (movieData as any).showDate }));

    // Ensure no null show_date goes out
    if (showTimesPayload.some(st => !st.show_date)) {
      throw new Error('Show date is required for each show time.');
    }

    const payload = {
      movie_name: movieData.movieName,
      theatre_name: movieData.theatreName,
      total_tickets: movieData.totalTickets,
      available_tickets: movieData.availableTickets,
      show_times: showTimesPayload,
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
    movieData: Partial<Movie> & { showTimesDetailed?: { time: string; date: string }[] }
  ): Promise<Movie> => {
    const showTimesPayload = (movieData.showTimesDetailed && movieData.showTimesDetailed.length > 0)
      ? movieData.showTimesDetailed.map(st => ({ show_time: st.time, show_date: st.date }))
      : (movieData.showTimes ?? []).map((t) => ({ show_time: t, show_date: (movieData as any).showDate }));

    if (showTimesPayload.some(st => !st.show_date)) {
      throw new Error('Show date is required for each show time.');
    }

    const payload = {
      movie_name: movieName,
      theatre_name: theatreName,
      total_tickets: movieData.totalTickets ?? movieData.availableTickets,
      available_tickets: movieData.availableTickets ?? movieData.totalTickets,
      show_times: showTimesPayload,
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
