import {CategoryDto} from './category-dto';

export class EmailDto {
  id: string;
  from: string[];
  subject: string;
  content: string;
  confidence: number;
  category: CategoryDto

  constructor(data: any) {
    this.id = data.id;
    this.from = data.from;
    this.subject = data.subject;
    this.content = data.content;
    this.confidence = data.confidence;
    this.category = data.category;
  }

  get fromString(): string {
    if (this.from.length === 0) return 'No address found.'

    let firstAddress: string = this.from[0];
    if (this.from.length === 1) return firstAddress;
    else return firstAddress + " +" + (this.from.length - 1);
  }
}
