import {SingleClassificationDto} from './single-classification-dto';

export class ClassifyDto {
  id: string;
  explanation: String;
  singleClassifications: SingleClassificationDto[]

  constructor(data: any) {
    this.id = data.id;
    this.explanation = data.explanation;
    this.singleClassifications = data.singleClassifications;
  }
}
