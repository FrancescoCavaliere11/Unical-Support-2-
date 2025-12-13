import { Injectable } from '@angular/core';
import {Environment} from '../utils/environment';
import {HttpClient} from '@angular/common/http';
import {EmailDto} from '../model/email-dto';
import {BehaviorSubject, map, Observable, tap} from 'rxjs';


@Injectable({
  providedIn: 'root',
})
export class Email {
  private _apiUrl: string = Environment.getInstance().apiUrl + 'email';
  private emailsCache$: BehaviorSubject<EmailDto[] | null> = new BehaviorSubject<EmailDto[] | null>(null);

  protected constructor(private http: HttpClient) {}

  private loadEmails() {
    return this.http.get<EmailDto[]>(this._apiUrl)
  }

  getEmails(forceRefresh: boolean = false): Observable<EmailDto[]> {
    if (!forceRefresh && this.emailsCache$.value !== null) {
      return this.emailsCache$.asObservable().pipe(
        map(emails => emails ?? [])
      );
    }

    return this.loadEmails().pipe(
      tap(emails => this.emailsCache$.next(emails))
    );
  }

  updateCategoryForEmail(
    updateDto: { id: string, updateSingleClassificationDtos: Array<{ categoryId: string, text: string }> }
  ) {
    return this.http.patch(this._apiUrl, updateDto)
  }

  updateAndSendResponse(updateAnswerDto: { id: string, singleAnswers: Array<{ answer: string, template_id: string | null }> }) {
    return this.http.put<EmailDto>(this._apiUrl + "/answer", updateAnswerDto);
  }
}
