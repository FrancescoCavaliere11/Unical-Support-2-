import { Injectable } from '@angular/core';
import {Environment} from '../utils/environment';
import {BehaviorSubject, map, Observable, tap} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {TemplateDto} from '../model/template-dto';

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

  createTemplate(createDto: {
    name: string; categoryId: string; content: string; description: string;
    parameters: { name: string; required: boolean; }[]
  }) {
    return this.http.post<TemplateDto>(this._apiUrl, createDto).pipe(
      tap((newTemplate: TemplateDto) => {
        console.log(newTemplate);
        const currentTemplates = this.templatesCache$.value;
        if (currentTemplates) {
          this.templatesCache$.next([newTemplate, ...currentTemplates]);
        }
      })
    );
  }

  updateTemplate(
    updateDto: {
      id: string; name: string; categoryId: string; content: string, description: string;
      parameters: { name: string; required: boolean; }[]
    }
  ) {
    return this.http.put<TemplateDto>(this._apiUrl, updateDto).pipe(
      tap((updatedTemplate: TemplateDto) => {
        const currentTemplates = this.templatesCache$.value;
          if (currentTemplates) {
            const newTemplatesList = currentTemplates.map(template =>
              template.id === updatedTemplate.id ? updatedTemplate : template
            );

            this.templatesCache$.next(newTemplatesList);
          }
      })
    );
  }

  deleteTemplate(templateId: string) {
    return this.http.delete(`${this._apiUrl}/${templateId}`).pipe(
      tap(() => {
        const currentTemplates = this.templatesCache$.value;
        if (currentTemplates) {
          const newTemplatesList = currentTemplates.filter(t => t.id !== templateId);
          this.templatesCache$.next(newTemplatesList);
        }
      })
    );
  }
}
