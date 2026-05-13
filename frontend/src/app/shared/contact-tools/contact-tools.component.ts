import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Tag } from '../../core/tags/tag-api.service';

export interface ContactToolsQuery {
  search: string;
  favoriteFilter: 'all' | 'favorites';
  filterTagId: number | null;
}

@Component({
  selector: 'app-contact-tools',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './contact-tools.component.html',
  styleUrl: './contact-tools.component.css'
})
export class ContactToolsComponent implements OnChanges {
  @Input() search = '';
  @Input() searchPlaceholder = 'Cerca contatti';
  @Input() favoriteFilter: 'all' | 'favorites' = 'all';
  @Input() filterTagId: number | null = null;
  @Input() tags: Tag[] = [];
  @Input() showControls = true;
  @Input() showSummary = false;
  @Input() totalContacts = 0;
  @Input() favoriteCount = 0;
  @Input() totalTags = 0;
  @Input() contactsToReview = 0;

  @Output() searchChange = new EventEmitter<string>();
  @Output() favoriteFilterChange = new EventEmitter<'all' | 'favorites'>();
  @Output() filterTagIdChange = new EventEmitter<number | null>();
  @Output() query = new EventEmitter<ContactToolsQuery>();

  draftSearch = '';
  draftFavoriteFilter: 'all' | 'favorites' = 'all';
  draftFilterTagId: number | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['search']) {
      this.draftSearch = this.search;
    }
    if (changes['favoriteFilter']) {
      this.draftFavoriteFilter = this.favoriteFilter;
    }
    if (changes['filterTagId']) {
      this.draftFilterTagId = this.filterTagId;
    }
  }

  onSearchChanged(value: string): void {
    this.draftSearch = value;
  }

  onFavoriteChanged(value: 'all' | 'favorites'): void {
    this.draftFavoriteFilter = value;
  }

  onTagChanged(value: number | null): void {
    this.draftFilterTagId = value;
  }

  applyQuery(): void {
    this.search = this.draftSearch;
    this.favoriteFilter = this.draftFavoriteFilter;
    this.filterTagId = this.draftFilterTagId;
    this.query.emit({
      search: this.search,
      favoriteFilter: this.favoriteFilter,
      filterTagId: this.filterTagId
    });
  }
}
