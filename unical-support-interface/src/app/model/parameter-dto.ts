export class ParameterDto {
  name: string;
  required: boolean;

  constructor(data: any) {
    this.name = data.name;
    this.required = data.required;
  }
}
