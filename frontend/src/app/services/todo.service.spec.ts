import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TodoService } from './todo.service';
import { Todo } from '../models/todo.model';
import { environment } from '../../environments/environment';

describe('TodoService', () => {
  let service: TodoService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiBaseUrl}/todos`;

  const sampleTodo: Todo = { id: 1, title: 'Buy milk', description: 'From the store', done: false };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(TodoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should GET the list of todos', () => {
    service.list().subscribe((todos) => {
      expect(todos).toEqual([sampleTodo]);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush([sampleTodo]);
  });

  it('should GET todos matching a search keyword', () => {
    service.search('milk').subscribe((todos) => {
      expect(todos).toEqual([sampleTodo]);
    });

    const req = httpMock.expectOne((r) => r.url === `${baseUrl}/search`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('keyword')).toBe('milk');
    req.flush([sampleTodo]);
  });

  it('should GET a single todo by id', () => {
    service.get(1).subscribe((todo) => {
      expect(todo).toEqual(sampleTodo);
    });

    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(sampleTodo);
  });

  it('should POST a new todo', () => {
    const newTodo: Todo = { title: 'New', description: '', done: false };

    service.create(newTodo).subscribe((todo) => {
      expect(todo).toEqual(sampleTodo);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newTodo);
    req.flush(sampleTodo);
  });

  it('should PUT an updated todo', () => {
    service.update(1, sampleTodo).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(sampleTodo);
    req.flush({});
  });

  it('should DELETE a todo by id', () => {
    service.delete(1).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
