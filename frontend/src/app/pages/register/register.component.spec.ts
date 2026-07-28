import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';

describe('RegisterComponent', () => {
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['register', 'login']);

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
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
    const fixture = TestBed.createComponent(RegisterComponent);
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

    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('should register, log in, and navigate to /todos on success', () => {
    authServiceSpy.register.and.returnValue(of({}));
    authServiceSpy.login.and.returnValue(of({ token: 'abc' }));
    const fixture = createComponent();
    fixture.componentInstance.form.setValue({ username: 'alice', password: 'pw' });

    fixture.componentInstance.submit();

    expect(authServiceSpy.register).toHaveBeenCalledWith({ username: 'alice', password: 'pw' });
    expect(authServiceSpy.login).toHaveBeenCalledWith({ username: 'alice', password: 'pw' });
    expect(fixture.componentInstance.loading()).toBeFalse();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/todos');
  });

  it('should navigate to /login if register succeeds but auto-login fails', () => {
    authServiceSpy.register.and.returnValue(of({}));
    authServiceSpy.login.and.returnValue(throwError(() => ({})));
    const fixture = createComponent();
    fixture.componentInstance.form.setValue({ username: 'alice', password: 'pw' });

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.loading()).toBeFalse();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });

  it('should set an error message and stop loading when registration fails', () => {
    authServiceSpy.register.and.returnValue(
      throwError(() => ({ error: { error: 'Username taken' } }))
    );
    const fixture = createComponent();
    fixture.componentInstance.form.setValue({ username: 'alice', password: 'pw' });

    fixture.componentInstance.submit();

    expect(authServiceSpy.login).not.toHaveBeenCalled();
    expect(fixture.componentInstance.loading()).toBeFalse();
    expect(fixture.componentInstance.errorMessage()).toBe('Username taken');
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('should fall back to a default error message when none is provided by the server', () => {
    authServiceSpy.register.and.returnValue(throwError(() => ({})));
    const fixture = createComponent();
    fixture.componentInstance.form.setValue({ username: 'alice', password: 'pw' });

    fixture.componentInstance.submit();

    expect(fixture.componentInstance.errorMessage()).toBe('Registration failed. Try a different username.');
  });
});
