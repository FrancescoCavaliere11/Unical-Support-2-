import {SingleClassificationDto} from './single-classification-dto';

export class ClassifyDto {
  explanation: String;
  singleClassifications: SingleClassificationDto[]

  constructor(data: any) {
    this.explanation = data.explanation;
    this.singleClassifications = data.singleClassifications.map((item: any) => new SingleClassificationDto(item));
  }
}
