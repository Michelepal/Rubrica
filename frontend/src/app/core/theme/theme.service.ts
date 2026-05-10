import { Injectable } from '@angular/core';

export type ThemeMode = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly storageKey = 'rubricaJavaAngular.theme';

  applySavedTheme(): void {
    try {
      const saved = localStorage.getItem(this.storageKey) as ThemeMode | null;
      const preferred: ThemeMode = window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
      this.setTheme(saved ?? preferred);
    } catch (error) {
      console.error('Errore durante l applicazione del tema salvato.', error);
      this.applyThemeToDocument('light');
    }
  }

  setTheme(theme: ThemeMode): void {
    try {
      this.applyThemeToDocument(theme);
      localStorage.setItem(this.storageKey, theme);
    } catch (error) {
      console.error('Errore durante il salvataggio del tema.', { theme, error });
      this.applyThemeToDocument(theme);
    }
  }

  toggle(): ThemeMode {
    const current = this.current();
    const next = current === 'dark' ? 'light' : 'dark';
    this.setTheme(next);
    return next;
  }

  current(): ThemeMode {
    return document.documentElement.dataset['theme'] === 'dark' ? 'dark' : 'light';
  }

  private applyThemeToDocument(theme: ThemeMode): void {
    document.documentElement.dataset['theme'] = theme;
  }
}
