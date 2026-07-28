import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    routerSpy = jasmine.createSpyObj('Router', ['parseUrl']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });
  });

  function runGuard() {
    return TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));
  }

  it('should allow activation when the user is logged in', () => {
    authServiceSpy.isLoggedIn.and.returnValue(true);

    const result = runGuard();

    expect(result).toBeTrue();
    expect(routerSpy.parseUrl).not.toHaveBeenCalled();
  });

  it('should redirect to /login when the user is not logged in', () => {
    authServiceSpy.isLoggedIn.and.returnValue(false);
    const urlTree = {} as UrlTree;
    routerSpy.parseUrl.and.returnValue(urlTree);

    const result = runGuard();

    expect(routerSpy.parseUrl).toHaveBeenCalledWith('/login');
    expect(result).toBe(urlTree);
  });
});
