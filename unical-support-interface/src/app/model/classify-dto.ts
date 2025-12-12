import {SingleClassificationDto} from './single-classification-dto';

export class ClassifyDto {
  id: string;
  explanation: String;
  isClassified: boolean;
  singleClassifications: SingleClassificationDto[]

  constructor(data: any) {
    this.id = data.id;
    this.explanation = data.explanation;
    this.isClassified = data.isClassified;
    this.singleClassifications = data.singleClassifications;
  }
}
