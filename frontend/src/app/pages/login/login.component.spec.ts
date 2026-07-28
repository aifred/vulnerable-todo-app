import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';

describe('LoginComponent', () => {
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        provideNoopAnimations(),
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
  });

  function createComponent() {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create the component', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should not submit when the form is invalid', () => {
    const fixture = createComponent();
    fixture.componentInstance.form.setValue({ username: '', password: '' });

    fixture.componentInstance.submit();

    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('should log in and navigate to /todos on successful submit', () => {
    authServiceSpy.login.and.returnValue(of({ token: 'abc' }));
    const fixture = createComponent();
    fixture.componentInstance.form.setValue({ username: 'alice', password: 'pw' });

    fixture.componentInstance.submit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({ username: 'alice', password: 'pw' });
    expect(fixture.componentInstance.loading()).toBeFalse();
    expect(fixture.componentInstance.errorMessage()).toBeNull();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/todos');
  });

  it('should set an error message and stop loading when login fails', () => {
    authServiceSpy.login.and.returnValue(
      throwError(() => ({ error: { error: 'Invalid credentials' } }))
    );
    const fixture = createComponent();
    fixture.componentInstance.form.setValue({ username: 'alice', password: 'wrong' });

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.loading()).toBeFalse();
    expect(fixture.componentInstance.errorMessage()).toBe('Invalid credentials');
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('should fall back to a default error message when none is provided by the server', () => {
    authServiceSpy.login.and.returnValue(throwError(() => ({})));
    const fixture = createComponent();
    fixture.componentInstance.form.setValue({ username: 'alice', password: 'wrong' });

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.errorMessage()).toBe('Login failed. Check your credentials.');
  });
});
