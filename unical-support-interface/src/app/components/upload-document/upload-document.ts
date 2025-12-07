import {ChangeDetectorRef, Component, EventEmitter, Output, OnInit} from '@angular/core';
import {
  Cancel01Icon,
  DocumentAttachmentIcon,
  Upload01Icon
} from '@hugeicons/core-free-icons';
import {CategoryDto} from '../../model/category-dto';
import {Category} from '../../services/category';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Document} from '../../services/document';

@Component({
  selector: 'app-upload-document',
  standalone: false,
  templateUrl: './upload-document.html',
  styleUrls: ['./upload-document.css', '../../../../public/styles/input.css'],
})
export class UploadDocument implements OnInit{
  protected readonly Upload01Icon = Upload01Icon;
  protected readonly Cancel01Icon = Cancel01Icon;
  protected readonly DocumentAttachmentIcon = DocumentAttachmentIcon;

  protected isLoading: boolean = false;
  protected dragOver: boolean = false;
  protected categories: CategoryDto[] = []
  protected form: FormGroup = new FormGroup({});

  @Output() close = new EventEmitter<void>();

  constructor(
    private categoryService: Category,
    private formBuilder: FormBuilder,
    private changeDetectorRef: ChangeDetectorRef,
    private documentService: Document,
  ) {
    this.form = this.formBuilder.group({
      categoryId: [''],
      files: [[]]
    })
  }

  get files() {
    return this.form.get('files')?.value || [];
  }

  get isUploadDisabled(): boolean {
    return this.files.length === 0 || !this.form.get('categoryId')?.value;
  }

  ngOnInit(): void {
    this.categoryService.getCategories()
      .subscribe(categories => this.categories = categories);
  }


  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();

    this.dragOver = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();

    this.dragOver = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.dragOver = false;

    const files = event.dataTransfer?.files;
    if (!files || files.length === 0) return;

    // todo controllare se ho già inserito dei file con lo stesso nome
    const currentFiles: File[] = this.form.get('files')?.value || [];

    const newFiles = Array.from(files);
    this.form.get('files')?.setValue([...currentFiles, ...newFiles]);
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    // todo controllare se ho già inserito dei file con lo stesso nome
    const currentFiles: File[] = this.form.get('files')?.value || [];
    this.form.get('files')?.setValue([...currentFiles, ...Array.from(input.files)]);

    console.log(this.form.value);
  }


  closePopup() {
    this.close.emit();
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  removeFile(index: number) {
    const currentFiles: File[] = this.form.get('files')?.value || [];
    currentFiles.splice(index, 1);
    this.form.get('files')?.setValue(currentFiles);
  }


  // API
  upload() {
    this.isLoading = true;
    this.documentService.uploadDocuments(this.files, this.form.value.categoryId)
      .subscribe({
        next: () => {
          this.changeDetectorRef.detectChanges();
          this.isLoading = false;
          this.closePopup();
        },
        error: err => {
          console.error(err) // todo
          alert("Errore durante l'upload dei documenti");
          this.changeDetectorRef.detectChanges();
          this.isLoading = false;
        }
      });
  }

}
