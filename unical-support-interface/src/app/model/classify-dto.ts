import {SingleClassificationDto} from './single-classification-dto';

export class ClassifyDto {
  id: string;
  explanation: string;
  classified: boolean;
  singleClassifications: SingleClassificationDto[]

  constructor(data: any) {
    this.id = data.id;
    this.explanation = data.explanation;
    this.classified = data.classified;
    this.singleClassifications = data.singleClassifications;
  }
}
