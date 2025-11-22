import {CategoryDto} from './category-dto';

export class ResponderResultDto {
  categories: CategoryDto[];

  constructor(data: any) {
    this.categories = data.categories;
  }

  get categoriesToString(): string {
    if (this.categories.length === 0) return 'No category found.'

    let firstCategory: string = this.categories[0].name;
    if (this.categories.length === 1) return firstCategory;
    else return firstCategory + " +" + (this.categories.length - 1);
  }
}
