import {EmailDto} from './email-dto';
import {ResponderResultDto} from './responder-result-dto';



export class EmailToAnswerDto extends EmailDto{
  responderResult: ResponderResultDto

  constructor(data: any) {
    super(data);
    this.responderResult = data.responderResult;
  }
}
