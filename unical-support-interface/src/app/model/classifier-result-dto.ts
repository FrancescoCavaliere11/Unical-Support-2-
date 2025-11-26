import {CategoryDto} from './category-dto';

export class SingleClassificationDto {
  text: string;
  confidence: number;
  category: CategoryDto;

  constructor(data: any) {
    this.text = data.text;
    this.confidence = data.confidence;
    this.category = data.category;
  }

  get confidenceLabel() {
    const c = this.confidence ?? 0;
    if (c >= 60) return 'high';
    if (c >= 30) return 'mid';
    return 'low';
  }
}
