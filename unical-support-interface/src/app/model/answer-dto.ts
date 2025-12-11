import {SingleAnswerDto} from './single-answer-dto';

export class AnswerDto {
  id: string;
  answered: boolean;
  singleAnswers: SingleAnswerDto[];

  constructor(data: any) {
    this.id = data.id;
    this.answered = data.answered;
    this.singleAnswers = data.singleAnswers;
  }
}
