import { Pipe, PipeTransform } from '@angular/core';
import {EmailDto} from '../email-dto';

@Pipe({
  name: 'addressesFormat',
  standalone: true
})
export class AddressesFormatPipe implements PipeTransform {

  transform(email: EmailDto): string {
    if (!email || !email.to || email.to.length === 0) return 'No address found.';

    const firstAddress = email.to[0];
    if (email.to.length === 1) return firstAddress;

    return `${firstAddress} +${email.to.length - 1}`;
  }
}
