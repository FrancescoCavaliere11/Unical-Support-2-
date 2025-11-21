export class CategoryDto {
  id: string;
  name: string;

  constructor(data: any) {
    this.id = data.id;
    this.name = data.name;
  }
}
