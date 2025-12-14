import {ParameterDto} from './ParameterDto';
import {CategoryDto} from './category-dto';

export class TemplateDto {
  id: string;
  name: string;
  content: string;
  description: string;
  category: CategoryDto;
  parameters: ParameterDto[];

  constructor(data: any) {
    this.id = data.id;
    this.name = data.name;
    this.content = data.content;
    this.description = data.description;
    this.category = data.category;
    this.parameters = data.parameters;
  }
}
