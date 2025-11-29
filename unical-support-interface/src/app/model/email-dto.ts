export abstract class EmailDto {
  id: string;
  to: string[];
  subject: string;
  body: string;

  protected constructor(data: any) {
    this.id = data.id;
    this.to = data.to;
    this.subject = data.subject;
    this.body = data.body;
  }
}
