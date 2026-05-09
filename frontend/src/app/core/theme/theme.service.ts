import { Injectable } from '@angular/core';

export type ThemeMode = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly storageKey = 'rubricaJavaAngular.theme';

  applySavedTheme(): void {
    const saved = localStorage.getItem(this.storageKey) as ThemeMode | null;
    const preferred: ThemeMode = window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    this.setTheme(saved ?? preferred);
  }

  setTheme(theme: ThemeMode): void {
    document.documentElement.dataset['theme'] = theme;
    localStorage.setItem(this.storageKey, theme);
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
}

