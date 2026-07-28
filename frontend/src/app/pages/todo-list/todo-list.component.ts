import { Component, OnInit, signal } from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Todo } from '../../models/todo.model';
import { TodoService } from '../../services/todo.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-todo-list',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatToolbarModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './todo-list.component.html',
  styleUrl: './todo-list.component.css'
})
export class TodoListComponent implements OnInit {
  private readonly fb = new FormBuilder();

  readonly todos = signal<Todo[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly searchTerm = signal('');
  readonly editingId = signal<number | null>(null);

  readonly addForm = this.fb.nonNullable.group({
    title: ['', Validators.required],
    description: ['']
  });

  readonly editForm = this.fb.nonNullable.group({
    title: ['', Validators.required],
    description: ['']
  });

  constructor(
    private readonly todoService: TodoService,
    readonly authService: AuthService,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar
  ) {}

  get searchTermValue(): string {
    return this.searchTerm();
  }

  set searchTermValue(value: string) {
    this.searchTerm.set(value);
  }

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const request = this.searchTerm().trim()
      ? this.todoService.search(this.searchTerm().trim())
      : this.todoService.list();

    request.subscribe({
      next: (todos) => {
        this.todos.set(todos);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Could not load todos.');
      }
    });
  }

  onSearch(): void {
    this.refresh();
  }

  clearSearch(): void {
    this.searchTerm.set('');
    this.refresh();
  }

  addTodo(): void {
    if (this.addForm.invalid) {
      return;
    }

    const value = this.addForm.getRawValue();
    this.todoService.create({ title: value.title, description: value.description, done: false }).subscribe({
      next: () => {
        this.addForm.reset({ title: '', description: '' });
        this.refresh();
      },
      error: () => this.snackBar.open('Failed to add todo.', 'Dismiss', { duration: 3000 })
    });
  }

  toggleDone(todo: Todo): void {
    if (todo.id === undefined) {
      return;
    }

    const updated: Todo = { ...todo, done: !todo.done };
    this.todoService.update(todo.id, updated).subscribe({
      next: () => this.refresh(),
      error: () => this.snackBar.open('Failed to update todo.', 'Dismiss', { duration: 3000 })
    });
  }

  startEdit(todo: Todo): void {
    this.editingId.set(todo.id ?? null);
    this.editForm.setValue({ title: todo.title, description: todo.description ?? '' });
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  saveEdit(todo: Todo): void {
    if (this.editForm.invalid || todo.id === undefined) {
      return;
    }

    const value = this.editForm.getRawValue();
    const updated: Todo = { ...todo, title: value.title, description: value.description };

    this.todoService.update(todo.id, updated).subscribe({
      next: () => {
        this.editingId.set(null);
        this.refresh();
      },
      error: () => this.snackBar.open('Failed to save changes.', 'Dismiss', { duration: 3000 })
    });
  }

  deleteTodo(todo: Todo): void {
    if (todo.id === undefined) {
      return;
    }

    this.todoService.delete(todo.id).subscribe({
      next: () => this.refresh(),
      error: () => this.snackBar.open('Failed to delete todo.', 'Dismiss', { duration: 3000 })
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
