import {CategoryDto} from './category-dto';

export class SingleClassificationDto {
  category: CategoryDto;
  confidence: number;
  text: string;

  constructor(data: any) {
    this.category = new CategoryDto(data.category);
    this.confidence = data.confidence;
    this.text = data.text;
  }
}
