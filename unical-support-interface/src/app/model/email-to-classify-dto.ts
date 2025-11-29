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
}
