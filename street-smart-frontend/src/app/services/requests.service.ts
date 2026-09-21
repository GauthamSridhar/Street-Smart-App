import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environment';
export interface ShopApprovalResponseDTO {
  id: string;
  shopId: string;
  approvalStatus: string;
  approved: boolean;
  reason: string | null;
  deliveryPending: boolean;
  decidedBy: string | null;
  decidedAt: string | null;
}
@Injectable({ providedIn: 'root' })
export class RequestsService {
  private readonly url = environment.apiBaseUrl + '/approvals';
  constructor(private http: HttpClient) {}
  getPendingRequests(page = 0, size = 20) {
    return this.http.get<ShopApprovalResponseDTO[]>(this.url + '/pending', {
      params: { page, size },
    });
  }
  approveRequest(shopId: string) {
    return this.http.post<ShopApprovalResponseDTO>(this.url + '/' + shopId + '/approve', null);
  }
  rejectRequest(shopId: string, reason: string) {
    return this.http.post<ShopApprovalResponseDTO>(this.url + '/' + shopId + '/reject', null, {
      params: { reason },
    });
  }
  getRequestsCount(_userId = '') {
    return this.http.get<number>(this.url + '/pending/count');
  }
}
