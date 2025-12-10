import { Injectable } from '@angular/core';
import {Environment} from '../utils/environment';
import {HttpClient} from '@angular/common/http';
import {EmailDto} from '../model/email-dto';

@Injectable({
  providedIn: 'root',
})
export class Answer {
  private _apiUrl: string = Environment.getInstance().apiUrl + 'answers';

  protected constructor(private http: HttpClient) {}

  updateAndSendResponse(email: EmailDto) {
    return this.http.put(this._apiUrl, email);
  }
}
