import { apiClient } from './api.ts';
import { Ticket } from '../types';

interface TicketBookingRequest {
  movieName: string;
  theatreName: string;
  numberOfTickets: number;
  seatNumbers: string[];
}

export const ticketService = {
  bookTicket: async (movieName: string, ticketData: TicketBookingRequest): Promise<Ticket> => {
    // Ensure seat count matches seats to avoid validation errors
    const payload = {
      movieName: ticketData.movieName ?? movieName,
      theatreName: ticketData.theatreName,
      numberOfTickets: ticketData.numberOfTickets ?? ticketData.seatNumbers?.length,
      seatNumbers: ticketData.seatNumbers,
    };
    const response = await apiClient.post(`/${encodeURIComponent(movieName)}/add`, payload);
    return response.data.data;
  },

  getUserTickets: async (username: string): Promise<Ticket[]> => {
    if (!username) {
      const response = await apiClient.get('/tickets/all');
      return response.data.data;
    }
    const response = await apiClient.get(`/tickets/${encodeURIComponent(username)}`);
    return response.data.data;
  },

  getAllTickets: async (): Promise<Ticket[]> => {
    const response = await apiClient.get('/tickets/all');
    return response.data.data;
  },

  cancelTicket: async (bookingReference: string): Promise<void> => {
    await apiClient.delete(`/tickets/cancel/${encodeURIComponent(bookingReference)}`);
  },
};
