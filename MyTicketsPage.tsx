import { apiClient } from './api.ts';
import { Ticket } from '../types';

interface TicketBookingRequest {
  movieName: string;
  theatreName: string;
  numberOfTickets: number;
  seatNumbers: string[];
}

// Helper to map backend snake_case to frontend camelCase
const mapTicket = (t: any): Ticket => ({
  id: t.id,
  movieName: t.movieName ?? t.movie_name,
  theatreName: t.theatreName ?? t.theatre_name,
  numberOfTickets: t.numberOfTickets ?? t.number_of_tickets,
  seatNumbers: Array.isArray(t.seatNumbers) ? t.seatNumbers : (t.seat_numbers ?? []),
  userId: t.userId ?? t.user_id,
  userLoginId: t.userLoginId ?? t.user_login_id,
  status: t.status,
  totalAmount: t.totalAmount ?? t.total_price,
  bookingReference: t.bookingReference ?? t.booking_reference,
  bookingDate: t.bookingDate ?? t.booking_date,
  createdDate: t.createdDate ?? t.created_date,
  modifiedDate: t.modifiedDate ?? t.modified_date,
});

export const ticketService = {
  bookTicket: async (movieName: string, ticketData: TicketBookingRequest): Promise<Ticket> => {
    // Build payload in snake_case to match backend validation
    const payload = {
      movie_name: ticketData.movieName ?? movieName,
      theatre_name: ticketData.theatreName,
      number_of_tickets: ticketData.numberOfTickets,
      seat_numbers: ticketData.seatNumbers,
    };
    const response = await apiClient.post(`/${encodeURIComponent(movieName)}/add`, payload);
    return mapTicket(response.data.data);
  },

  getUserTickets: async (username?: string): Promise<Ticket[]> => {
    // If username is not provided, fetch tickets for the current authenticated user
    if (!username) {
      const response = await apiClient.get('/tickets/user');
      return (response.data.data ?? []).map(mapTicket);
    }
    const response = await apiClient.get(`/tickets/${encodeURIComponent(username)}`);
    return (response.data.data ?? []).map(mapTicket);
  },

  getAllTickets: async (): Promise<Ticket[]> => {
    // Admin-only endpoint
    const response = await apiClient.get('/tickets/all');
    return (response.data.data ?? []).map(mapTicket);
  },

  cancelTicket: async (bookingReference: string): Promise<void> => {
    await apiClient.delete(`/tickets/cancel/${encodeURIComponent(bookingReference)}`);
  },
};
