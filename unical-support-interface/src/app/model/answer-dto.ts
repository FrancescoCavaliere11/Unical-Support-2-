import {SingleAnswerDto} from './single-answer-dto';

export class AnswerDto {
  singleAnswers: SingleAnswerDto[];

  constructor(data: any) {
    this.singleAnswers = data.singleAnswers.map((sa: any) => new SingleAnswerDto(sa));
  }
}
