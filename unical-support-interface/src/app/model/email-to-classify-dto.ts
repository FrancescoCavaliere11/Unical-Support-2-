import {EmailDto} from './email-dto';
import {ClassifierResultDto} from './classifier-result-dto';

export class EmailToClassifyDto extends EmailDto{
  classifierResult: ClassifierResultDto;

  constructor(data: any) {
    super(data);
    this.classifierResult = data.classeifierResult;
  }

  get confidenceLabel() {
    const c = this.classifierResult.confidence ?? 0;
    if (c >= 60) return 'high';
    if (c >= 30) return 'mid';
    return 'low';
  }
}
