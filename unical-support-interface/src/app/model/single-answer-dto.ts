import {TemplateDto} from './TemplateDto';

export class SingleAnswerDto {
  answer: string;
  template: TemplateDto;

  constructor(data: any) {
    this.answer = data.answer;
    this.template = data.template;
  }
}
