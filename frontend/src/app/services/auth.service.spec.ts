import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiBaseUrl}/auth`;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created with no token when localStorage is empty', () => {
    expect(service.token).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
    expect(service.username()).toBeNull();
  });

  it('should send a POST request to register', () => {
    const credentials = { username: 'alice', password: 'pw' };

    service.register(credentials).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(credentials);
    req.flush({});
  });

  it('should store the token and username in localStorage on successful login', () => {
    const credentials = { username: 'bob', password: 'pw' };

    service.login(credentials).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ token: 'abc123' });

    expect(localStorage.getItem('auth_token')).toBe('abc123');
    expect(localStorage.getItem('auth_username')).toBe('bob');
    expect(service.token).toBe('abc123');
    expect(service.isLoggedIn()).toBeTrue();
    expect(service.username()).toBe('bob');
  });

  it('should not update state when login fails', () => {
    const credentials = { username: 'carol', password: 'wrong' };

    service.login(credentials).subscribe({
      next: () => fail('expected error'),
      error: () => {}
    });

    const req = httpMock.expectOne(`${baseUrl}/login`);
    req.flush({ error: 'invalid credentials' }, { status: 401, statusText: 'Unauthorized' });

    expect(service.token).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('should clear token and username on logout', () => {
    const credentials = { username: 'dave', password: 'pw' };

    service.login(credentials).subscribe();
    httpMock.expectOne(`${baseUrl}/login`).flush({ token: 'xyz' });

    expect(service.isLoggedIn()).toBeTrue();

    service.logout();

    expect(service.token).toBeNull();
    expect(service.username()).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(localStorage.getItem('auth_username')).toBeNull();
  });
});
