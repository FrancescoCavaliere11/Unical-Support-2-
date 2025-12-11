import {SingleAnswerDto} from './single-answer-dto';

export class AnswerDto {
  id: string;
  singleAnswers: SingleAnswerDto[];

  constructor(data: any) {
    this.id = data.id;
    this.singleAnswers = data.singleAnswers;
  }
}
