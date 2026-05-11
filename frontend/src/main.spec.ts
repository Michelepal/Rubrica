import { ContactsComponent } from './app/pages/contacts/contacts.component';
import { routes } from './app/app.routes';
import { HomeComponent } from './app/pages/home/home.component';
import { TagsComponent } from './app/pages/tags/tags.component';

describe('application routes', () => {
  it('maps each main page to its own standalone component', () => {
    expect(routes.find(route => route.path === 'home')?.component).toBe(HomeComponent);
    expect(routes.find(route => route.path === 'contacts')?.component).toBe(ContactsComponent);
    expect(routes.find(route => route.path === 'tags')?.component).toBe(TagsComponent);
  });
});
