export class DocumentDto {
  id: string;
  name: string;
  documentLink: string;
  createInDate: Date;

  constructor(data: any) {
    this.id = data.id;
    this.name = data.name;
    this.documentLink = data.documentLink;
    this.createInDate = data.createInDate;
  }
}
