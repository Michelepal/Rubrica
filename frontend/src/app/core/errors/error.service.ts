import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

export interface ApiError {
  message: string;
  fieldErrors: Record<string, string>;
}

@Injectable({ providedIn: 'root' })
export class ErrorService {
  toMessage(error: unknown): ApiError {
    if (error instanceof HttpErrorResponse) {
      const body = error.error;
      if (body?.message) {
        return { message: body.message, fieldErrors: body.fieldErrors ?? {} };
      }
      return { message: this.statusMessage(error.status), fieldErrors: {} };
    }
    if (error instanceof Error) {
      return { message: error.message, fieldErrors: {} };
    }
    return { message: 'Si e verificato un errore imprevisto.', fieldErrors: {} };
  }

  private statusMessage(status: number): string {
    const messages: Record<number, string> = {
      0: 'Connessione non disponibile. Verifica la rete o il server.',
      400: 'La richiesta contiene dati non validi.',
      401: 'Sessione non valida o credenziali errate.',
      403: 'Non hai i permessi per questa operazione.',
      404: 'Risorsa non trovata.',
      409: 'Esiste gia un dato con queste informazioni.',
      500: 'Errore del server. Riprova piu tardi.'
    };
    return messages[status] ?? 'Operazione non riuscita.';
  }
}
