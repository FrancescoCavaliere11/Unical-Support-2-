export class CategoryDto {
  id: string;
  name: string;
  description: string;

  constructor(data: any) {
    this.id = data.id;
    this.name = data.name;
    this.description = data.description;
  }
}
