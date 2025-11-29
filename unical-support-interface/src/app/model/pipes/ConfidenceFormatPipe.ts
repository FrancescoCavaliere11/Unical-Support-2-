import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'confidenceFormat',
  standalone: true
})
export class ConfidenceFormatPipe implements PipeTransform {

  transform(value: any): string {
    const confidence = Number(value);

    if (value === null || value === undefined || isNaN(confidence)) {
      return 'low';
    }

    if (confidence >= 60) return 'high';
    if (confidence >= 30) return 'mid';

    return 'low';
  }

}
