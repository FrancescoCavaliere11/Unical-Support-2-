export class DocumentDto {
  id: string;
  originalFilename: string;
  documentLink: string;
  createAt: Date;

  constructor(data: any) {
    this.id = data.id;
    this.originalFilename = data.originalFilename;
    this.documentLink = data.documentLink;
    this.createAt = data.createAt;
  }
}


