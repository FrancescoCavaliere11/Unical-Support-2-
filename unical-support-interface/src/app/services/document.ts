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
    if (forceRefresh || this.documentsCache$.value === null) {
      this.loadDocuments().subscribe(
        documents => this.documentsCache$.next(documents)
      );
    }

    return this.documentsCache$.asObservable().pipe(
      map(documents => documents ?? [])
    );

    /*
    if (!forceRefresh && this.documentsCache$.value !== null) {
      return this.documentsCache$.asObservable().pipe(
        map(documents => documents ?? [])
      );
    }

    return this.loadDocuments().pipe(
      tap(documents => this.documentsCache$.next(documents))
    );
     */
  }

  uploadDocuments(file: File, documentCreateDto: {categoryId: string, documentLink: string | null}) {
    const formData = new FormData();

    formData.append('document', file);
    formData.append(
      'documentCreateDto',
      new Blob( [JSON.stringify(documentCreateDto)], { type: 'application/json' } ) );

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

  deleteDocument(documentId: string) {
    return this.http.delete(`${this._apiUrl}/${documentId}`).pipe(
      tap(() => {
        const currentDocuments = this.documentsCache$.value;
        if (currentDocuments) {
          const newDocumentsList = currentDocuments.filter(t => t.id !== documentId);
          this.documentsCache$.next(newDocumentsList);
        }
      })
    );
  }
}
