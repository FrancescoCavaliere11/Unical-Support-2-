import { Pipe, PipeTransform } from '@angular/core';
import {SingleClassificationDto} from '../single-classification-dto';


@Pipe({
  name: 'categoriesFormat',
  standalone: true
})
export class CategoriesFormatPipe implements PipeTransform {

  transform(classifications: SingleClassificationDto[]): string {
    if (!classifications || classifications.length === 0) {
      return 'No category found.';
    }

    const firstCategory = classifications[0].category?.name || 'Unknown';

    if (classifications.length === 1) {
      return firstCategory;
    } else {
      return `${firstCategory} +${classifications.length - 1}`;
    }
  }

}
