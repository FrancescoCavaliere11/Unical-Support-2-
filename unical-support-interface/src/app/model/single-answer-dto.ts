import {TemplateDto} from './template-dto';

export class SingleAnswerDto {
  answer: string;
  template: TemplateDto;

  constructor(data: any) {
    this.answer = data.answer;
    this.template = data.template;
  }
}
