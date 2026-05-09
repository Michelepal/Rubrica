import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  let service: ThemeService;

  beforeEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
    TestBed.configureTestingModule({});
    service = TestBed.inject(ThemeService);
  });

  it('sets and persists dark theme', () => {
    service.setTheme('dark');

    expect(document.documentElement.dataset['theme']).toBe('dark');
    expect(localStorage.getItem('rubricaJavaAngular.theme')).toBe('dark');
  });

  it('toggles between themes', () => {
    service.setTheme('light');

    expect(service.toggle()).toBe('dark');
    expect(service.toggle()).toBe('light');
  });
});

