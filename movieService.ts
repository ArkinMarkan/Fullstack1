import { apiClient } from './api.ts';
import { Movie } from '../types';

// Normalize a time string to MySQL TIME format HH:mm:ss
const toMysqlTime = (input?: string): string | undefined => {
  if (!input) return undefined;
  const s = input.trim();
  // If already looks like HH:mm[:ss]
  const m = s.match(/^([01]?\d|2[0-3]):([0-5]\d)(?::([0-5]\d))?$/);
  if (m) {
    const hh = m[1].padStart(2, '0');
    const mm = m[2].padStart(2, '0');
    const ss = (m[3] ?? '00').padStart(2, '0');
    return `${hh}:${mm}:${ss}`;
  }
  // Handle 12-hour formats like "10:00 AM" / "9 PM" / "09:30 pm"
  const ampm = s.match(/^\s*(\d{1,2})(?::(\d{1,2}))?(?::(\d{1,2}))?\s*([AaPp][Mm])\s*$/);
  if (ampm) {
    let hour = parseInt(ampm[1], 10);
    const minute = parseInt(ampm[2] ?? '0', 10);
    const second = parseInt(ampm[3] ?? '0', 10);
    const isPM = ampm[4].toLowerCase() === 'pm';
    if (hour === 12) hour = isPM ? 12 : 0; // 12AM -> 00, 12PM -> 12
    else if (isPM) hour += 12;
    const hh = String(hour).padStart(2, '0');
    const mm = String(minute).padStart(2, '0');
    const ss = String(second).padStart(2, '0');
    return `${hh}:${mm}:${ss}`;
  }
  // Fallback: try Date parsing and extract time
  const date = new Date(`1970-01-01T${s}`);
  if (!isNaN(date.getTime())) {
    const hh = String(date.getHours()).padStart(2, '0');
    const mm = String(date.getMinutes()).padStart(2, '0');
    const ss = String(date.getSeconds()).padStart(2, '0');
    return `${hh}:${mm}:${ss}`;
  }
  // If cannot parse, return undefined
  return undefined;
};

// Normalize a date string to YYYY-MM-DD
const toMysqlDate = (input?: string): string | undefined => {
  if (!input) return undefined;
  const s = input.trim();
  // Already YYYY-MM-DD
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s;
  const d = new Date(s);
  if (isNaN(d.getTime())) return undefined;
  const yyyy = String(d.getFullYear());
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
};

const mapMovie = (m: any): Movie => ({
  id: m.id,
  movieName: m.movie_name ?? m.movieName,
  theatreName: m.theatre_name ?? m.theatreName,
  totalTickets: m.total_tickets ?? m.totalTickets,
  availableTickets: m.available_tickets ?? m.availableTickets,
  // If backend returns objects with time/date, map to human-readable strings, else pass-through
  showTimes: Array.isArray(m.showTimes ?? m.show_times)
    ? (m.showTimes ?? m.show_times).map((st: any) => {
        const time = st.time ?? st.show_time ?? st.showTime ?? st;
        const date = st.date ?? st.show_date ?? st.showDate ?? st.date;
        return date ? `${date} ${time}` : time;
      })
    : [],
  status: m.status,
  description: m.description,
  genre: m.genre,
  language: m.language,
  duration: m.duration,
  rating: m.rating,
  posterUrl: m.poster_url ?? m.posterUrl,
  releaseDate: m.release_date ?? m.releaseDate,
  createdDate: m.created_date ?? m.createdDate,
  modifiedDate: m.modified_date ?? m.modifiedDate,
});

// Normalize showTimes payload to backend format and validate required fields
const buildShowTimesPayload = (
  movieData:
    | (Omit<Movie, 'id'> & {
        showTimesDetailed?: { time?: string; showTime?: string; date?: string; showDate?: string; show_date?: string; screenNumber?: number | string; screen_number?: number | string }[];
        showDate?: string;
      })
    | (Partial<Movie> & {
        showTimesDetailed?: { time?: string; showTime?: string; date?: string; showDate?: string; show_date?: string; screenNumber?: number | string; screen_number?: number | string }[];
        showDate?: string;
      })
): Array<{ time: string; date: string; screen_number?: string | number }> => {
  const detailed = (movieData as any).showTimesDetailed as
    | { time?: string; showTime?: string; date?: string; showDate?: string; show_date?: string; screenNumber?: number | string; screen_number?: number | string }[]
    | undefined;

  const fallbackShowDate = toMysqlDate((movieData as any).showDate);

  let showTimesPayload: Array<{ time: string; date: string; screen_number?: string | number }> = [];

  if (Array.isArray(detailed) && detailed.length > 0) {
    showTimesPayload = detailed.map((st) => {
      const stAny = st as any;
      const timeRaw = stAny.time ?? stAny.show_time ?? stAny.showTime;
      const dateRaw = stAny.date ?? stAny.show_date ?? stAny.showDate ?? fallbackShowDate;
      const screen_number = stAny.screen_number ?? stAny.screenNumber;
      const time = toMysqlTime(timeRaw);
      const date = toMysqlDate(dateRaw);
      return { time: String(time ?? ''), date: String(date ?? ''), ...(screen_number != null ? { screen_number } : {}) };
    });
  } else {
    const times = (movieData as any).showTimes as string[] | undefined;
    showTimesPayload = (times ?? []).map((t) => {
      const time = toMysqlTime(t);
      const date = fallbackShowDate;
      return {
        time: String(time ?? ''),
        date: String(date ?? ''),
      };
    });
  }

  // Trim and validate
  showTimesPayload = showTimesPayload.map((st) => ({
    time: st.time?.toString().trim(),
    date: st.date?.toString().trim(),
    ...(st.screen_number != null ? { screen_number: st.screen_number } : {}),
  }));

  if (showTimesPayload.length === 0) {
    throw new Error('At least one show time is required. Provide showTimesDetailed or showTimes + showDate.');
  }
  if (showTimesPayload.some((st) => !st.date)) {
    throw new Error('Show date is required for each show time. Ensure date/showDate/show_date or top-level showDate is provided.');
  }
  if (showTimesPayload.some((st) => !st.time)) {
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
      showTimesDetailed?: { time?: string; showTime?: string; date?: string; showDate?: string; show_date?: string; screenNumber?: number | string; screen_number?: number | string }[];
      showDate?: string;
    }
  ): Promise<Movie> => {
    const show_times = buildShowTimesPayload(movieData);

    const payload = {
      movie_name: movieData.movieName,
      theatre_name: movieData.theatreName,
      total_tickets: movieData.totalTickets,
      available_tickets: movieData.availableTickets,
      show_times, // Jackson SNAKE_CASE -> List<ShowTime> fields { time, date, screen_number }
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
      showTimesDetailed?: { time?: string; showTime?: string; date?: string; showDate?: string; show_date?: string; screenNumber?: number | string; screen_number?: number | string }[];
      showDate?: string;
    }
  ): Promise<Movie> => {
    const show_times = buildShowTimesPayload(movieData);

    const payload = {
      movie_name: movieName,
      theatre_name: theatreName,
      total_tickets: movieData.totalTickets ?? movieData.availableTickets,
      available_tickets: movieData.availableTickets ?? movieData.totalTickets,
      show_times, // SNAKE_CASE
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
