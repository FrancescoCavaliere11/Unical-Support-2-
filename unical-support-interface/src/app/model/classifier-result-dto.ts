import {CategoryDto} from './category-dto';

export class ClassifierResultDto {
  confidence: number;
  explanation: string;
  categories: CategoryDto[];

  constructor(data: any) {
    this.confidence = data.confidence;
    this.explanation = data.explanation;
    this.categories = data.category;
  }

  get categoriesToString(): string {
    if (this.categories.length === 0) return 'No category found.'

    let firstCategory: string = this.categories[0].name;
    if (this.categories.length === 1) return firstCategory;
    else return firstCategory + " +" + (this.categories.length - 1);
  }
}
