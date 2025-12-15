import { Injectable } from '@angular/core';
import {Environment} from '../utils/environment';
import {HttpClient} from '@angular/common/http';
import {EmailDto} from '../model/email-dto';
import {BehaviorSubject, map, Observable, tap} from 'rxjs';
import {DocumentDto} from '../model/document-dto';
import {TemplateDto} from '../model/template-dto';

@Injectable({
  providedIn: 'root',
})
export class Document {
  private _apiUrl: string = Environment.getInstance().apiUrl + 'document';
  private documentsCache$: BehaviorSubject<DocumentDto[] | null> = new BehaviorSubject<DocumentDto[] | null>(null);

  constructor(private http: HttpClient) {}

  private loadDocuments() {
    return this.http.get<DocumentDto[]>(this._apiUrl)
  }

  getDocuments(forceRefresh: boolean = false): Observable<DocumentDto[]> {
    if (!forceRefresh && this.documentsCache$.value !== null) {
      return this.documentsCache$.asObservable().pipe(
        map(documents => documents ?? [])
      );
    }

    return this.loadDocuments().pipe(
      tap(documents => this.documentsCache$.next(documents))
    );
  }

  uploadDocuments(file: File, categoryId: string) {
    const formData = new FormData();

    formData.append('document', file);
    formData.append('categoryId', categoryId);

    return this.http.post<DocumentDto>(this._apiUrl, formData).pipe(
      tap((newDocument: DocumentDto) => {
        console.log(newDocument);
        const currentDocument = this.documentsCache$.value;
        if (currentDocument) {
          this.documentsCache$.next([newDocument, ...currentDocument]);
        }
      })
    );
  }
}
