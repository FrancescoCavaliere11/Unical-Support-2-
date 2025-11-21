import { Injectable } from '@angular/core';
import {Environment} from '../utils/environment';
import {HttpClient} from '@angular/common/http';
import {EmailDto} from '../model/email-dto';

@Injectable({
  providedIn: 'root',
})
export class Email {
  private _apiUrl: string = Environment.getInstance().apiUrl + 'email';

  protected constructor(private http: HttpClient) {}

  protected getEmails() {
    return this.http.get<EmailDto[]>(this._apiUrl)
  }

  protected updateCategoryForEmail(
    id: string,
    categoryId: string
  ) {
    return this.http.patch<EmailDto[]>(this._apiUrl, {id, categoryId})
  }
}
