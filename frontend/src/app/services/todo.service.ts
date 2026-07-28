import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Todo } from '../models/todo.model';

@Injectable({ providedIn: 'root' })
export class TodoService {
  private readonly baseUrl = `${environment.apiBaseUrl}/todos`;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<Todo[]> {
    return this.http.get<Todo[]>(this.baseUrl);
  }

  search(keyword: string): Observable<Todo[]> {
    return this.http.get<Todo[]>(`${this.baseUrl}/search`, { params: { keyword } });
  }

  get(id: number): Observable<Todo> {
    return this.http.get<Todo>(`${this.baseUrl}/${id}`);
  }

  create(todo: Todo): Observable<Todo> {
    return this.http.post<Todo>(this.baseUrl, todo);
  }

  update(id: number, todo: Todo): Observable<unknown> {
    return this.http.put(`${this.baseUrl}/${id}`, todo);
  }

  delete(id: number): Observable<unknown> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
