import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ErrorService } from './error.service';

describe('ErrorService', () => {
  let service: ErrorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ErrorService);
  });

  it('maps structured backend error', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: {
        message: 'Alcuni campi non sono validi.',
        fieldErrors: { firstName: 'Il nome e obbligatorio.' }
      }
    });

    expect(service.toMessage(error).message).toBe('Alcuni campi non sono validi.');
    expect(service.toMessage(error).fieldErrors['firstName']).toBe('Il nome e obbligatorio.');
  });

  it('maps network error', () => {
    const error = new HttpErrorResponse({ status: 0 });

    expect(service.toMessage(error).message).toContain('Connessione');
  });
});

