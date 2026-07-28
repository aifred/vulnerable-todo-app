import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { TodoListComponent } from './todo-list.component';
import { TodoService } from '../../services/todo.service';
import { AuthService } from '../../services/auth.service';
import { Todo } from '../../models/todo.model';

describe('TodoListComponent', () => {
  let todoServiceSpy: jasmine.SpyObj<TodoService>;
  let authServiceStub: { username: () => string | null; logout: jasmine.Spy };
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;
  let router: Router;

  const todo: Todo = { id: 1, title: 'Buy milk', description: 'From the store', done: false };

  beforeEach(async () => {
    todoServiceSpy = jasmine.createSpyObj('TodoService', ['list', 'search', 'create', 'update', 'delete']);
    todoServiceSpy.list.and.returnValue(of([todo]));
    authServiceStub = { username: () => 'alice', logout: jasmine.createSpy('logout') };
    snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [TodoListComponent],
      providers: [
        provideRouter([]),
        provideNoopAnimations(),
        { provide: TodoService, useValue: todoServiceSpy },
        { provide: AuthService, useValue: authServiceStub },
        { provide: MatSnackBar, useValue: snackBarSpy }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
  });

  function createComponent() {
    const fixture = TestBed.createComponent(TodoListComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should load todos on init', () => {
    const fixture = createComponent();

    expect(todoServiceSpy.list).toHaveBeenCalled();
    expect(fixture.componentInstance.todos()).toEqual([todo]);
    expect(fixture.componentInstance.loading()).toBeFalse();
  });

  it('should set an error message when loading todos fails', () => {
    todoServiceSpy.list.and.returnValue(throwError(() => new Error('boom')));

    const fixture = createComponent();

    expect(fixture.componentInstance.loading()).toBeFalse();
    expect(fixture.componentInstance.errorMessage()).toBe('Could not load todos.');
  });

  it('should search using the trimmed search term', () => {
    todoServiceSpy.search.and.returnValue(of([todo]));
    const fixture = createComponent();
    fixture.componentInstance.searchTermValue = '  milk  ';

    fixture.componentInstance.onSearch();

    expect(todoServiceSpy.search).toHaveBeenCalledWith('milk');
  });

  it('should clear the search term and reload the full list', () => {
    todoServiceSpy.search.and.returnValue(of([todo]));
    const fixture = createComponent();
    fixture.componentInstance.searchTermValue = 'milk';
    fixture.componentInstance.onSearch();
    todoServiceSpy.list.calls.reset();

    fixture.componentInstance.clearSearch();

    expect(fixture.componentInstance.searchTermValue).toBe('');
    expect(todoServiceSpy.list).toHaveBeenCalled();
  });

  it('should not add a todo when the add form is invalid', () => {
    const fixture = createComponent();
    fixture.componentInstance.addForm.setValue({ title: '', description: '' });

    fixture.componentInstance.addTodo();

    expect(todoServiceSpy.create).not.toHaveBeenCalled();
  });

  it('should create a todo and refresh the list on success', () => {
    todoServiceSpy.create.and.returnValue(of(todo));
    const fixture = createComponent();
    fixture.componentInstance.addForm.setValue({ title: 'Buy milk', description: 'From the store' });
    todoServiceSpy.list.calls.reset();

    fixture.componentInstance.addTodo();

    expect(todoServiceSpy.create).toHaveBeenCalledWith({
      title: 'Buy milk',
      description: 'From the store',
      done: false
    });
    expect(todoServiceSpy.list).toHaveBeenCalled();
  });

  it('should show a snack bar message when creating a todo fails', () => {
    todoServiceSpy.create.and.returnValue(throwError(() => new Error('boom')));
    const fixture = createComponent();
    fixture.componentInstance.addForm.setValue({ title: 'Buy milk', description: '' });

    fixture.componentInstance.addTodo();

    expect(snackBarSpy.open).toHaveBeenCalledWith('Failed to add todo.', 'Dismiss', { duration: 3000 });
  });

  it('should toggle the done state of a todo', () => {
    todoServiceSpy.update.and.returnValue(of({}));
    const fixture = createComponent();

    fixture.componentInstance.toggleDone(todo);

    expect(todoServiceSpy.update).toHaveBeenCalledWith(1, { ...todo, done: true });
  });

  it('should do nothing when toggling a todo without an id', () => {
    const fixture = createComponent();

    fixture.componentInstance.toggleDone({ title: 'x', description: '', done: false });

    expect(todoServiceSpy.update).not.toHaveBeenCalled();
  });

  it('should populate the edit form when starting an edit', () => {
    const fixture = createComponent();

    fixture.componentInstance.startEdit(todo);

    expect(fixture.componentInstance.editingId()).toBe(1);
    expect(fixture.componentInstance.editForm.getRawValue()).toEqual({
      title: 'Buy milk',
      description: 'From the store'
    });
  });

  it('should clear the editing id when cancelling an edit', () => {
    const fixture = createComponent();
    fixture.componentInstance.startEdit(todo);

    fixture.componentInstance.cancelEdit();

    expect(fixture.componentInstance.editingId()).toBeNull();
  });

  it('should save edits and refresh the list on success', () => {
    todoServiceSpy.update.and.returnValue(of({}));
    const fixture = createComponent();
    fixture.componentInstance.startEdit(todo);
    fixture.componentInstance.editForm.setValue({ title: 'Buy bread', description: 'Updated' });

    fixture.componentInstance.saveEdit(todo);

    expect(todoServiceSpy.update).toHaveBeenCalledWith(1, {
      ...todo,
      title: 'Buy bread',
      description: 'Updated'
    });
    expect(fixture.componentInstance.editingId()).toBeNull();
  });

  it('should delete a todo and refresh the list', () => {
    todoServiceSpy.delete.and.returnValue(of({}));
    const fixture = createComponent();

    fixture.componentInstance.deleteTodo(todo);

    expect(todoServiceSpy.delete).toHaveBeenCalledWith(1);
  });

  it('should show a snack bar message when deleting a todo fails', () => {
    todoServiceSpy.delete.and.returnValue(throwError(() => new Error('boom')));
    const fixture = createComponent();

    fixture.componentInstance.deleteTodo(todo);

    expect(snackBarSpy.open).toHaveBeenCalledWith('Failed to delete todo.', 'Dismiss', { duration: 3000 });
  });

  it('should log out and navigate to /login', () => {
    const fixture = createComponent();

    fixture.componentInstance.logout();

    expect(authServiceStub.logout).toHaveBeenCalled();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });
});
