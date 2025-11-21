import { Injectable } from '@angular/core';
import {Environment} from '../utils/environment';
import {HttpClient} from '@angular/common/http';
import {CategoryDto} from '../model/category-dto';
import {BehaviorSubject, map, Observable, tap} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Category {
  private _apiUrl: string = Environment.getInstance().apiUrl + 'category';
  private categoriesCache$: BehaviorSubject<CategoryDto[] | null> = new BehaviorSubject<CategoryDto[] | null>(null);

  protected constructor(private http: HttpClient) {}

  private loadCategories() {
    return this.http.get<CategoryDto[]>(this._apiUrl)
  }

  getCategories(forceRefresh: boolean = false): Observable<CategoryDto[]> {
    if (!forceRefresh && this.categoriesCache$.value !== null) {
      return this.categoriesCache$.asObservable().pipe(
        map(categories => categories ?? [])
      );
    }

    return this.loadCategories().pipe(
      tap(categories => this.categoriesCache$.next(categories))
    );
  }
}
