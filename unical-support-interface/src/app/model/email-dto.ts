import {ClassifyDto} from './classify-dto';
import {AnswerDto} from './answer-dto';

export class EmailDto {
  id: string;
  to: string[];
  subject: string;
  body: string;
  classify: ClassifyDto;
  answer: AnswerDto;

  constructor(data: any) {
    this.id = data.id;
    this.to = data.to;
    this.subject = data.subject;
    this.body = data.body;
    this.classify = data.classify;
    this.answer = data.answer;
  }
}

export { ClassifyDto };
