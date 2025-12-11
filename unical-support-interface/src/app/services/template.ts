import { Injectable } from '@angular/core';
import {Environment} from '../utils/environment';
import {BehaviorSubject, map, Observable, tap} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {TemplateDto} from '../model/TemplateDto';

@Injectable({
  providedIn: 'root',
})
export class Template {
  private _apiUrl: string = Environment.getInstance().apiUrl + 'template';
  private templatesCache$: BehaviorSubject<TemplateDto[] | null> = new BehaviorSubject<TemplateDto[] | null>(null);

  protected constructor(private http: HttpClient) {}

  private loadTemplate() {
    return this.http.get<TemplateDto[]>(this._apiUrl)
  }

  getTemplates(forceRefresh: boolean = false): Observable<TemplateDto[]> {
    if (!forceRefresh && this.templatesCache$.value !== null) {
      return this.templatesCache$.asObservable().pipe(
        map(templates => templates ?? [])
      );
    }

    return this.loadTemplate().pipe(
      tap(templates => this.templatesCache$.next(templates))
    );
  }

  createTemplate(
    createDto: {
      name: string;
      categoryId: string;
      content: string;
      parameters: {
        name: string;
        required: boolean;
      }[]
    }
  ) {
    return this.http.post<TemplateDto>(this._apiUrl, createDto).pipe(
      tap(() => this.getTemplates(true).subscribe())
    );
  }

  updateTemplate(
    createDto: {
      id: string;
      name: string;
      categoryId: string;
      content: string;
      parameters: {
        name: string;
        required: boolean;
      }[]
    }
  ) {
    return this.http.put(this._apiUrl, createDto).pipe(
      tap(() => this.getTemplates(true).subscribe())
    );
  }

  deleteTemplate(templateId: string) {
    return this.http.delete(`${this._apiUrl}/${templateId}`).pipe(
      tap(() => this.getTemplates(true).subscribe())
    );
  }
}
