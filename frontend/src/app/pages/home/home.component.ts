import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ThemeService } from '../../core/theme/theme.service';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';

interface ContactVm {
  id: number;
  name: string;
  company: string;
  email: string;
  phone: string;
  tags: string[];
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [FormsModule, ConfirmDialogComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  private readonly authService = inject(AuthService);
  private readonly themeService = inject(ThemeService);
  private readonly router = inject(Router);

  search = '';
  modalOpen = false;
  modalTitle = '';
  modalMessage = '';
  modalDestructive = false;

  contacts: ContactVm[] = [
    { id: 1, name: 'Laura Bianchi', company: 'Northwind', email: 'laura.bianchi@example.local', phone: '+39 333 123 4567', tags: ['Lavoro', 'VIP'] },
    { id: 2, name: 'Marco Rossi', company: 'Studio Rossi', email: 'm.rossi@example.local', phone: '02 555 0199', tags: ['Clienti'] },
    { id: 3, name: 'Giulia Verdi', company: 'Personale', email: 'giulia.verdi@example.local', phone: '+39 347 222 8899', tags: ['Famiglia'] }
  ];

  get filteredContacts(): ContactVm[] {
    const term = this.search.trim().toLowerCase();
    if (!term) {
      return this.contacts;
    }
    return this.contacts.filter(contact => JSON.stringify(contact).toLowerCase().includes(term));
  }

  toggleTheme(): void {
    this.themeService.toggle();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  askEditTag(tag: string): void {
    this.openModal('Conferma modifica tag', `Vuoi modificare il tag "${tag}"?`, false);
  }

  askDeleteTag(tag: string): void {
    this.openModal('Conferma cancellazione tag', `Vuoi cancellare il tag "${tag}"?`, true);
  }

  askDeleteContact(contact: ContactVm): void {
    this.openModal('Conferma cancellazione contatto', `Vuoi cancellare ${contact.name}?`, true);
  }

  closeModal(): void {
    this.modalOpen = false;
  }

  openModal(title: string, message: string, destructive: boolean): void {
    this.modalTitle = title;
    this.modalMessage = message;
    this.modalDestructive = destructive;
    this.modalOpen = true;
  }
}
