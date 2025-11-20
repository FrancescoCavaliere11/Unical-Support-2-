import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClassificationPage } from './classification-page';

describe('ClassificationPage', () => {
  let component: ClassificationPage;
  let fixture: ComponentFixture<ClassificationPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ClassificationPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ClassificationPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
