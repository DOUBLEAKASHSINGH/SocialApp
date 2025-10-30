import { TestBed } from '@angular/core/testing';

import { RelationApiService } from './relation-api.service';

describe('RelationApiService', () => {
  let service: RelationApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RelationApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
