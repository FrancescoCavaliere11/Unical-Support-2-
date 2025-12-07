import { Injectable } from '@angular/core';
import {Environment} from '../utils/environment';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class Document {
  private _apiUrl: string = Environment.getInstance().apiUrl + 'document';

  constructor(private http: HttpClient) {}

  uploadDocuments(file: File, categoryId: string) {
    const formData = new FormData();

    formData.append('document', file);
    formData.append('categoryId', categoryId);

    return this.http.post(this._apiUrl, formData);
  }
}
