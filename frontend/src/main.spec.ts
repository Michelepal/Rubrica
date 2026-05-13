import { routes } from './app/app.routes';

describe('application routes', () => {
  it('lazy loads each main page as a standalone component', () => {
    expect(routes.find(route => route.path === 'home')?.loadComponent).toEqual(jasmine.any(Function));
    expect(routes.find(route => route.path === 'contacts')?.loadComponent).toEqual(jasmine.any(Function));
    expect(routes.find(route => route.path === 'tags')?.loadComponent).toEqual(jasmine.any(Function));
  });
});
