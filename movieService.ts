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
        const time = st.show_time ?? st.showTime ?? st.time ?? st;
        const date = st.show_date ?? st.showDate ?? st.date;
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

// Normalize showTimes payload to backend format and validate required fields
const buildShowTimesPayload = (
  movieData:
    | (Omit<Movie, 'id'> & {
        showTimesDetailed?: { time?: string; showTime?: string; date?: string; showDate?: string; show_date?: string; screenNumber?: number; screen_number?: number }[];
        showDate?: string;
      })
    | (Partial<Movie> & {
        showTimesDetailed?: { time?: string; showTime?: string; date?: string; showDate?: string; show_date?: string; screenNumber?: number; screen_number?: number }[];
        showDate?: string;
      })
): Array<{ show_time: string; show_date: string; screen_number?: number }> => {
  const detailed = (movieData as any).showTimesDetailed as
    | { time?: string; showTime?: string; date?: string; showDate?: string; show_date?: string; screenNumber?: number; screen_number?: number }[]
    | undefined;

  const fallbackShowDate = (movieData as any).showDate;

  let showTimesPayload: Array<{ show_time: string; show_date: string; screen_number?: number }> = [];

  if (Array.isArray(detailed) && detailed.length > 0) {
    showTimesPayload = detailed.map((st) => {
      const show_time = (st as any).show_time ?? st.showTime ?? st.time;
      const show_date = (st as any).show_date ?? st.showDate ?? st.date ?? fallbackShowDate;
      const screen_number = (st as any).screen_number ?? st.screenNumber;
      return { show_time: String(show_time ?? ''), show_date: String(show_date ?? ''), ...(screen_number != null ? { screen_number } : {}) };
    });
  } else {
    const times = (movieData as any).showTimes as string[] | undefined;
    showTimesPayload = (times ?? []).map((t) => ({
      show_time: String(t ?? ''),
      show_date: String(fallbackShowDate ?? ''),
    }));
  }

  // Trim and validate
  showTimesPayload = showTimesPayload.map((st) => ({
    show_time: st.show_time?.toString().trim(),
    show_date: st.show_date?.toString().trim(),
    ...(st.screen_number != null ? { screen_number: st.screen_number } : {}),
  }));

  if (showTimesPayload.length === 0) {
    throw new Error('At least one show time is required. Provide showTimesDetailed or showTimes + showDate.');
  }
  if (showTimesPayload.some((st) => !st.show_date)) {
    throw new Error('Show date is required for each show time. Ensure date/showDate/show_date or top-level showDate is provided.');
  }
  if (showTimesPayload.some((st) => !st.show_time)) {
    throw new Error('Show time is required for each show time entry.');
  }

  return showTimesPayload;
};

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

  addMovie: async (
    movieData: Omit<Movie, 'id'> & {
      showTimesDetailed?: { time?: string; showTime?: string; date?: string; showDate?: string; show_date?: string; screenNumber?: number; screen_number?: number }[];
      showDate?: string;
    }
  ): Promise<Movie> => {
    const show_times = buildShowTimesPayload(movieData);

    const payload = {
      movie_name: movieData.movieName,
      theatre_name: movieData.theatreName,
      total_tickets: movieData.totalTickets,
      available_tickets: movieData.availableTickets,
      show_times, // validated and normalized
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
    movieData: Partial<Movie> & {
      showTimesDetailed?: { time?: string; showTime?: string; date?: string; showDate?: string; show_date?: string; screenNumber?: number; screen_number?: number }[];
      showDate?: string;
    }
  ): Promise<Movie> => {
    const show_times = buildShowTimesPayload(movieData);

    const payload = {
      movie_name: movieName,
      theatre_name: theatreName,
      total_tickets: movieData.totalTickets ?? movieData.availableTickets,
      available_tickets: movieData.availableTickets ?? movieData.totalTickets,
      show_times, // validated and normalized
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
