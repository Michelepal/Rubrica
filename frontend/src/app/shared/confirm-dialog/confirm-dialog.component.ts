import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.css'
})
export class ConfirmDialogComponent {
  @Input() open = false;
  @Input() title = 'Conferma operazione';
  @Input() message = 'Confermi di voler procedere?';
  @Input() destructive = false;
  @Input() confirmLabel = 'Conferma';
  @Input() cancelLabel = 'Annulla';
  @Input() showCancel = true;
  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();
}
