import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authServiceStub: { token: string | null };

  beforeEach(() => {
    authServiceStub = { token: null };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useFactory: () => authServiceStub }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should add an X-Auth-Token header when a token is present', () => {
    authServiceStub.token = 'my-token';

    httpClient.get('/api/todos').subscribe();

    const req = httpMock.expectOne('/api/todos');
    expect(req.request.headers.get('X-Auth-Token')).toBe('my-token');
    req.flush({});
  });

  it('should not add an X-Auth-Token header when no token is present', () => {
    authServiceStub.token = null;

    httpClient.get('/api/todos').subscribe();

    const req = httpMock.expectOne('/api/todos');
    expect(req.request.headers.has('X-Auth-Token')).toBeFalse();
    req.flush({});
  });
});
