import { Injectable } from '@angular/core';
import {Environment} from '../utils/environment';
import {HttpClient} from '@angular/common/http';
import {EmailDto} from '../model/email-dto';
import {EmailToClassifyDto} from '../model/email-to-classify-dto';

@Injectable({
  providedIn: 'root',
})
export class Email {
  private _apiUrl: string = Environment.getInstance().apiUrl + 'email';

  protected constructor(private http: HttpClient) {}

  getEmails() {
    return this.http.get<EmailToClassifyDto[]>(this._apiUrl)
  }

  updateCategoryForEmail(
    updateDto :{ id: string, categoryId: string }
  ) {
    return this.http.patch(this._apiUrl, updateDto)
  }
}
