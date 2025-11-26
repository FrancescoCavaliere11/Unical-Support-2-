import {EmailDto} from './email-dto';
import {SingleClassificationDto} from './classifier-result-dto';

export class EmailToClassifyDto extends EmailDto{
  explanation: String;
  singleClassifications: SingleClassificationDto[];

  constructor(data: any) {
    super(data);
    this.explanation = data.explanation;
    this.singleClassifications = data.singleClassifications;
  }

  get categoriesToString(): string {
    if (this.singleClassifications.length === 0) return 'No category found.'

    let firstCategory: string = this.singleClassifications[0].category.name;
    if (this.singleClassifications.length === 1) return firstCategory;
    else return firstCategory + " +" + (this.singleClassifications.length - 1);
  }
}
